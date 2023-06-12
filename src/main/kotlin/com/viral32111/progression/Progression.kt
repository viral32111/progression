package com.viral32111.progression

import com.mojang.brigadier.Command
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.viral32111.progression.callback.EnterPortalCallback
import com.viral32111.progression.callback.GainExperienceCallback
import com.viral32111.progression.config.Configuration
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.block.EndPortalFrameBlock
import net.minecraft.entity.boss.BossBar
import net.minecraft.entity.boss.ServerBossBar
import net.minecraft.item.Items
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.StandardOpenOption
import kotlin.io.path.createDirectory
import kotlin.io.path.notExists
import kotlin.io.path.readText
import kotlin.io.path.writeText

@Suppress( "UNUSED" )
class Progression: DedicatedServerModInitializer {
	companion object {
		const val MOD_ID = "progression"
		val LOGGER: Logger = LoggerFactory.getLogger( "com.viral32111.$MOD_ID" )

		@OptIn( ExperimentalSerializationApi::class )
		val JSON = Json {
			prettyPrint = true
			prettyPrintIndent = "\t"
			ignoreUnknownKeys = true
		}

		const val CONFIGURATION_DIRECTORY_NAME = "viral32111"
		const val CONFIGURATION_FILE_NAME = "$MOD_ID.json"

		var configuration = Configuration()
		var state = ProgressionState()

		val bossBar: ServerBossBar = ServerBossBar(
			Text.of( configuration.bossBarTitles.nether ), // Assume Nether is not unlocked yet
			BossBar.Color.GREEN,
			BossBar.Style.PROGRESS
		)

		private fun isNetherUnlocked() = state.experienceCounter >= configuration.experienceThresholds.nether
		private fun isEndUnlocked() = state.experienceCounter >= configuration.experienceThresholds.end

		fun updateBossBar() {
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
		}
	}

	override fun onInitializeServer() {
		LOGGER.info( "Progression initialized on the server." )

		configuration = loadConfigurationFile()

		registerCallbackListeners()
		registerCommands()
	}

	private fun loadConfigurationFile(): Configuration {
		val serverConfigurationDirectory = FabricLoader.getInstance().configDir
		val configurationDirectory = serverConfigurationDirectory.resolve( CONFIGURATION_DIRECTORY_NAME )
		val configurationFile = configurationDirectory.resolve( CONFIGURATION_FILE_NAME )

		if ( configurationDirectory.notExists() ) {
			configurationDirectory.createDirectory()
			LOGGER.info( "Created directory '${ configurationDirectory }' for configuration files." )
		}

		if ( configurationFile.notExists() ) {
			val configAsJSON = JSON.encodeToString( Configuration() )

			configurationFile.writeText( configAsJSON, options = arrayOf(
				StandardOpenOption.CREATE_NEW,
				StandardOpenOption.WRITE
			) )

			LOGGER.info( "Created configuration file '${ configurationFile }'." )
		}

		val configAsJSON = configurationFile.readText()
		val config = JSON.decodeFromString<Configuration>( configAsJSON )
		LOGGER.info( "Loaded configuration from file '${ configurationFile }'" )

		LOGGER.debug( "Experience Threshold (Nether): '${ config.experienceThresholds.nether }'" )
		LOGGER.debug( "Experience Threshold (End): '${ config.experienceThresholds.end }'" )
		LOGGER.debug( "Chat Message (Nether): '${ config.chatMessages.nether }'" )
		LOGGER.debug( "Chat Message (End): '${ config.chatMessages.end }'" )
		LOGGER.debug( "Boss Bar Title (Nether): '${ config.bossBarTitles.nether }'" )
		LOGGER.debug( "Boss Bar Title (End): '${ config.bossBarTitles.end }'" )

		return config
	}

	private fun registerCallbackListeners() {
		EnterPortalCallback.EVENT.register( ::blockPortalTravel )
		GainExperienceCallback.EVENT.register( ::updateExperienceProgress )

		// https://www.fabricmc.net/wiki/tutorial:event_index
		ServerLifecycleEvents.SERVER_STARTED.register { server ->
			state = ProgressionState.getProgressionState( server ) ?: throw RuntimeException( "Persistent state manager for Overworld is null" )
			LOGGER.info( "Acquired persistent state from server (experience counter: ${ state.experienceCounter }, players hiding progress bar: ${ state.playersHidingProgressBar.size })." )

			updateBossBar()
		}

		ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
			//val state = ProgressionState.getProgressionState( server )
			val player = handler.player

			if ( !state.playersHidingProgressBar.contains( player.uuid ) ) {
				bossBar.addPlayer( player )
			}
		}

		UseBlockCallback.EVENT.register { player, world, hand, hitResult ->
			val blockState = world.getBlockState( hitResult.blockPos )

			if ( !isEndUnlocked() && blockState.block is EndPortalFrameBlock && player.getStackInHand( hand ).item == Items.ENDER_EYE ) {
				player.sendMessage( Text.of( configuration.chatMessages.end ), false )
				LOGGER.info( "Preventing player '${ player.displayName.string }' (${ player.uuidAsString }) from using end portal frame as experience progress is only ${ state.experienceCounter } / ${ configuration.experienceThresholds.end }." )
				return@register ActionResult.FAIL
			}

			return@register ActionResult.PASS
		}

		LOGGER.info( "Registered callback listeners." );
	}

	private fun registerCommands() {
		// https://www.fabricmc.net/wiki/tutorial:commands
		CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
			dispatcher.register( literal( "toggleprogressbar" ).executes { context ->
				//val state = ProgressionState.getProgressionState( context.source.server )
				val player = context.source.player

				// This probably happens if the server console executes the command
				if ( player == null ) {
					LOGGER.error( "Player is null within command execution!?" )
					throw SimpleCommandExceptionType( Text.literal( "Something went horribly wrong while attempting to execute that command." ) ).create() // TODO: Should really be Text.translatable()
				}

				if ( state.playersHidingProgressBar.contains( player.uuid ) ) {
					state.playersHidingProgressBar.remove( player.uuid )
					bossBar.addPlayer( player )
					player.sendMessage( Text.literal( "Experience progression bar now visible." ) )
				} else {
					state.playersHidingProgressBar.add( player.uuid )
					bossBar.removePlayer( player )
					player.sendMessage( Text.literal( "Experience progression bar now hidden.") )
				}

				state.markDirty()

				return@executes Command.SINGLE_SUCCESS
			} )
		}

		LOGGER.info( "Registered commands." )
	}
}
