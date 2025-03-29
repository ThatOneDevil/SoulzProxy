package me.thatonedevil.soulzProxy;

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.RegisteredServer
import me.thatonedevil.soulzProxy.commands.Hub
import me.thatonedevil.soulzProxy.commands.ServerBroadcast
import me.thatonedevil.soulzProxy.utils.Config
import net.luckperms.api.LuckPerms
import org.slf4j.Logger


@Plugin(
    id = "soulzproxy",
    name = "SoulzProxy",
    version = BuildConstants.VERSION,
    url = "https://thatonedevil.github.io/",
    authors = ["ThatOneDevil"],
    dependencies = [
        Dependency(id = "luckperms"),
    ]
)


class SoulzProxy @Inject constructor(private val logger: Logger, private var proxy: ProxyServer) {

    companion object {
        lateinit var instance: SoulzProxy
    }

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        instance = this
        logger.info("SoulzProxy is now running!")

        val commandManager = proxy.commandManager
        val hubCommand = Hub("hub", null, proxy)
        val serverBroadcast = ServerBroadcast("serverbroadcast", "sb", proxy)

        commandManager.register(hubCommand.commandMeta(), hubCommand)
        commandManager.register(serverBroadcast.commandMeta(), serverBroadcast)

        Config.loadConfigAsync()

    }

    @Subscribe
    fun onProxyInitialization(event: ProxyShutdownEvent) {
        logger.info("SoulzProxy is shutting down!")
    }

}
