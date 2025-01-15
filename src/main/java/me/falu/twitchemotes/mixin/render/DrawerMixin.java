package me.falu.twitchemotes.mixin.render;

import me.falu.twitchemotes.emote.Emote;
import me.falu.twitchemotes.emote.EmoteStyleOwner;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Style;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextRenderer.Drawer.class)
public class DrawerMixin {
    @Shadow float x;
    @Shadow float y;
    @Shadow @Final int color;
    @Shadow @Final private Matrix4f matrix;

    // Inject right before the glyph is added, then cancel the whole thing. All the rendering is done on our side for emotes (which have already been replaced by a "_" character).
    // After rendering the glyph all we need to do is increment `this.x`.
    @Inject(method = "accept", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", shift = At.Shift.BEFORE), cancellable = true)
    private void drawEmote(int i, Style style, int j, CallbackInfoReturnable<Boolean> cir) {
        Emote emote = ((EmoteStyleOwner) style).twitchemotes$getEmoteStyle();
        if (emote != null) {
            if (emote.scheduleDraw(this.x, this.y, this.matrix, ColorHelper.getAlphaFloat(this.color))) {
                this.x += emote.textureHandler.getWidth();
                cir.setReturnValue(true);
                cir.cancel();
                return;
            }
            ((EmoteStyleOwner) style).twitchemotes$setEmoteStyle(null);
        }
    }
}
