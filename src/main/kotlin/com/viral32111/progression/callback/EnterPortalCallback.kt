package com.viral32111.progression.callback

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult

fun interface EnterPortalCallback {
	companion object {
		val EVENT: Event<EnterPortalCallback> = EventFactory.createArrayBacked( EnterPortalCallback::class.java ) { listeners ->
			EnterPortalCallback { player, destinationWorld ->
				for ( listener in listeners ) {
					val result = listener.interact( player, destinationWorld )
					if ( result != ActionResult.PASS ) return@EnterPortalCallback result
				}

				return@EnterPortalCallback ActionResult.PASS
			}
		}
	}

	fun interact( player: ServerPlayerEntity, destinationWorld: ServerWorld ): ActionResult
}
