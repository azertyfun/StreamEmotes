package eu.e4b4.streamemotes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.e4b4.streamemotes.emote.Emote;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import webpdecoderjn.WebPDecoder;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class StreamEmotes implements ClientModInitializer {
    public static final ModContainer MOD_CONTAINER = FabricLoader.getInstance().getModContainer("streamemotes").orElseThrow(RuntimeException::new);
    public static final String MOD_NAME = MOD_CONTAINER.getMetadata().getName();
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);
    public static final String MOD_VERSION = String.valueOf(MOD_CONTAINER.getMetadata().getVersion());
    public static final float EMOTE_SIZE = 9.0F;
    public static final Queue<Emote.DrawData> SCHEDULED_DRAW = new ArrayDeque<>();

    public static final Map<String, Emote> EMOTE_MAP = new HashMap<>();
    public static final Map<UUID, Map<String, Boolean>> USER_EMOTE_MAP = new HashMap<>();

    public static void log(Object msg) {
        LOGGER.log(Level.INFO, msg);
    }

    private static JsonElement getJsonResponse(String endpoint) throws IOException {
        URL url = URI.create(endpoint).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        InputStream inputStream = connection.getInputStream();
        String result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        return JsonParser.parseString(result);
    }

    private static Map<String, Boolean> fetchPlayerEmotes(UUID playerUUID) {
        try {
            HashMap<String, Boolean> response = new HashMap<>();
            LOGGER.info("Fetching player emotes for {}", playerUUID);
            getJsonResponse("https://stream-emotes.e4b4.eu/v1/emotes/" + playerUUID.toString()).getAsJsonArray().forEach(e -> {
                JsonObject el = e.getAsJsonObject();
                String emoteName = el.get("name").getAsString();

                // If no-one else has this emote, create it
                if (!EMOTE_MAP.containsKey(emoteName)) {
                    Emote emote = new Emote(emoteName, el.get("id").getAsString(), el.get("url").getAsString(), el.get("animated").getAsBoolean() ? Emote.ImageType.GIF : Emote.ImageType.STATIC);
                    EMOTE_MAP.put(emoteName, emote);
                }

                response.put(emoteName, true);
            });
            return response;
        } catch (IOException e) {
            LOGGER.error("Failed getting emotes for {}: {}", playerUUID, e);
            return new HashMap<>();
        }
    }

    public static Emote getEmote(String name, UUID playerUUID) {
        if (!USER_EMOTE_MAP.containsKey(playerUUID)) {
            // This is an unknown player -> get all emotes.
            // See rationale in onInitializeClient() below.
            USER_EMOTE_MAP.put(playerUUID, fetchPlayerEmotes(playerUUID));
        }

        if (USER_EMOTE_MAP.get(playerUUID).containsKey(name)) {
            return EMOTE_MAP.get(name);
        }

        return null;
    }

    public static Set<String> getEmoteKeys() {
        return EMOTE_MAP.keySet();
    }

    public static void invalidateEmote(Emote emote) {
        if (emote instanceof Emote) {
            EMOTE_MAP.remove(emote.name);
        }
    }

    @Override
    public void onInitializeClient() {
        log("Using " + MOD_NAME + " v" + MOD_VERSION);
        try {
            WebPDecoder.init();
            log("WebP decoder test: " + WebPDecoder.test());
        } catch (IOException e) {
            LOGGER.error("Couldn't initialize WebP decoder", e);
        }

        /*
         * On player join, we need to start fetching that player's emotes. That way when they start talking in chat
         * they'll hopefully be ready
         *
         * We also lazy-load them when we receive the first chat message from a user; that way already connected players
         * also get emotes. For those we could listen on "server join" event and list all players, but on large servers
         * this may be an issue (not my use-case though).
         */

        // We joined the game
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            assert client.player != null;
            UUID ownUUID = client.player.getUuid();
            LOGGER.info("Fetching emotes for self ({})", ownUUID);
            synchronized (USER_EMOTE_MAP) {
                USER_EMOTE_MAP.put(ownUUID, fetchPlayerEmotes(ownUUID));
            }

        });

        // Someone joined the game
        // In single-player this also triggers when we join the game. But on the server I play on it doesn't because its
        // plugins hides the join message for oneself.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            LOGGER.info("User {} ({}) joined the game; loading all emotes.", handler.player.getName().getLiteralString(), handler.player.getUuid());
            synchronized (USER_EMOTE_MAP) {
                USER_EMOTE_MAP.put(handler.player.getUuid(), fetchPlayerEmotes(handler.player.getUuid()));
            }
        });
    }
}
