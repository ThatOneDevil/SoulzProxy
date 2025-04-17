package me.thatonedevil.soulzProxy.linking.database

import com.velocitypowered.api.proxy.Player
import me.thatonedevil.soulzProxy.linking.LinkingData
import java.util.*

object DataManager {

    fun init() {
        Database.getConnection()?.use { conn ->
            LinkingDataRepository.createTable(conn)
            LinkingCodeRepository.createTable(conn)
        }
    }

    fun storeLinkingCode(code: String, uuid: UUID) = LinkingCodeRepository.store(code, uuid)

    fun getUUIDFromCode(code: String) = LinkingCodeRepository.getUUID(code)

    fun removeLinkingCode(code: String) = LinkingCodeRepository.remove(code)

    fun savePlayerData(data: LinkingData) = LinkingDataRepository.save(data)

    fun deletePlayerData(uuid: UUID) = LinkingDataRepository.delete(uuid)

    fun loadPlayerData(uuid: UUID) = LinkingDataRepository.load(uuid)

    fun getUUIDFromDiscordId(discordId: String) = LinkingDataRepository.getUUIDFromDiscord(discordId)

    fun clearAllData() = LinkingDataRepository.clearAll()

    fun getPlayerData(player: Player): LinkingData =
        loadPlayerData(player.uniqueId) ?: LinkingData(player.uniqueId, player.username)

    fun isLinked(id: String) = LinkingDataRepository.isLinked(id)
}
