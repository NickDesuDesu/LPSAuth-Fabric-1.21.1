package net.nickxd.lpsauth.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.nickxd.lpsauth.LPSAuth;
import net.nickxd.lpsauth.utils.IEntityDataSaver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Entity.class)
public abstract class EntityDataSaverMixin implements IEntityDataSaver {
    private NbtCompound persistentData;
    private static final String LOCATION_NBT_KEY = String.format("%s.last_known_location", LPSAuth.MOD_ID);

    @Override
    public NbtCompound getPersistentData() {
        if (this.persistentData == null) {
            this.persistentData = new NbtCompound();
        }

        return persistentData;
    }

    @Inject(method = "writeNbt", at = @At("HEAD"))
    protected void writeMethod(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        if(persistentData != null) {
            nbt.put(LOCATION_NBT_KEY, persistentData);
        }
    }

    @Inject(method = "readNbt", at = @At("HEAD"))
    protected void readMethod(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains(LOCATION_NBT_KEY)) {
            persistentData = nbt.getCompound(LOCATION_NBT_KEY);
        }
    }

}
