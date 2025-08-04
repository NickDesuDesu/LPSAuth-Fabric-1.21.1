package net.nickxd.lpsauth.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.nickxd.lpsauth.utils.HttpUtil;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class LoginCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("login")
                .requires(source -> source.getEntity() instanceof ServerPlayerEntity)
                .then(argument("password", StringArgumentType.word())
                        .executes(ctx -> loginCommand(ctx.getSource(), StringArgumentType.getString(ctx, "password")))));
    }

    private static int loginCommand(ServerCommandSource source, String password) {
        ServerPlayerEntity player = source.getPlayer();

        HttpUtil.sendLoginRequest(player, password);
        return 1;
    }
}