package me.thatonedevil.soulzProxy.discordCommands

import com.velocitypowered.api.proxy.ProxyServer
import me.thatonedevil.soulzProxy.linking.database.DataManager
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.awt.Color

class UserInfoCommand(var proxy: ProxyServer) : ListenerAdapter() {

    override fun onSlashCommandInteraction(e: SlashCommandInteractionEvent) {
        if (e.name == "userinfo") {

            val member = e.member
            if (member == null || !member.hasPermission(Permission.ADMINISTRATOR)) {
                e.reply("❌ You do not have permission to use this command!").setEphemeral(true).queue()
                return
            }

            val user = e.getOption("user")?.asUser!!
            val uuidOfUser = DataManager.getUUIDFromDiscordId(user.id)

            val minecraftName = proxy.getPlayer(uuidOfUser).get().username ?: "Unknown"

            val embed = EmbedBuilder()
                .setTitle("User Information")
                .setColor(Color(46, 204, 113))
                .setThumbnail(user.avatarUrl)
                .addField("Discord ID", user.id, false)
                .addField("Minecraft Name", minecraftName, false)
                .setFooter("SoulzSteal Linking System • ThatOneDevil", null)
                .build()

            e.replyEmbeds(embed).setEphemeral(true).queue()

        }
    }
}