package com.viral32111.progression.callback

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResult

fun interface GainExperienceCallback {
	companion object {
		val EVENT: Event<GainExperienceCallback> = EventFactory.createArrayBacked( GainExperienceCallback::class.java ) { listeners ->
			GainExperienceCallback { player, experience ->
				for ( listener in listeners ) {
					val result = listener.interact( player, experience )
					if ( result != ActionResult.PASS ) return@GainExperienceCallback result
				}

				return@GainExperienceCallback ActionResult.PASS
			}
		}
	}

	fun interact( player: PlayerEntity, experience: Int ): ActionResult
}
