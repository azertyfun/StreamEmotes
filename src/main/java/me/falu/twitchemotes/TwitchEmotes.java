package me.falu.twitchemotes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.falu.twitchemotes.emote.Emote;
import me.falu.twitchemotes.emote.provider.*;
import net.fabricmc.api.ClientModInitializer;
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
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class TwitchEmotes implements ClientModInitializer {
    public static final ModContainer MOD_CONTAINER = FabricLoader.getInstance().getModContainer("twitchemotes").orElseThrow(RuntimeException::new);
    public static final String MOD_NAME = MOD_CONTAINER.getMetadata().getName();
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);
    public static final String MOD_VERSION = String.valueOf(MOD_CONTAINER.getMetadata().getVersion());
    public static final float EMOTE_SIZE = 9.0F;
    public static final Queue<Emote.DrawData> SCHEDULED_DRAW = new ArrayDeque<>();
    private static final EmoteProvider[] EMOTE_PROVIDERS = new EmoteProvider[] {
            new BTTVEmoteProvider(),
            new FFZEmoteProvider(),
            new STVEmoteProvider(),
            new TwitchProxyEmoteProvider()
    };
    private static final Map<String, Emote> EMOTE_MAP = new HashMap<>();

    public static void log(Object msg) {
        LOGGER.log(Level.INFO, msg);
    }

    private static boolean validStrings(String... strings) {
        for (String string : strings) {
            if (string == null || string.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static Emote getEmote(String name, Map<String, Emote> specific) {
        if (EMOTE_MAP.containsKey(name)) {
            return EMOTE_MAP.get(name);
        }
        return specific.get(name);
    }

    public static Set<String> getEmoteKeys() {
        return EMOTE_MAP.keySet();
    }

    public static void invalidateEmote(Emote emote) {
        if (emote instanceof Emote) {
            EMOTE_MAP.remove(emote.name);
        }
    }

    public static void reloadEmotes() {
        EMOTE_MAP.clear();
        for (EmoteProvider provider : EMOTE_PROVIDERS) {
            List<Emote> emotes = provider.collectEmotes();
            for (Emote emote : emotes) {
                EMOTE_MAP.put(emote.name, emote);
            }
            log("Finished loading " + emotes.size() + " emotes from " + provider.getProviderName() + ".");
        }
    }

    public static void reload() {
        reloadEmotes();
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
        reload();
    }
}
