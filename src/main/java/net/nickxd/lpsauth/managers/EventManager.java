package net.nickxd.lpsauth.managers;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.nickxd.lpsauth.records.PlayerPosition;
import net.nickxd.lpsauth.utils.HttpUtil;
import net.nickxd.lpsauth.utils.IEntityDataSaver;
import net.nickxd.lpsauth.utils.PlayerLocationData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static net.nickxd.lpsauth.LPSAuth.MOD_ID;

public class EventManager {
    public static final Map<ServerPlayerEntity, List<Boolean>> PLAYER_LIST = new HashMap<>();
    public static final Map<UUID, NbtList> SERVER_INVENTORY_COPY = new HashMap<>();
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


    private static Integer ticks = 0;

    public static void loginPlayer(ServerPlayerEntity player) {
        PlayerPosition playerPosition = PlayerLocationData.getData((IEntityDataSaver) player);
        List<Double> xyz = playerPosition.position();
        List<Float> rot = playerPosition.rotation();
        player.teleport(player.getServerWorld(), xyz.get(0), xyz.get(1), xyz.get(2), rot.get(0), rot.get(1));
        player.sendMessage(Text.literal("Welcome back " + player.getName().getString() + "!"), true);

        PLAYER_LIST.get(player).set(1, true);
    }
    public static void register() {
        ServerPlayConnectionEvents.JOIN.register(EventManager::onPlayerJoin);
        ServerPlayConnectionEvents.DISCONNECT.register(EventManager::onPlayerDisconnect);
        ServerTickEvents.END_SERVER_TICK.register(EventManager::onNotLoggedInPlayersTick);
        AttackEntityCallback.EVENT.register(EventManager::onEntityAttack);
        AttackBlockCallback.EVENT.register(EventManager::onBlockAttack);
        UseBlockCallback.EVENT.register(EventManager::onUseBlock);
        UseEntityCallback.EVENT.register(EventManager::onEntityAttack);
        UseItemCallback.EVENT.register(EventManager::onUseItem);
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register(EventManager::onPlayerChat);
    }

    /**
     * Saves player details on join.
     * @param handler
     * @param sender
     * @param server
     */
    private static void onPlayerJoin(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        ServerPlayerEntity player = handler.getPlayer();
        PLAYER_LIST.put(player, Arrays.asList(false, false));

        PlayerPosition playerPosition = new PlayerPosition(
                Arrays.asList(player.prevX, player.prevY, player.prevZ),
                Arrays.asList(player.getYaw(), player.getPitch()));

        PlayerLocationData.addData(playerPosition, (IEntityDataSaver) player);

        NbtList nbt = player.getInventory().writeNbt(new NbtList());
        SERVER_INVENTORY_COPY.put(player.getUuid(), nbt);

        LOGGER.info(player.getName().getString() + "Added to NOT_LOGGED_IN");
    }

    private static void onPlayerDisconnect(ServerPlayNetworkHandler serverPlayNetworkHandler, MinecraftServer server) {
        ServerPlayerEntity player = serverPlayNetworkHandler.getPlayer();
        if (isLoggedIn(player)) {
            PlayerPosition playerPosition = new PlayerPosition(
                    Arrays.asList(player.prevX, player.prevY, player.prevZ),
                    Arrays.asList(player.getYaw(), player.getPitch()));

            PlayerLocationData.updateData(playerPosition, (IEntityDataSaver) player);
        }

        PLAYER_LIST.remove(player);

    }

