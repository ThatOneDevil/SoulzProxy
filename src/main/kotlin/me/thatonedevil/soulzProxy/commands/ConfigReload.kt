package me.thatonedevil.soulzProxy.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import me.thatonedevil.soulzProxy.SoulzCommandAdmin
import me.thatonedevil.soulzProxy.utils.Config
import me.thatonedevil.soulzProxy.utils.Utils.convertLegacyToMiniMessage
import net.kyori.adventure.text.Component

class ConfigReload(override var commandName: String, override var aliases: String?, override var proxy: ProxyServer) : SoulzCommandAdmin {
    override fun execute(invocation: SimpleCommand.Invocation) {

        val source = invocation.source()

        Config.loadConfigAsync()

        if (source is Player) {
            source.sendMessage(convertLegacyToMiniMessage(Config.getServerSpecificMessage("messages.global.configReloadSuccess", source.currentServer.get())))
            return
        }

        proxy.sendMessage(Component.text("Config reloaded successfully."))


    }
}