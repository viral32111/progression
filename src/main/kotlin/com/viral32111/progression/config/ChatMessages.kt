package com.viral32111.progression.config

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessages(
	@Required @SerialName( "nether" ) val nether: String = "The Nether is not unlocked yet, tell players to gain more experience!",
	@Required @SerialName( "end" ) val end: String = "The End is not unlocked yet, tell players to gain more experience!"
)
