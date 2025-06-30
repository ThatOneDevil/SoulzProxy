package me.thatonedevil.soulzProxy

import com.google.inject.Inject
import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import me.thatonedevil.soulzProxy.commands.*
import me.thatonedevil.soulzProxy.linking.LinkClaimCommand
import me.thatonedevil.soulzProxy.linking.LinkCommand
import me.thatonedevil.soulzProxy.linking.database.DataManager
import me.thatonedevil.soulzProxy.linking.database.Database
import me.thatonedevil.soulzProxy.utils.Config
import me.thatonedevil.soulzProxy.utils.Config.getMessage
import org.slf4j.Logger
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.TimeUnit


@Plugin(
    id = "soulzproxy",
    name = "SoulzProxy",
    version = BuildConstants.VERSION,
    url = "https://thatonedevil.github.io/",
    authors = ["ThatOneDevil"],
    dependencies = [
        Dependency(id = "luckperms"),
        Dependency(id = "redisbungee"),
    ]
)


class SoulzProxy @Inject constructor(var logger: Logger, private var proxy: ProxyServer, @DataDirectory val dataDirectory: Path) {

    companion object {
        lateinit var instance: SoulzProxy
            private set
        lateinit var redisBungeeAPI: RedisBungeeAPI
            private set
        var secondProxy: Boolean = false
        lateinit var playerList: Set<UUID>
    }

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        instance = this
        logger.info("SoulzProxy is now running!")

        Config.loadConfigAsync().thenRun {
            val token = getMessage("token")
            if (token.isEmpty()) {
                logger.error("Missing bot token in config.yml")
                this.proxy.shutdown()
            }
            secondProxy = getMessage("secondProxy").toBoolean()
            JdaManager.init(token, proxy)
            DataManager.init()
            redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi()

            if (!secondProxy) {
                proxy.scheduler.buildTask(this, Runnable {
                    JdaManager.updateChannelTopic()
                    playerList = redisBungeeAPI.playersOnline
                }).repeat(10, TimeUnit.SECONDS).schedule()
            }
        }

        val commandManager = proxy.commandManager
        val hubCommand = Hub("hub", null, proxy)
        val serverBroadcast = ServerBroadcast("serverBroadcast", "sb", proxy)
        val configReload = ConfigReload("configReload", null, proxy)
        val send = Send("send", null, proxy)
        val proxyInfo = ProxyInfo("proxyInfo", null, proxy)
        val linkCommand = LinkCommand("link", null, proxy)
        val linkClaim = LinkClaimCommand("linkClaim", null, proxy)

        if (!secondProxy){
            commandManager.register(linkCommand.commandMeta(), linkCommand)
        }

        commandManager.register(hubCommand.commandMeta(), hubCommand)
        commandManager.register(serverBroadcast.commandMeta(), serverBroadcast)
        commandManager.register(configReload.commandMeta(), configReload)
        commandManager.register(send.commandMeta(), send)
        commandManager.register(proxyInfo.commandMeta(), proxyInfo)
        commandManager.register(linkClaim.commandMeta(), linkClaim)

        println(proxy.allServers.forEach {
            logger.info("Server: ${it.serverInfo.name}")
        })

    }

    @Subscribe
    fun onProxyInitialization(event: ProxyShutdownEvent) {
        logger.info("SoulzProxy is shutting down!")

        JdaManager.shutdown()
        Database.closeConnection()
    }

}
