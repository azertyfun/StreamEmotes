package me.falu.twitchemotes.emote.provider;

import com.google.gson.*;
import me.falu.twitchemotes.TwitchEmotes;
import me.falu.twitchemotes.emote.Emote;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TwitchProxyEmoteProvider extends EmoteProvider {
    private static final String BASE_URL = "http://localhost:8080/v1/emotes"; // TODO productize!

    @Override
    public String getProviderName() {
        return "Twitch (proxy)";
    }

    @Override
    public JsonArray getGlobalEmotes() {
        return new JsonArray();
    }

    @Override
    public JsonArray getUserEmotes(String userId) {
        try {
            URL url = new URL(BASE_URL + "/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            InputStream inputStream = connection.getInputStream();
            String result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            JsonElement response = JsonParser.parseString(result);

            TwitchEmotes.LOGGER.info("Got emotes -> " + response.toString());

            if (response.isJsonNull() || !response.isJsonArray()) {
                return new JsonArray();
            }
            return response.getAsJsonArray();
        } catch (IOException e) {
            TwitchEmotes.LOGGER.error("Error while making HTTP request", e);
        }
        return new JsonArray();
    }

    @Override
    public Emote createEmote(JsonObject data) {
        String id = data.get("id").getAsString();
        boolean isAnimated = data.get("animated").getAsBoolean();
        Emote.ImageType type = isAnimated ? Emote.ImageType.GIF : Emote.ImageType.STATIC;

        return Emote.builder()
                .name(data.get("name").getAsString())
                .id(id)
                .url(data.get("url").getAsString())
                .imageType(type)
                .build();
    }
}
