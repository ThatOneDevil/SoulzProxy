package me.thatonedevil.soulzProxy.discordCommands

import com.velocitypowered.api.proxy.ProxyServer
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class PlayerList(var proxy: ProxyServer) : ListenerAdapter() {

    override fun onSlashCommandInteraction(e: SlashCommandInteractionEvent) {
        if (e.name == "playerlist") {

            val member = e.member
            if (member == null || !member.hasPermission(Permission.ADMINISTRATOR)) {
                e.reply("❌ You do not have permission to use this command!").setEphemeral(true).queue()
                return
            }

            val server = e.getOption("server")?.asString

            val playerList = proxy.getServer(server).get().playersConnected
            val names: MutableList<String> = mutableListOf()
            for (player in playerList) {
                names.add(player.username)
            }

            val message = StringBuilder()
            message.append("**Online Players:**\n")
            if (playerList.isEmpty()) {
                message.append("No players online.")
            } else {
                message.append("\n``${names.joinToString(", ")}``\n")
            }

            message.append("\n**Total Players:** ${playerList.size}")

            e.reply(message.toString()).queue()
        }
    }
}