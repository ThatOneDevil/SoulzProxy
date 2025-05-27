package me.thatonedevil.soulzProxy

import com.velocitypowered.api.proxy.ProxyServer
import me.thatonedevil.soulzProxy.SoulzProxy.Companion.redisBungeeAPI
import me.thatonedevil.soulzProxy.discordCommands.PlayerList
import me.thatonedevil.soulzProxy.discordCommands.ProxyInfoCommand
import me.thatonedevil.soulzProxy.discordCommands.UserInfoCommand
import me.thatonedevil.soulzProxy.linking.LinkEmbed
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
    private var isReady: Boolean = false
    var guild: Guild? = null
    var verifiedRole: Role? = null

    fun init(token: String, proxy: ProxyServer) {
        CompletableFuture.runAsync {
            runCatching {
                jda = JDABuilder.createDefault(token)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .setBulkDeleteSplittingEnabled(false)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(LinkEmbed(proxy), UserInfoCommand(proxy), ProxyInfoCommand(proxy), PlayerList(proxy))
                    .build()
                    .awaitReady()

                isReady = true
                validateJDAConfig()
                registerCommands(proxy)

            }.onFailure {
                logError(("Failed to init JDA: ${it.message}"))
            }
        }
    }

    fun shutdown() {
        if (!isReady) return

        jda.shutdownNow()
        DriverManager.getConnection(getMessage("database.jdbcString")).close()
    }

    private fun validateJDAConfig() {

        val guildId = getMessage("guildId")
        val verifiedRoleId = getMessage("verifiedRole")

        guild = jda.getGuildById(guildId ?: "")
        verifiedRole = guild?.getRoleById(verifiedRoleId ?: "")

        if (guild == null) logError("Guild not found ($guildId)")
        if (verifiedRole == null) logError("Verified role not found ($verifiedRoleId)")
    }

    private fun registerCommands(proxy: ProxyServer) {
        val serverNames = proxy.allServers.map { it.serverInfo.name }

        val serverOption = OptionData(OptionType.STRING, "server", "The server to show the player list for", true)
        for (server in serverNames) {
            serverOption.addChoice(server, server)
        }

        guild?.updateCommands()?.addCommands(
            Commands.slash("linkembed", "Makes the link embed"),
            Commands.slash("userinfo", "Shows user info")
                .addOptions(OptionData(OptionType.USER, "user", "The user to show info for", true)),
            Commands.slash("proxyinfo", "proxyinfo"),
            Commands.slash("playerlist", "Shows the player list")
                .addOptions(serverOption)
        )?.queue()
    }

    fun updateChannelTopic(online: Boolean = true) {
        if (!isReady) return

        val topic = if (online) "Global Players: ${redisBungeeAPI.playersOnline.size}" else "Server Offline"
        jda.presence.activity = Activity.watching(topic)

        SoulzProxy.instance.logger.info("Updated status: $topic")
    }

    private fun logError(msg: String) {
        SoulzProxy.instance.logger.error(msg)
    }
}
