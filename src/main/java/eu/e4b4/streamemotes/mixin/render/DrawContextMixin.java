package eu.e4b4.streamemotes.mixin.render;

import eu.e4b4.streamemotes.StreamEmotes;
import eu.e4b4.streamemotes.emote.Emote;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DrawContext.class)
public class DrawContextMixin {
    @Inject(
            method = {
                    "drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)I",
                    "drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;IIIZ)I"
            },
            at = @At(
                    value = "RETURN"
            )
    )
    private void drawScheduledEmotes(CallbackInfoReturnable<Integer> cir) {
        Emote.DrawData data;
        while ((data = StreamEmotes.SCHEDULED_DRAW.poll()) != null) {
            data.draw();
        }
    }
}
