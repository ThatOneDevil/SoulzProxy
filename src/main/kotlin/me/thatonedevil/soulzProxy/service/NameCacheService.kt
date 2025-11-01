package me.thatonedevil.soulzProxy.service

import com.google.gson.JsonParser
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*

/**
 * This service is used to cache the names of players and their UUIDs.
 * It uses the Mojang API to query the name and UUID of a player.
 * The results are cached in memory for faster access.
 */
object NameCacheService {

    private const val MOJANG_PROFILE_ENDPOINT = "https://sessionserver.mojang.com/session/minecraft/profile"
    private const val MOJANG_UUID_ENDPOINT = "https://api.mojang.com/users/profiles/minecraft"

    private val cache = mutableMapOf<UUID, String>()

    private val httpClient: HttpClient = HttpClient.newHttpClient()

    private fun queryMojangNameByUUID(uuid: UUID): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$MOJANG_PROFILE_ENDPOINT/$uuid"))
            .GET()
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        val name = JsonParser.parseString(response.body()).asJsonObject.get("name").asString
        cache[uuid] = name
        return name
    }

    private fun queryMojangUUIDByName(name: String): UUID {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$MOJANG_UUID_ENDPOINT/$name"))
            .GET()
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        val uuid = UUID.fromString(JsonParser.parseString(response.body()).asJsonObject.get("id").asString)
        cache[uuid] = name
        return uuid
    }

    fun nameFromUUID(uuid: UUID): String {
        return cache[uuid] ?: queryMojangNameByUUID(uuid)
    }
}