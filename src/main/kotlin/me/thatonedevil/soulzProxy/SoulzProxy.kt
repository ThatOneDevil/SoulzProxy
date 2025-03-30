package me.thatonedevil.soulzProxy;

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import me.thatonedevil.soulzProxy.commands.ConfigReload
import me.thatonedevil.soulzProxy.commands.Hub
import me.thatonedevil.soulzProxy.commands.SendPlayerToServer
import me.thatonedevil.soulzProxy.commands.ServerBroadcast
import me.thatonedevil.soulzProxy.utils.Config
import org.slf4j.Logger
import java.nio.file.Path


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


class SoulzProxy @Inject constructor(private val logger: Logger, private var proxy: ProxyServer, @DataDirectory val dataDirectory: Path) {

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
        val configReload = ConfigReload("configreload", null, proxy)
        val sendPlayerToServer = SendPlayerToServer("sendplayertoserver", null, proxy)

        println(proxy.allServers.forEach {
            logger.info("Server: ${it.serverInfo.name}")
        })

        commandManager.register(hubCommand.commandMeta(), hubCommand)
        commandManager.register(serverBroadcast.commandMeta(), serverBroadcast)
        commandManager.register(configReload.commandMeta(), configReload)
        commandManager.register(sendPlayerToServer.commandMeta(), sendPlayerToServer)

        Config.loadConfigAsync()

    }

    @Subscribe
    fun onProxyInitialization(event: ProxyShutdownEvent) {
        logger.info("SoulzProxy is shutting down!")
    }

}
