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


class Send(override var commandName: String, override var aliases: String?, override var proxy: ProxyServer) :
    SoulzCommandAdmin {


    override fun execute(invocation: SimpleCommand.Invocation) {
        val source = invocation.source()

        if (source !is Player) {
            source.sendMessage(Component.text("Only players can use this command."))
            return
        }

        val serverConnection = source.currentServer.get()

        if (invocation.arguments().isEmpty()) {
            source.sendMessage(
                convertLegacyToMiniMessage(
                    Config.getServerSpecificMessage(
                        "messages.sendCommand.noArgumentsPlayer",
                        serverConnection
                    )
                )
            )
            return
        }

        if (invocation.arguments().size < 2) {
            source.sendMessage(
                convertLegacyToMiniMessage(
                    Config.getServerSpecificMessage(
                        "messages.sendCommand.noArgumentsServer",
                        serverConnection
                    )
                )
            )
            return
        }

        val playerArg = invocation.arguments()[0]
        val serverNameArg = invocation.arguments()[1]
        val server = proxy.getServer(serverNameArg).getOrElse {
            source.sendMessage(
                convertLegacyToMiniMessage(
                    Config.getServerSpecificMessage(
                        "messages.sendCommand.noArgumentsServer",
                        serverConnection
                    )
                )
            )
            return
        }

        if (playerArg == "all") {
            val sendPlayerToServerMessage: String =
                Config.getServerSpecificMessage("messages.sendCommand.sendServerToServer", serverConnection)
                    .replace("<server>", serverNameArg)

            for (player in source.currentServer.get().server.playersConnected) {
                player.createConnectionRequest(server).connect()
            }

            source.sendMessage(convertLegacyToMiniMessage(sendPlayerToServerMessage))

            return
        }

        val player = proxy.getPlayer(invocation.arguments()[0]).getOrElse {
            source.sendMessage(
                convertLegacyToMiniMessage(
                    Config.getServerSpecificMessage(
                        "messages.sendCommand.noArgumentsPlayer",
                        serverConnection
                    )
                )
            )
            return
        }

        player.createConnectionRequest(server).connect()

        val sendPlayerToServerMessage: String =
            Config.getServerSpecificMessage("messages.sendCommand.sendPlayerToServer", serverConnection)
                .replace("<player>", player.username)
                .replace("<server>", serverNameArg)

        source.sendMessage(convertLegacyToMiniMessage(sendPlayerToServerMessage))
    }


    override fun suggestAsync(invocation: SimpleCommand.Invocation): CompletableFuture<List<String>> {
        if (invocation.arguments().isEmpty()) {
            val players = mutableListOf("all")

            players.addAll(
                proxy.allPlayers.stream()
                    .map { player: Player -> player.username }
                    .collect(Collectors.toList()))

            return CompletableFuture.completedFuture(players)
        }

        if (invocation.arguments().size == 2) {
            val serverNames = proxy.allServers.stream()
                .map { server -> server.serverInfo.name }
                .collect(Collectors.toList())

            return CompletableFuture.completedFuture(serverNames)
        }

        return CompletableFuture.completedFuture(emptyList())
    }


}