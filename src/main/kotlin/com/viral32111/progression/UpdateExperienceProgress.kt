package com.viral32111.progression

import com.viral32111.progression.Progression.Companion.LOGGER
import com.viral32111.progression.Progression.Companion.bossBar
import com.viral32111.progression.Progression.Companion.configuration
import com.viral32111.progression.Progression.Companion.isEndUnlocked
import com.viral32111.progression.Progression.Companion.isNetherUnlocked
import com.viral32111.progression.Progression.Companion.state
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.ActionResult

fun updateExperienceProgress( player: PlayerEntity, experience: Int ): ActionResult {
	state.experienceCounter += experience
	state.markDirty()

	LOGGER.info( "Incremented global experience counter to ${ state.experienceCounter } due to player '${ player.displayName }' (${ player.uuidAsString }) gaining $experience experience." )

	if ( !isNetherUnlocked() ) {
		bossBar.name = Text.of( configuration.bossBarTitles.nether )
		bossBar.isVisible = state.experienceCounter < configuration.experienceThresholds.nether
		bossBar.percent = state.experienceCounter.toFloat() / configuration.experienceThresholds.nether
	} else if ( isNetherUnlocked() && !isEndUnlocked() ) {
		bossBar.name = Text.of( configuration.bossBarTitles.end )
		bossBar.isVisible = state.experienceCounter < configuration.experienceThresholds.end
		bossBar.percent = state.experienceCounter.toFloat() / configuration.experienceThresholds.end
	} else {
		bossBar.isVisible = false
	}

	return ActionResult.PASS
}
