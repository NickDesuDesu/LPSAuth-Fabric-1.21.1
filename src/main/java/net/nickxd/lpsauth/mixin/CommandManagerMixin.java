package net.nickxd.lpsauth.mixin;

import com.mojang.brigadier.ParseResults;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.nickxd.lpsauth.managers.EventManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(CommandManager.class)
public class CommandManagerMixin {
    private static final Set<String> ALLOWED_COMMANDS = new HashSet<String>(Set.of(
            "help",
            "login",
            "register",
            "verify"
    ));

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private void onExecute(ParseResults<ServerCommandSource> parseResults, String command, CallbackInfo ci){
        ServerCommandSource source = parseResults.getContext().getSource();
        Entity entity = source.getEntity();

        if (entity instanceof ServerPlayerEntity player) {
            if (!EventManager.isLoggedIn(player)) {
                String baseCommand = command.split(" ")[0].toLowerCase();

                if (!ALLOWED_COMMANDS.contains(baseCommand)) {
                    player.sendMessage(Text.literal("🚫 You must log in before using that command. Do /login <password>"), true);
                    ci.cancel();
                }
            }
        }

        
    }
}