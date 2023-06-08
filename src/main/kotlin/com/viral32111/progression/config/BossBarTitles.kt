package com.viral32111.progression.config

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BossBarTitles(
	@Required @SerialName( "nether" ) val nether: String = "Unlock The Nether",
	@Required @SerialName( "end" ) val end: String = "Unlock The End"
)
