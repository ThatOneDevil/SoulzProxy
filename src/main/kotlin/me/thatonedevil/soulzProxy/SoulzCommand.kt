package me.thatonedevil.soulzProxy

import com.velocitypowered.api.command.CommandMeta
import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.ProxyServer
import me.thatonedevil.soulzProxy.SoulzProxy.Companion.instance

interface SoulzCommand : SimpleCommand {

    var commandName: String
    var aliases: String?
    var proxy: ProxyServer

    fun commandMeta(): CommandMeta {
        val commandMeta = proxy.commandManager.metaBuilder(commandName)
        commandMeta.plugin(instance)

        if (aliases != null) {
            commandMeta.aliases(aliases)
        }

        return commandMeta.build()
    }
}   