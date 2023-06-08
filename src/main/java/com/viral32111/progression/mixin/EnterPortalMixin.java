package com.viral32111.progression.mixin;

import com.viral32111.progression.callback.EnterPortalCallback;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin( ServerPlayerEntity.class )
public abstract class EnterPortalMixin {

	@Inject( method = "moveToWorld", at = @At( value = "HEAD" ), cancellable = true )
	public void enterPortal( ServerWorld destinationWorld, CallbackInfoReturnable<Entity> callbackInfo ) {
		ServerPlayerEntity player = ( ServerPlayerEntity ) ( Object ) this;

		ActionResult actionResult = EnterPortalCallback.Companion.getEVENT().invoker().interact( player, destinationWorld );

		if ( actionResult == ActionResult.FAIL ) {
			callbackInfo.setReturnValue( player );
		}
	}

}
