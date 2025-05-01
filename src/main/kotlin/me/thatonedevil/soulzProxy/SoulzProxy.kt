package me.thatonedevil.soulzProxy

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import me.thatonedevil.soulzProxy.commands.*
import me.thatonedevil.soulzProxy.events.PlayerConnectionEvents
import me.thatonedevil.soulzProxy.linking.database.DataManager
import me.thatonedevil.soulzProxy.linking.LinkClaimCommand
import me.thatonedevil.soulzProxy.linking.LinkCommand
import me.thatonedevil.soulzProxy.linking.database.Database
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


class SoulzProxy @Inject constructor(var logger: Logger, private var proxy: ProxyServer, @DataDirectory val dataDirectory: Path) {

    companion object {
        lateinit var instance: SoulzProxy
    }

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        instance = this
        logger.info("SoulzProxy is now running!")

        val commandManager = proxy.commandManager
        val hubCommand = Hub("hub", null, proxy)
        val serverBroadcast = ServerBroadcast("serverBroadcast", "sb", proxy)
        val configReload = ConfigReload("configReload", null, proxy)
        val send = Send("send", null, proxy)
        val proxyInfo = ProxyInfo("proxyInfo", null, proxy)
        val linkCommand = LinkCommand("link", null, proxy)
        val linkClaim = LinkClaimCommand("linkClaim", null, proxy)

        println(proxy.allServers.forEach {
            logger.info("Server: ${it.serverInfo.name}")
        })

        proxy.eventManager.register(this, PlayerConnectionEvents(proxy));

        commandManager.register(hubCommand.commandMeta(), hubCommand)
        commandManager.register(serverBroadcast.commandMeta(), serverBroadcast)
        commandManager.register(configReload.commandMeta(), configReload)
        commandManager.register(send.commandMeta(), send)
        commandManager.register(proxyInfo.commandMeta(), proxyInfo)
        commandManager.register(linkCommand.commandMeta(), linkCommand)
        commandManager.register(linkClaim.commandMeta(), linkClaim)

        Config.loadConfigAsync().thenRun {
            val token = Config.getMessage("token")
            if (token.isEmpty()) {
                logger.error("Missing bot token in config.yml")
                this.proxy.shutdown()
            }

            JdaManager.init(token, proxy)
            DataManager.init()
        }
    }

    @Subscribe
    fun onProxyInitialization(event: ProxyShutdownEvent) {
        logger.info("SoulzProxy is shutting down!")

        JdaManager.shutdown()
        Database.closeConnection()
    }

}
