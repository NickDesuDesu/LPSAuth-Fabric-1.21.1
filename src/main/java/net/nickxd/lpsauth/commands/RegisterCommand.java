package net.nickxd.lpsauth.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.nickxd.lpsauth.managers.AuthManager;
import net.nickxd.lpsauth.utils.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class RegisterCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger("lpsauth");

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("register")
                .requires(source -> source.getEntity() instanceof ServerPlayerEntity)
                .then(argument("discord_identifier", StringArgumentType.word())
                        .then(argument("password", StringArgumentType.word())
                                .then(argument("confirmPassword", StringArgumentType.word())
                                        .executes(ctx -> registerCommand(
                                                ctx.getSource(),
                                                StringArgumentType.getString(ctx, "discord_identifier"),
                                                StringArgumentType.getString(ctx, "password"),
                                                StringArgumentType.getString(ctx, "confirmPassword")
                                        ))))));
    }

    private static int registerCommand(ServerCommandSource source, String discord_identifier, String password, String confirmPassword) {
        ServerPlayerEntity player = source.getPlayer();


        if (!password.equals(confirmPassword)) {
            player.sendMessage(Text.literal("❌ Invalid Password!"), true);
            return 0;
        }

        String otp = AuthManager.generateOtp(player, discord_identifier);
        HttpUtil.sendRegisterRequest(player, discord_identifier, password, otp);
        return 1;
    }
}