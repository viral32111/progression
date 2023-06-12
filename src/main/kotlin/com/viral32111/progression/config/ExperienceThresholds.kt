package com.viral32111.progression.config

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExperienceThresholds(
	@Required @SerialName( "nether" ) val nether: Int = 5000,
	@Required @SerialName( "end" ) val end: Int = 25000
)
