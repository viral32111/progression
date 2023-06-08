package com.viral32111.progression.config

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Configuration(
	@Required @SerialName( "experienceThresholds" ) val experienceThresholds: ExperienceThresholds = ExperienceThresholds(),
	@Required @SerialName( "chatMessages" ) val chatMessages: ChatMessages = ChatMessages(),
	@Required @SerialName( "bossBarTitles" ) val bossBarTitles: BossBarTitles = BossBarTitles()
)
