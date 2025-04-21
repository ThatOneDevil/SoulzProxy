package me.thatonedevil.soulzProxy.utils

import com.google.common.io.ByteStreams
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ServerConnection
import com.velocitypowered.api.proxy.messages.ChannelIdentifier
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import me.thatonedevil.soulzProxy.SoulzProxy
import java.util.*

object MessagingUtils {

    fun notifyBackend(player: Player) {
        val out = ByteStreams.newDataOutput().apply {
            writeUTF(player.uniqueId.toString())
            writeBoolean(true)
        }
        sendPluginMessageToBackendUsingPlayer(
            player,
            MinecraftChannelIdentifier.from("soulzproxy:main"),
            out.toByteArray()
        )
    }

    private fun sendPluginMessageToBackendUsingPlayer(player: Player, identifier: ChannelIdentifier, data: ByteArray): Boolean {
        val connection: Optional<ServerConnection> = player.currentServer
        if (connection.isPresent) {
            val serverConnection: ServerConnection = connection.get()
            SoulzProxy.instance.logger.info("[Messaging] Sending plugin message to backend using player '${player.username}' on server: ${serverConnection.server.serverInfo.name}")
            return serverConnection.sendPluginMessage(identifier, data)
        }
        return false
    }
}