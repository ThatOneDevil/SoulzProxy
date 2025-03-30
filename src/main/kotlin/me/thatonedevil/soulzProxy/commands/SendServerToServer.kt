package me.thatonedevil.soulzProxy.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import me.thatonedevil.soulzProxy.SoulzCommandAdmin
import me.thatonedevil.soulzProxy.utils.Config
import me.thatonedevil.soulzProxy.utils.Utils.convertLegacyToMiniMessage
import net.kyori.adventure.text.Component
import java.util.concurrent.CompletableFuture
import java.util.stream.Collectors
import kotlin.jvm.optionals.getOrElse


class SendServerToServer(override var commandName: String, override var aliases: String?, override var proxy: ProxyServer ) : SoulzCommandAdmin {

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

        val noServerError = convertLegacyToMiniMessage(Config.getMessage("messages.sendCommand.noArgumentsServer"))
        if (invocation.arguments().isEmpty()) {
            source.sendMessage(noServerError)
            return
        }

        if (invocation.arguments().size < 2) {
            source.sendMessage(noServerError)
            return
        }

        val initialServer = proxy.getServer(invocation.arguments()[0]).getOrElse { null }
        val destinationServer = proxy.getServer(invocation.arguments()[1]).getOrElse { null }

        if (initialServer == null || destinationServer == null) {
            source.sendMessage(convertLegacyToMiniMessage(Config.getMessage("messages.sendCommand.noServerError")))
            return
        }

        val sendServerToServerMessage: String = Config.getMessage("messages.sendCommand.sendServerToServer")
            .replace("<server1>", initialServer.serverInfo.name)
            .replace("<server2>", destinationServer.serverInfo.name)

        source.sendMessage(convertLegacyToMiniMessage(sendServerToServerMessage))

        for (player in initialServer.playersConnected) {
            player.createConnectionRequest(destinationServer).connect()
        }

    }

    override fun suggestAsync(invocation: SimpleCommand.Invocation): CompletableFuture<List<String>> {
        if (invocation.arguments().isEmpty() || invocation.arguments().size == 2){
            val serverNames = proxy.allServers.stream()
                .map { server -> server.serverInfo.name }
                .collect(Collectors.toList())

            return CompletableFuture.completedFuture(serverNames)
        }

        return CompletableFuture.completedFuture(emptyList())
    }

}