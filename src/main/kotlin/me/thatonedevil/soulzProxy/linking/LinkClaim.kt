package me.thatonedevil.soulzProxy.linking

import com.google.common.io.ByteStreams
import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import me.thatonedevil.soulzProxy.SoulzCommand
import me.thatonedevil.soulzProxy.linking.database.DataManager
import me.thatonedevil.soulzProxy.utils.Config.getServerSpecificMessage
import me.thatonedevil.soulzProxy.utils.Utils
import me.thatonedevil.soulzProxy.utils.Utils.convertLegacyToMiniMessage
import net.kyori.adventure.text.Component

class LinkClaim(override var commandName: String, override var aliases: String?, override var proxy: ProxyServer) : SoulzCommand {
    override fun execute(invocation: SimpleCommand.Invocation) {

        val source = invocation.source()

        if (source !is Player) {
            source.sendMessage(Component.text("Only players can use this command."))
            return
        }

        val playerData = DataManager.getPlayerData(source)
        val serverConnection = source.currentServer.get()

        if (!playerData.linked) {
            source.sendMessage(convertLegacyToMiniMessage(getServerSpecificMessage("messages.linkCommand.linkedError", serverConnection)))
            return
        }

        val out = ByteStreams.newDataOutput()
        out.writeUTF(playerData.userId)
        out.writeBoolean(true)

        Utils.sendPluginMessageToBackendUsingPlayer(source, MinecraftChannelIdentifier.from("soulzproxy:main"), out.toByteArray());
    }
}