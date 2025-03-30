package me.thatonedevil.soulzProxy.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import jdk.internal.org.jline.reader.impl.DefaultParser.ArgumentList
import me.thatonedevil.soulzProxy.SoulzCommand
import me.thatonedevil.soulzProxy.utils.Config
import me.thatonedevil.soulzProxy.utils.Utils.convertLegacyToMiniMessage
import net.kyori.adventure.text.Component
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collectors


class SendPlayerToServer(override var commandName: String, override var aliases: String?,  override var proxy: ProxyServer ) : SoulzCommand {

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
            source.sendMessage(convertLegacyToMiniMessage(Config.getMessage("messages.sendCommand.noArgumentsPlayer")))
            return
        }

        if (invocation.arguments().size < 2) {
            source.sendMessage(convertLegacyToMiniMessage(Config.getMessage("messages.sendCommand.noArgumentsServer")))
            return
        }

        val player = proxy.getPlayer(invocation.arguments()[0]).get()
        val serverName = invocation.arguments()[1]
        val server = proxy.getServer(serverName)

        val sendPlayerToServerMessage: String = Config.getMessage("messages.sendCommand.sendPlayerToServer")
            .replace("<player>", player.username)
            .replace("<server>", serverName)
        val sendPlayerToServerErrorMessage: String = Config.getMessage("messages.sendCommand.noServerError")

        server.ifPresentOrElse({
            player.createConnectionRequest(it).fireAndForget()
            source.sendMessage(convertLegacyToMiniMessage(sendPlayerToServerMessage))
        }, {
            source.sendMessage(convertLegacyToMiniMessage(sendPlayerToServerErrorMessage))
        })
    }


    override fun suggestAsync(invocation: SimpleCommand.Invocation): CompletableFuture<List<String>> {
        if (invocation.arguments().isEmpty()) {
            val players = proxy.allPlayers.stream()
                .map { player: Player -> player.username }
                .collect(Collectors.toList())

            return CompletableFuture.completedFuture(players)
        }

        if (invocation.arguments().size == 2){
            val serverNames = proxy.allServers.stream()
                .map { server -> server.serverInfo.name }
                .collect(Collectors.toList())

            return CompletableFuture.completedFuture(serverNames)
        }

        return CompletableFuture.completedFuture(emptyList())
    }

    override fun hasPermission(invocation: SimpleCommand.Invocation): Boolean {
        return invocation.source().hasPermission("soulzProxy.sendPlayerToServer");
    }


}