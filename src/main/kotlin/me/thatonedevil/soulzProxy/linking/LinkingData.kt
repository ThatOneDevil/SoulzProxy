package me.thatonedevil.soulzProxy.linking


import java.util.*

data class LinkingData(
    val uuid: UUID,
    var name: String,
    var linked: Boolean = false,
    var userId: String = "0"
)
