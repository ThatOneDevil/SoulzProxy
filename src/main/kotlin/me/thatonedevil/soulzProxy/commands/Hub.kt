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

        val hubSuccessMessage: String = Config.getMessage("messages.hub.hubSuccess")
        val hubServerNotFoundMessage: String = Config.getMessage("messages.hub.hubError")


        server.ifPresentOrElse({
            source.createConnectionRequest(it).fireAndForget()
            source.sendMessage(convertLegacyToMiniMessage(hubSuccessMessage))
        }, {
            source.sendMessage(convertLegacyToMiniMessage(hubServerNotFoundMessage))
        })
    }

}