package me.thatonedevil.soulzProxy.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import me.thatonedevil.soulzProxy.SoulzCommand
import me.thatonedevil.soulzProxy.utils.Config
import me.thatonedevil.soulzProxy.utils.Utils.convertLegacyToMiniMessage
import net.kyori.adventure.text.Component

class Hub(override var commandName: String, override var aliases: String?, override var proxy: ProxyServer) : SoulzCommand {
    override fun execute(invocation: SimpleCommand.Invocation) {
        val source = invocation.source()

        if (source !is Player) {
            source.sendMessage(Component.text("Only players can use this command."))
            return
        }

        val server = proxy.getServer("hub")

        val hubSuccessMessage: String = Config.getServerSpecificMessage("messages.hub.hubSuccess", source.currentServer.get())
        val hubServerNotFoundMessage: String = Config.getServerSpecificMessage("messages.hub.hubError", source.currentServer.get())

        server.ifPresentOrElse({
            source.createConnectionRequest(it).connect()
            source.sendMessage(convertLegacyToMiniMessage(hubSuccessMessage))
        }, {
            source.sendMessage(convertLegacyToMiniMessage(hubServerNotFoundMessage))
        })
    }

}