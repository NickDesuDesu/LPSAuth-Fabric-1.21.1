package net.nickxd.lpsauth.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.nickxd.lpsauth.managers.AuthManager;
import net.nickxd.lpsauth.records.RegistrationInfo;
import net.nickxd.lpsauth.utils.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class VerifyCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger("lpsauth");

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("verify")
                .requires(source -> source.getEntity() instanceof ServerPlayerEntity)
                .then(argument("otp", StringArgumentType.word())
                        .executes(ctx -> verifyCommand(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "otp")
                        ))));
    }

    private static int verifyCommand(ServerCommandSource source, String otp) {
        ServerPlayerEntity player = source.getPlayer();


        try {
            RegistrationInfo registrationInfo = AuthManager.getRegistrationInfo(player.getUuid());

            HttpUtil.sendVerifyRequest(player, registrationInfo.discordIdentifier(), registrationInfo.otp().equals(otp));
            return 1;
        } catch (Exception e) {
            player.sendMessage(Text.literal("§a[Auth] You do not have an ongoing registration. Please do /register"), true);
            return 0;
        }
    }
}