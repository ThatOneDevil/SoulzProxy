package me.thatonedevil.soulzProxy.discordCommands


import com.velocitypowered.api.proxy.ProxyServer
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class ProxyInfoCommand(var proxy: ProxyServer) : ListenerAdapter() {

    override fun onSlashCommandInteraction(e: SlashCommandInteractionEvent) {
        if (e.name == "proxyinfo") {

            val member = e.member
            if (member == null || !member.hasPermission(Permission.ADMINISTRATOR)) {
                e.reply("❌ You do not have permission to use this command!").setEphemeral(true).queue()
                return
            }

            val servers = proxy.allServers

            val message = StringBuilder()
            message.append("=== Proxy Information ===\n")
                .append(" ▍ **Proxy Version:** ``${proxy.version.version}``\n")
                .append(" ▍ **Number of Players:** ``${proxy.playerCount}``\n")
                .append(" ▍ **Server Count:** ``${servers.size}``\n\n")
                .append("=== Servers List ===\n")

            for (server in servers) {
                val playerCount = server.playersConnected.size
                message.append(" ▍ **${server.serverInfo.name}**: ``$playerCount``\n")
            }

            e.reply(message.toString()).queue()
        }
    }
}