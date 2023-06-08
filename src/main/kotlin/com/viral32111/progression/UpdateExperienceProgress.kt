package com.viral32111.progression

import com.viral32111.progression.Progression.Companion.LOGGER
import com.viral32111.progression.Progression.Companion.state
import com.viral32111.progression.Progression.Companion.updateBossBar
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResult

fun updateExperienceProgress( player: PlayerEntity, experience: Int ): ActionResult {
	state.experienceCounter += experience
	state.markDirty()

	LOGGER.info( "Incremented global experience counter to ${ state.experienceCounter } due to player '${ player.displayName.string }' (${ player.uuidAsString }) gaining $experience experience." )

	updateBossBar()

	return ActionResult.PASS
}
