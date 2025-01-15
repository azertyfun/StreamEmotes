package eu.e4b4.streamemotes.mixin;

import eu.e4b4.streamemotes.gui.overlay.HtyLaserOverlay;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {
    @Unique private static final HtyLaserOverlay HTYLASER_OVERLAY = new HtyLaserOverlay();
    @Shadow public int width;
    @Shadow public int height;

    @Inject(method = "renderBackground", at = @At("TAIL"))
    private void renderPpOverlay(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        HTYLASER_OVERLAY.render(context.getMatrices(), this.width, this.height);
    }
}
