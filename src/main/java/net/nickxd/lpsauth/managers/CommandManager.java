package net.nickxd.lpsauth.managers;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.nickxd.lpsauth.commands.LoginCommand;
import net.nickxd.lpsauth.commands.RegisterCommand;
import net.nickxd.lpsauth.commands.VerifyCommand;

public class CommandManager {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LoginCommand.register(dispatcher);
            RegisterCommand.register(dispatcher);
            VerifyCommand.register(dispatcher);
        });
    }
}
