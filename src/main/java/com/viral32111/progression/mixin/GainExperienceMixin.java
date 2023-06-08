package com.viral32111.progression.mixin;

import com.viral32111.progression.callback.GainExperienceCallback;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin( PlayerEntity.class )
public class GainExperienceMixin {

	@Inject( method = "addExperience", at = @At( "HEAD" ) )
	public void gainExperience( int experience, CallbackInfo info ) {
		PlayerEntity player = ( PlayerEntity ) ( Object ) this;

		GainExperienceCallback.Companion.getEVENT().invoker().interact( player, experience );
	}

}
