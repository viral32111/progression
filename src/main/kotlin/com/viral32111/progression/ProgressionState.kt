package com.viral32111.progression

import com.viral32111.progression.Progression.Companion.LOGGER
import com.viral32111.progression.Progression.Companion.MOD_ID
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.server.MinecraftServer
import net.minecraft.world.PersistentState
import java.util.UUID

// https://www.fabricmc.net/wiki/tutorial:persistent_states

class ProgressionState : PersistentState() {
	companion object {
		private const val KEY_EXPERIENCE_COUNTER = "experienceCounter"
		private const val KEY_PLAYERS_HIDING_BOSS_BAR = "playersHidingProgressBar"

		fun getProgressionState( server: MinecraftServer ): ProgressionState? {
			val persistentStateManager = server.overworld?.persistentStateManager
			return persistentStateManager?.getOrCreate( ::createFromNbt, { ProgressionState() }, MOD_ID )
		}

		private fun createFromNbt( nbt: NbtCompound ): ProgressionState {
			val progressionState = ProgressionState()

			progressionState.experienceCounter = nbt.getInt( KEY_EXPERIENCE_COUNTER )

			val playersHidingProgressBarList = nbt.getList( KEY_PLAYERS_HIDING_BOSS_BAR, NbtElement.STRING_TYPE.toInt() )
			for ( index: Int in 0 until playersHidingProgressBarList.size ) {
				val uuidAsString = playersHidingProgressBarList.getString( index )
				if ( uuidAsString.isEmpty() ) {
					LOGGER.warn( "Skipping empty player UUID '${ uuidAsString }' from players hiding progress bar list!" )
				} else {
					val playerUUID = UUID.fromString( playersHidingProgressBarList.getString( index ) )
					progressionState.playersHidingProgressBar.add( playerUUID )
					LOGGER.info( "Progress bar will be hidden for player with UUID '${ playerUUID }'." )
				}
			}

			LOGGER.info( "Created persistent state." )

			return progressionState
		}
	}

	var experienceCounter = 0
	var playersHidingProgressBar = HashSet<UUID>()

	override fun writeNbt( nbt: NbtCompound ): NbtCompound {
		nbt.putInt( KEY_EXPERIENCE_COUNTER, experienceCounter )

		val playersHidingBossBarList = NbtList()
		for ( uuid in playersHidingProgressBar ) playersHidingBossBarList.add( NbtString.of( uuid.toString() ) )
		nbt.put( KEY_PLAYERS_HIDING_BOSS_BAR, playersHidingBossBarList )

		LOGGER.info( "Saved persistent state." )

		return nbt
	}
}
