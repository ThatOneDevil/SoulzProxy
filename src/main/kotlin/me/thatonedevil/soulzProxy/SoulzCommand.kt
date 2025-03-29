package me.thatonedevil.soulzProxy

import com.velocitypowered.api.command.CommandMeta
import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.ProxyServer
import me.thatonedevil.soulzProxy.SoulzProxy.Companion.instance

interface SoulzCommand : SimpleCommand {

    var commandName: String
    var proxy: ProxyServer
    var aliases: String?

    fun commandMeta(): CommandMeta {
        val commandMeta = proxy.commandManager.metaBuilder(commandName)
            .plugin(instance)
            .aliases(aliases ?: "") // If aliases are null, use an empty string
            .build()

        return commandMeta
    }



}
