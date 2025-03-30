package me.thatonedevil.soulzProxy

import com.velocitypowered.api.command.SimpleCommand

interface SoulzCommandAdmin : SoulzCommand {

    override fun hasPermission(invocation: SimpleCommand.Invocation): Boolean {
        return invocation.source().hasPermission("soulzProxy.${commandName}")
    }
}
