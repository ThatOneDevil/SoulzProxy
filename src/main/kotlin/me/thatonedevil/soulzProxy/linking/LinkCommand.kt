package me.thatonedevil.soulzProxy.linking

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import me.thatonedevil.soulzProxy.SoulzCommand
import me.thatonedevil.soulzProxy.SoulzProxy.Companion.instance
import me.thatonedevil.soulzProxy.utils.Config.getMessage
import me.thatonedevil.soulzProxy.utils.Utils.convertLegacyToMiniMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import java.util.*
import java.util.concurrent.TimeUnit

class LinkCommand(override var commandName: String, override var aliases: String?, override var proxy: ProxyServer) : SoulzCommand {
    companion object {
        fun getUUIDFromCode(code: String): UUID? = DataManager.getUUIDFromCode(code)

        fun removeCode(code: String) = DataManager.removeLinkingCode(code)
    }

    override fun execute(invocation: SimpleCommand.Invocation) {
        val source = invocation.source()

        if (source !is Player) {
            source.sendMessage(Component.text("Only players can use this command."))
            return
        }

        if (LinkManager(source).isLinked) {
            val miniMessageFormatted =
                convertLegacyToMiniMessage(getMessage("messages.linkCommand.linkedError"))
            source.sendMessage(miniMessageFormatted)
            return
        }

        val code = (1..6)
            .map { ('A'..'Z') + ('0'..'9') }
            .flatten()
            .shuffled()
            .take(6)
            .joinToString("")

        DataManager.storeLinkingCode(code, source.uniqueId)

        val rawMessage = getMessage("messages.linkCommand.linkCodeMessage")
        val formattedMessage = rawMessage.replace("<code>", code)
        val miniMessageFormatted = convertLegacyToMiniMessage(formattedMessage)

        source.sendMessage(miniMessageFormatted.clickEvent(ClickEvent.copyToClipboard(code)))

        proxy.scheduler.buildTask(instance, Runnable {
            if (DataManager.getUUIDFromCode(code) != null) {
                DataManager.removeLinkingCode(code)
                val expiredMessage = getMessage("messages.linkCommand.linkCodeExpired")
                val miniMessageExpired = convertLegacyToMiniMessage(expiredMessage)
                source.sendMessage(miniMessageExpired)
            }
        }).delay(30L, TimeUnit.SECONDS).schedule();
    }



}