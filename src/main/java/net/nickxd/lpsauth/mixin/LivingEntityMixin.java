package net.nickxd.lpsauth.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.nickxd.lpsauth.managers.EventManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
	@Inject(
			method = "canTarget(Lnet/minecraft/entity/LivingEntity;)Z",
			at = @At("HEAD"),
			cancellable = true
	)
	private void onCanTarget(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
		if (target instanceof ServerPlayerEntity player && !EventManager.isLoggedIn(player)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "damage", at = @At("HEAD"), cancellable = true)
	private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		LivingEntity self = (LivingEntity)(Object)this;

		if ((self instanceof ServerPlayerEntity player) && !EventManager.isLoggedIn(player)) {
			cir.setReturnValue(false);
		}
	}


	@Inject(method = "tick", at = @At("HEAD"))
	private void resetAir(CallbackInfo ci) {
		if ((Object) this instanceof ServerPlayerEntity player) { // Safe cast check
			player.setAir(player.getMaxAir()); // Reset air to max
		}
	}
}