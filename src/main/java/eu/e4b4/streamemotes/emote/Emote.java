package eu.e4b4.streamemotes.emote;

import com.mojang.blaze3d.systems.RenderSystem;
import eu.e4b4.streamemotes.StreamEmotes;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import eu.e4b4.streamemotes.emote.texture.EmoteTextureHandler;
import net.minecraft.client.render.*;
import net.minecraft.client.gl.ShaderProgramKeys;
import org.joml.Matrix4f;

@Builder
@RequiredArgsConstructor
@ToString
public class Emote {
    public final String name;
    public final String id;
    public final String url;
    public final ImageType imageType;
    public final EmoteTextureHandler textureHandler = new EmoteTextureHandler(this);

    public boolean scheduleDraw(float x, float y, Matrix4f matrix, float alpha) {
        if (this.textureHandler.getImage() != null || this.textureHandler.loading) {
            return StreamEmotes.SCHEDULED_DRAW.add(new DrawData(this, x, y, matrix, alpha));
        }
        return false;
    }

    public void draw(DrawData data) {
        this.createTextureBuffer(data.matrix, data.x, data.y - 1.0F, data.alpha);
        this.textureHandler.postRender();
    }

    public void createTextureBuffer(Matrix4f matrix, float x, float y, float alpha) {
        int glId = this.textureHandler.getGlId();
        if (glId == -1) {
            return;
        }
        RenderSystem.setShaderTexture(0, glId);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.enableBlend();
        float size = StreamEmotes.EMOTE_SIZE;
        float width = this.textureHandler.getWidth();
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder
                .vertex(matrix, x, y, 100.0F)
                .color(1.0F, 1.0F, 1.0F, alpha)
                .texture(0.0F, 0.0F);
        bufferBuilder
                .vertex(matrix, x, y + size, 100.0F)
                .color(1.0F, 1.0F, 1.0F, alpha)
                .texture(0.0F, 1.0F);
        bufferBuilder
                .vertex(matrix, x + width, y + size, 100.0F)
                .color(1.0F, 1.0F, 1.0F, alpha)
                .texture(1.0F, 1.0F);
        bufferBuilder
                .vertex(matrix, x + width, y, 100.0F)
                .color(1.0F, 1.0F, 1.0F, alpha)
                .texture(1.0F, 0.0F);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public enum ImageType {
        GIF("gif"),
        WEBP("webp"),
        STATIC("png");
        public final String suffix;

        ImageType(String suffix) {
            this.suffix = suffix;
        }
    }

    @ToString
    @RequiredArgsConstructor
    @SuppressWarnings("ClassCanBeRecord")
    public static class DrawData {
        public final Emote emote;
        public final float x;
        public final float y;
        public final Matrix4f matrix;
        public final float alpha;

        public void draw() {
            this.emote.draw(this);
        }
    }
}
