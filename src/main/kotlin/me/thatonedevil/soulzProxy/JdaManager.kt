package me.thatonedevil.soulzProxy

import com.velocitypowered.api.proxy.ProxyServer
import me.thatonedevil.soulzProxy.linking.LinkEmbed
import me.thatonedevil.soulzProxy.linking.UserInfoCommand
import me.thatonedevil.soulzProxy.utils.Config
import me.thatonedevil.soulzProxy.utils.Config.getMessage
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import java.sql.DriverManager
import java.util.concurrent.CompletableFuture

object JdaManager {
    private lateinit var jda: JDA
    var jdaEnabled: Boolean = true
    private var isReady: Boolean = false
    var guild: Guild? = null
    var verifiedRole: Role? = null

    fun init(token: String, proxy: ProxyServer) {
        if (!jdaEnabled) return

        CompletableFuture.runAsync {
            runCatching {
                jda = JDABuilder.createDefault(token)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .setBulkDeleteSplittingEnabled(false)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(LinkEmbed(proxy), UserInfoCommand(proxy))
                    .build()
                    .awaitReady()

                isReady = true
                validateJDAConfig()
                registerCommands()

            }.onFailure {
                logError(("Failed to init JDA: ${it.message}"))
            }
        }
    }

    fun shutdown() {
        if (!jdaEnabled) return
        if (!isReady) return

        jda.shutdownNow()
        DriverManager.getConnection(getMessage("database.jdbcString")).close()
    }

    private fun validateJDAConfig() {
        if (!jdaEnabled) return

        val guildId = getMessage("guildId")
        val verifiedRoleId = getMessage("verifiedRole")

        guild = jda.getGuildById(guildId ?: "")
        verifiedRole = guild?.getRoleById(verifiedRoleId ?: "")

        if (guild == null) logError("Guild not found ($guildId)")
        if (verifiedRole == null) logError("Verified role not found ($verifiedRoleId)")
    }

    private fun registerCommands() {
        if (!jdaEnabled) return

        guild?.updateCommands()?.addCommands(
            Commands.slash("linkembed", "Makes the link embed"),
            Commands.slash("userinfo", "Shows user info")
                .addOptions(OptionData(OptionType.USER, "user", "The user to show info for").setRequired(true))
        )?.queue()
    }


    fun updateChannelTopic(online: Boolean = true, proxy: ProxyServer) {
        if (!jdaEnabled) return

        if (!isReady) return

        val topic = if (online) "Players: ${proxy.allPlayers.size}" else "Server Offline"
        jda.presence.activity = Activity.watching(topic)

        SoulzProxy.instance.logger.info("Updated channel topic to: $topic")
    }

    private fun logError(msg: String) {
        SoulzProxy.instance.logger.error(msg)
    }
}
