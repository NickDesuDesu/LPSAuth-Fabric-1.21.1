package net.nickxd.lpsauth.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.nickxd.lpsauth.managers.EventManager;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtil {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static String getResponse(HttpURLConnection con) {
        try {
            int status = con.getResponseCode();
            BufferedReader in;

            if (status >= 200 && status < 300) {
                in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }

            in.close();
            con.disconnect();

            return response.toString();
        } catch (Exception e) {
            return "Connection Error";
        }
    }

    public static void checkAccountRegistration(ServerPlayerEntity player) {
        new Thread(() -> {
            try {
                String username = player.getName().getString();
                String query = "http://localhost:8000/minecraft/user?minecraft_username=" + username;
                URL url = new URL(query);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);

                int status = con.getResponseCode();

                if (status == 200) {
                    player.sendMessage(Text.literal("§a[Auth] Please login using /login <password>"), true);
                } else if (status == 404) {
                    player.sendMessage(Text.literal("§e[Auth] Please register using /register <discord_identifier> <password> <confirm_password>"), true);
                } else {
                    player.sendMessage(Text.literal("§c[Auth] Something went horribly wrong. Please contact NickXD on discord. " + status), true);
                }

                con.disconnect();

            } catch (Exception e) {
                player.sendMessage(Text.literal("§a[Auth] Failed to contact auth server. Please Contact NickXD on discord."), true);
            }
        }).start();
    }
    public static void sendRegisterRequest(ServerPlayerEntity player, String discord_identifier, String password, String otp) {
        new Thread(() -> {
            try {
                String username = player.getName().getString();
                String query = "http://localhost:8000/minecraft/register?minecraft_username=" + username +
                        "&discord_identifier=" + discord_identifier +
                        "&password=" + password +
                        "&otp="+ otp;
                URL url = new URL(query);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);

                int status = con.getResponseCode();
                JsonObject json = JsonParser.parseString(getResponse(con)).getAsJsonObject();

                if (status == 202) {
                    player.sendMessage(Text.literal("§a[Auth] Please verify your account by doing /verify <otp sent to your discord account>"), true);
                } else if (status == 403) {
                    player.sendMessage(Text.literal("§e[Auth] " + json.get("error").getAsString()), true);
                } else {
                    player.sendMessage(Text.literal("§c[Auth] Something went horribly wrong. Please contact NickXD on discord. " + status), true);
                }

                con.disconnect();

            } catch (Exception e) {
                player.sendMessage(Text.literal("§a[Auth] Failed to contact auth server. Please Contact NickXD on discord."), true);
            }
        }).start();
    }

    public static void sendVerifyRequest(ServerPlayerEntity player, String discord_identifier, Boolean valid) {
        new Thread(() -> {
            try {
                String query = "http://localhost:8000/minecraft/verify?discord_identifier=" + discord_identifier +
                        "&otp_confirmed=" + valid.toString();
                URL url = new URL(query);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);

                int status = con.getResponseCode();
                JsonObject json = JsonParser.parseString(getResponse(con)).getAsJsonObject();

                if (status == 201) {
                    player.sendMessage(Text.literal("§a[Auth] Registration completed successfully. You can now login using /login <password>"), true);
                }else if (status == 403) {
                    player.sendMessage(Text.literal("§e[Auth] Invalid OTP"), true);
                }  else if (status == 404) {
                    player.sendMessage(Text.literal("§e[Auth] " + json.get("error").getAsString()), true);
                } else {
                    player.sendMessage(Text.literal("§c[Auth] Something went horribly wrong. Please contact NickXD on discord. " + status), true);
                }

                con.disconnect();

            } catch (Exception e) {
                player.sendMessage(Text.literal("§a[Auth] Failed to contact auth server. Please Contact NickXD on discord."), true);
            }
        }).start();
    }

    public static void sendLoginRequest(ServerPlayerEntity player, String password) {
        new Thread(() -> {
            try {
                String username = player.getName().getString();
                String query = "http://localhost:8000/minecraft/login?minecraft_username=" + username +
                        "&password=" + password;
                URL url = new URL(query);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);

                int status = con.getResponseCode();
                JsonObject json = JsonParser.parseString(getResponse(con)).getAsJsonObject();

                if (status == 200) {
                    EventManager.PLAYER_LIST.get(player).set(0, true);
                    player.sendMessage(Text.literal("§a[Auth] Login Successful."), true);
                    player.sendMessage(Text.literal("§a[Auth] Welcome back " + username + "!"), true);
                } else if (status >= 400) {
                    player.sendMessage(Text.literal("§e[Auth] " + json.get("error")), true);
                } else {
                    player.sendMessage(Text.literal("§c[Auth] Something went horribly wrong. Please contact NickXD on discord. " + status), true);
                }

                con.disconnect();

            } catch (Exception e) {
                player.sendMessage(Text.literal("§a[Auth] Failed to contact auth server. Please Contact NickXD on discord."), true);
            }
        }).start();
    }
}