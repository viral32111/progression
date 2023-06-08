package com.viral32111.progression

import net.fabricmc.api.DedicatedServerModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress( "UNUSED" )
class Server: DedicatedServerModInitializer {
	companion object {
		val LOGGER: Logger = LoggerFactory.getLogger( "com.viral32111.progression" )
	}

	override fun onInitializeServer() {
		LOGGER.info( "Progression initialized on the server." )
	}
}
