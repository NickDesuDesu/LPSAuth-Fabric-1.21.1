package net.nickxd.lpsauth.managers;
import net.minecraft.server.network.ServerPlayerEntity;
import net.nickxd.lpsauth.records.RegistrationInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class AuthManager {
    private static final Map<UUID, RegistrationInfo> REGISTRATION_MAP = new HashMap<>();

    public static String generateOtp(ServerPlayerEntity player, String discordIdentifier) {
        String otp = String.format("%05d", new Random().nextInt(100000));
        REGISTRATION_MAP.put(player.getUuid(), new RegistrationInfo(discordIdentifier, otp));
        return otp;
    }

    public static RegistrationInfo getRegistrationInfo(UUID uuid) {
        return REGISTRATION_MAP.get(uuid);
    }

    public static String getOtp(UUID uuid) {
        RegistrationInfo info = REGISTRATION_MAP.get(uuid);
        return info != null ? info.otp() : null;
    }

    public static String getDiscordIdentifier(UUID uuid) {
        RegistrationInfo info = REGISTRATION_MAP.get(uuid);
        return info != null ? info.discordIdentifier() : null;
    }

    public static void clear(UUID uuid) {
        REGISTRATION_MAP.remove(uuid);
    }

    public static boolean isPending(UUID uuid) {
        return REGISTRATION_MAP.containsKey(uuid);
    }
}
