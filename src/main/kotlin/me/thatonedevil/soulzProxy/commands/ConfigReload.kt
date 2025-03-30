package me.thatonedevil.soulzProxy.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.ProxyServer
import me.thatonedevil.soulzProxy.SoulzCommand
import me.thatonedevil.soulzProxy.utils.Config
import me.thatonedevil.soulzProxy.utils.Utils.convertLegacyToMiniMessage

class ConfigReload(override var commandName: String, override var aliases: String?, override var proxy: ProxyServer) : SoulzCommand {
    override fun execute(invocation: SimpleCommand.Invocation) {

        val source = invocation.source()

        Config.loadConfigAsync()

        source.sendMessage(convertLegacyToMiniMessage(Config.getMessage("messages.global.configReloadSuccess")))

    }

    override fun hasPermission(invocation: SimpleCommand.Invocation): Boolean {
        return invocation.source().hasPermission("soulzProxy.configreload");
    }
}