package eu.e4b4.streamemotes.emote.texture;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;

public class EmoteBackedTexture extends NativeImageBackedTexture {
    public final long duration;

    public EmoteBackedTexture(NativeImage image) {
        this(image, -1L);
    }

    public EmoteBackedTexture(NativeImage image, long duration) {
        super(image);
        this.duration = duration;
    }
}
