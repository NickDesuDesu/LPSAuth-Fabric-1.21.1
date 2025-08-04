package net.nickxd.lpsauth.utils;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.nickxd.lpsauth.records.PlayerPosition;

import java.util.List;

public class PlayerLocationData {
    public static PlayerPosition getData(IEntityDataSaver player) {
        NbtCompound nbt = player.getPersistentData();

        NbtCompound comp = nbt.getCompound(((ServerPlayerEntity) player).getUuid().toString());

        List<Double> pos = List.of(comp.getDouble("x"), comp.getDouble("y"), comp.getDouble("z"));
        List<Float> rot = List.of(comp.getFloat("yaw"), comp.getFloat("pitch"));

        return new PlayerPosition(pos, rot);
    }

    public static void addData(PlayerPosition playerPosition, IEntityDataSaver player) {
        NbtCompound nbt = player.getPersistentData();
        NbtCompound comp = new NbtCompound();

        if (nbt.contains(((ServerPlayerEntity) player).getUuid().toString())) {
            return;
        }

        comp.putDouble("x", playerPosition.position().get(0));
        comp.putDouble("y", playerPosition.position().get(1));
        comp.putDouble("z", playerPosition.position().get(2));
        comp.putFloat("yaw", playerPosition.rotation().get(0));
        comp.putFloat("pitch", playerPosition.rotation().get(1));

        nbt.put(((ServerPlayerEntity)player).getUuid().toString(), comp);
    }

    public static void updateData(PlayerPosition playerPosition, IEntityDataSaver player) {
        NbtCompound nbt = player.getPersistentData();
        NbtCompound comp = new NbtCompound();

        comp.putDouble("x", playerPosition.position().get(0));
        comp.putDouble("y", playerPosition.position().get(1));
        comp.putDouble("z", playerPosition.position().get(2));
        comp.putFloat("yaw", playerPosition.rotation().get(0));
        comp.putFloat("pitch", playerPosition.rotation().get(1));

        nbt.put(((ServerPlayerEntity)player).getUuid().toString(), comp);
    }
}
