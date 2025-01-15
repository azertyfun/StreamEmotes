package eu.e4b4.streamemotes.emote.texture;

import eu.e4b4.streamemotes.StreamEmotes;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import eu.e4b4.streamemotes.emote.Emote;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@ToString
public class EmoteTextureHandler {
    private final Emote emote;
    private final List<EmoteBackedTexture> textures = new ArrayList<>();
    public boolean loading;
    public boolean failed;
    private int currentFrame = 0;
    private long lastAdvanceTime = 0L;

    public float getWidth() {
        if (this.textures.isEmpty()) {
            return StreamEmotes.EMOTE_SIZE;
        }
        NativeImageBackedTexture texture = this.textures.get(this.currentFrame);
        if (texture.getImage() == null) {
            return StreamEmotes.EMOTE_SIZE;
        }
        NativeImage img = texture.getImage();
        return (StreamEmotes.EMOTE_SIZE * img.getWidth()) / img.getHeight();
    }

    public NativeImage getImage() {
        if (this.textures.isEmpty() && !this.loading && !this.failed) {
            new Thread(() -> {
                List<EmoteBackedTexture> textures;
                URL url;
                try {
                    url = URI.create(this.emote.url.replace("http:", "https:")).toURL();
                } catch (MalformedURLException ignored) {
                    StreamEmotes.LOGGER.error("Invalid URL for emote '{}'.", this.emote.name);
                    StreamEmotes.invalidateEmote(this.emote);
                    return;
                }
                try {
                    TextureReader textureReader = new TextureReader(url, this.emote.imageType);
                    textures = new ArrayList<>(textureReader.read());
                } catch (IOException e) {
                    StreamEmotes.LOGGER.error("Error while reading image for '{}'", this.emote.name, e);
                    StreamEmotes.invalidateEmote(this.emote);
                    this.failed = true;
                    this.loading = false;
                    return;
                }
                this.textures.addAll(textures);
                this.loading = false;
            }).start();
            this.loading = true;
            return null;
        } else if (this.loading) {
            return null;
        }
        return this.textures.get(this.currentFrame).getImage();
    }

    public void postRender() {
        if (this.textures.size() <= 1) {
            return;
        }
        long now = Util.getMeasuringTimeMs();
        long duration = this.textures.get(this.currentFrame).duration;
        if (this.lastAdvanceTime > 0L) {
            long difference = now - this.lastAdvanceTime;
            if (difference >= duration) {
                this.currentFrame++;
                if (this.currentFrame >= this.textures.size()) {
                    this.currentFrame = 0;
                }
                this.lastAdvanceTime = now;
            }
        } else {
            this.lastAdvanceTime = now + duration;
        }
    }

    public int getGlId() {
        try {
            return this.textures.get(this.currentFrame).getGlId();
        } catch (IndexOutOfBoundsException ignored) {
            if (!this.loading) {
                StreamEmotes.LOGGER.error("Requested frame doesn't exist for emote '{}'.", this.emote.name);
                StreamEmotes.invalidateEmote(this.emote);
            }
        }
        return -1;
    }
}
