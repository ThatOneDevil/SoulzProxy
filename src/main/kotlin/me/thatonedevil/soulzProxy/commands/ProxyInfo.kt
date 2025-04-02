package me.thatonedevil.soulzProxy.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.ProxyServer
import me.thatonedevil.soulzProxy.SoulzCommandAdmin
import me.thatonedevil.soulzProxy.utils.Utils.convertLegacyToMiniMessage
import net.kyori.adventure.text.Component

class ProxyInfo(override var commandName: String, override var aliases: String?, override var proxy: ProxyServer) : SoulzCommandAdmin {
    override fun execute(p0: SimpleCommand.Invocation?) {
        val source = p0?.source()

        val servers = proxy.allServers

        if (source != null) {
            val message = Component.text("")
                .append(convertLegacyToMiniMessage("&r<color:#F11642>=== Proxy Information ===\n"))
                .append(convertLegacyToMiniMessage(" <color:#EF5B80>&l▍ &r<color:#EF5B80>Proxy Version: <color:#F11642>${proxy.version.version}\n"))
                .append(convertLegacyToMiniMessage(" <color:#EF5B80>&l▍ &r<color:#EF5B80>Number of Players: <color:#F11642>${proxy.playerCount}\n"))
                .append(convertLegacyToMiniMessage(" <color:#EF5B80>&l▍ &r<color:#EF5B80>Server Count: <color:#F11642>${servers.size}\n\n"))
                .append(convertLegacyToMiniMessage("&r<color:#F11642>=== Servers List ===\n"))

            for (server in servers) {
                val playerCount = server.playersConnected.size
                message.append(convertLegacyToMiniMessage(" <color:#EF5B80>&l▍ &r<color:#EF5B80>${server.serverInfo.name}: <color:#F11642>$playerCount\n"))
            }

            source.sendMessage(message)
        } else {
            println("Command executed from console.")
        }


    }
}