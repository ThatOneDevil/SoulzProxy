package me.thatonedevil.soulzProxy.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import me.thatonedevil.soulzProxy.SoulzCommandAdmin
import me.thatonedevil.soulzProxy.utils.Config
import me.thatonedevil.soulzProxy.utils.Utils.convertLegacyToMiniMessage
import net.kyori.adventure.text.Component

class ServerBroadcast(override var commandName: String, override var aliases: String?, override var proxy: ProxyServer,) : SoulzCommandAdmin {
    override fun execute(invocation: SimpleCommand.Invocation) {
        val source = invocation.source()

        if (source !is Player) {
            source.sendMessage(Component.text("Only players can use this command."))
            return
        }

        val serverConnection = source.currentServer.get()

        if (invocation.arguments().isEmpty()) {
            source.sendMessage(convertLegacyToMiniMessage(Config.getServerSpecificMessage("messages.broadcast.noArguments", serverConnection)))
            return
        }

        val message = invocation.arguments().joinToString(" ")
        val broadcastMessage: String = Config.getServerSpecificMessage("messages.broadcast.broadcastMessage", serverConnection)
            .replace("<message>", message)
            .replace("<player>", source.username)

        proxy.sendMessage(Component.empty())
        proxy.sendMessage(convertLegacyToMiniMessage(broadcastMessage))
        proxy.sendMessage(Component.empty())

    }
}