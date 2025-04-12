package me.thatonedevil.soulzProxy.linking

import com.velocitypowered.api.proxy.Player
import java.util.*

// this class is normally used for skript reflection
class LinkManager(player: Player) {

    private val data = DataManager.getPlayerData(player)

    var isLinked: Boolean
        get() = data.linked
        set(value) {
            data.linked = value
            save()
        }

    var userId: String
        get() = data.userId
        set(value) {
            data.userId = value
            save()
        }

    val uuid: UUID
        get() = data.uuid

    fun getData(): LinkingData = data

    fun save() {
        if (data.linked) {
            DataManager.savePlayerData(data)
        }
    }

    fun delete() {
        DataManager.deletePlayerData(data.uuid)
    }

}
