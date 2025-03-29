package me.thatonedevil.soulzProxy.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import me.thatonedevil.soulzProxy.SoulzCommand
import me.thatonedevil.soulzProxy.utils.Config
import me.thatonedevil.soulzProxy.utils.Utils.convertLegacyToMiniMessage
import net.kyori.adventure.text.Component

class ServerBroadcast(override var commandName: String, override var aliases: String?, override var proxy: ProxyServer,) : SoulzCommand {
    override fun execute(invocation: SimpleCommand.Invocation) {
        val source = invocation.source()

        if (source !is Player) {
            source.sendMessage(Component.text("Only players can use this command."))
            return
        }

        if (!hasPermission(invocation)) {
            source.sendMessage(convertLegacyToMiniMessage(Config.getMessage("messages.global.permissionError")))
            return
        }

        if (invocation.arguments().isEmpty()) {
            source.sendMessage(convertLegacyToMiniMessage(Config.getMessage("messages.broadcast.noArguments")))
            return
        }

        val message = invocation.arguments().joinToString(" ")
        val broadcastMessage: String = Config.getMessage("messages.broadcast.broadcastMessage").replace("<message>", message)


        proxy.sendMessage(Component.empty())
        proxy.sendMessage(convertLegacyToMiniMessage(broadcastMessage))
        proxy.sendMessage(Component.empty())

    }

    override fun hasPermission(invocation: SimpleCommand.Invocation): Boolean {
        return invocation.source().hasPermission("soulzProxy.serverbroadcast");
    }
}