package me.thatonedevil.soulzProxy.events

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.PostLoginEvent
import com.velocitypowered.api.proxy.ProxyServer
import me.thatonedevil.soulzProxy.JdaManager.updateChannelTopic

class PlayerConnectionEvents(var proxy: ProxyServer) {

    @Subscribe
    fun onPlayerJoin(event: PostLoginEvent) {
        updateChannelTopic(proxy = proxy)
    }

    @Subscribe
    fun onPlayerLeave(event: DisconnectEvent) {
        updateChannelTopic(proxy = proxy)
    }
}