    /**
     * Cancels player and camera movement; and inventory changes.
     * @param server
     */
    private static void onNotLoggedInPlayersTick(MinecraftServer server) {
        if (ticks > 99) ticks = 0;

        for (Map.Entry<ServerPlayerEntity, List<Boolean>> entry: PLAYER_LIST.entrySet()) {
            ServerPlayerEntity player = entry.getKey();
            Boolean isLoggedIn = entry.getValue().get(0);
            Boolean loginPlayerOnce = entry.getValue().get(1);

            if (isLoggedIn) {
                if (!loginPlayerOnce) {
                    loginPlayer(player);
                }

                continue;
            }

            PlayerPosition playerPosition = PlayerLocationData.getData((IEntityDataSaver) player);
            List<Double> xyz = playerPosition.position();
            List<Float> rot = playerPosition.rotation();

            player.teleport(player.getServerWorld(), xyz.get(0), 10000, xyz.get(2), rot.get(0), rot.get(1));
            player.setVelocity(Vec3d.ZERO);
            player.velocityModified = true;

            NbtList savedInventory = SERVER_INVENTORY_COPY.get(player.getUuid());
            if (savedInventory != null) {
                player.getInventory().readNbt(savedInventory);
                player.currentScreenHandler.syncState();
            }

            if (ticks%50 == 0) HttpUtil.checkAccountRegistration(player);
        }

        ticks++;
    }

    /**
     * Cancelling Attacks on entity
     * @param player
     * @param world
     * @param hand
     * @param entity
     * @param entityHitResult
     * @return
     */
    private static ActionResult onEntityAttack(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult entityHitResult) {
        if (!isLoggedIn((ServerPlayerEntity) player)) {
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }

    /**
     * Cancelling attacks on blocks. (Breaking blocks/Left click)
     * @param player
     * @param world
     * @param hand
     * @param pos
     * @param direction
     * @return
     */
    private static ActionResult onBlockAttack(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) {
        if (!isLoggedIn((ServerPlayerEntity) player)) {
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }


    /**
     * Cancelling placing blocks. (Right Click)
     * @param player
     * @param world
     * @param hand
     * @param hitResult
     * @return
     */
    private static ActionResult onUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (!isLoggedIn((ServerPlayerEntity) player)) {
            ((ServerPlayerEntity) player).networkHandler.sendPacket(
                    new ScreenHandlerSlotUpdateS2CPacket(
                            -2, // -2 = player's inventory (not a screen)
                            0,
                            player.getInventory().selectedSlot,
                            player.getInventory().getStack(player.getInventory().selectedSlot)
                    )
            );
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }

    /**
     * Cancels interactions with entity. (Right click)
     * @param player
     * @param world
     * @param hand
     * @param entity
     * @param entityHitResult
     * @return
     */
    private static ActionResult onUseEntity(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult entityHitResult) {
        if (!isLoggedIn((ServerPlayerEntity) player)) {
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }

    /**
     *  Cancel usage of items. (Right click with items)
     * @param player
     * @param world
     * @param hand
     * @return
     */
    private static TypedActionResult onUseItem(PlayerEntity player, World world, Hand hand) {
        if (!isLoggedIn((ServerPlayerEntity) player)) {

            ((ServerPlayerEntity) player).networkHandler.sendPacket(
                    new ScreenHandlerSlotUpdateS2CPacket(
                            -2,
                            0,
                            player.getInventory().selectedSlot,
                            player.getInventory().getStack(player.getInventory().selectedSlot)
                    )
            );

            return TypedActionResult.fail(ItemStack.EMPTY);
        }
        return TypedActionResult.pass(ItemStack.EMPTY);
    }

    /**
     * Cancels player chats.
     * @param message
     * @param sender
     * @param params
     * @return
     */
    private static Boolean onPlayerChat(SignedMessage message, ServerPlayerEntity sender, MessageType.Parameters params) {
        if (!isLoggedIn(sender)) {
            sender.sendMessage(Text.literal("⛔ You must log in before chatting. Do /login <password>"), true);
            return false;
        }

        return true;
    }

    /**
     * Checks whether the player given is not logged in.
     * @param player
     * @return
     */
    public static boolean isLoggedIn(ServerPlayerEntity player) {
        try {
            return PLAYER_LIST.get(player).get(0);
        } catch (Exception e) {
            return false;
        }

    }

}