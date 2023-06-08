package com.viral32111.progression

import com.viral32111.progression.Progression.Companion.LOGGER
import com.viral32111.progression.Progression.Companion.configuration
import com.viral32111.progression.Progression.Companion.state
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.world.World

fun blockPortalTravel( player: ServerPlayerEntity, destinationWorld: ServerWorld ): ActionResult {
	val currentDimension = player.serverWorld.registryKey
	val destinationDimension = destinationWorld.registryKey

	if ( currentDimension != World.OVERWORLD ) return ActionResult.PASS

	if ( destinationDimension == World.NETHER && state.experienceCounter <= configuration.experienceThresholds.nether ) {
		player.sendMessage( Text.of( configuration.chatMessages.nether ), false )
		LOGGER.info( "Preventing player '${ player.displayName }' (${ player.uuidAsString }) from entering the nether as experience progress is only ${ state.experienceCounter } / ${ configuration.experienceThresholds.nether }." )
		return ActionResult.FAIL
	} else if ( destinationDimension == World.END && state.experienceCounter <= configuration.experienceThresholds.end ) {
		player.sendMessage( Text.of( configuration.chatMessages.end ), false )
		LOGGER.info( "Preventing player '${ player.displayName }' (${ player.uuidAsString }) from entering the end as experience progress is only ${ state.experienceCounter } / ${ configuration.experienceThresholds.end }." )
		return ActionResult.FAIL
	}

	return ActionResult.PASS
}
