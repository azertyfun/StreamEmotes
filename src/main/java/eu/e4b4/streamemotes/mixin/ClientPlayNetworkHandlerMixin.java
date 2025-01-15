package eu.e4b4.streamemotes.mixin;

import eu.e4b4.streamemotes.StreamEmotes;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "handlePlayerListAction", at = @At(value = "HEAD"))
    private void handlePlayerListAction(PlayerListS2CPacket.Action action, PlayerListS2CPacket.Entry receivedEntry, PlayerListEntry currentEntry, CallbackInfo ci) {
        if (action == PlayerListS2CPacket.Action.UPDATE_LISTED && receivedEntry.listed()) {
            // Player joined the game (probably) because they've been added to the player list!

            if (receivedEntry.profile() == null) {
                StreamEmotes.LOGGER.warn("Got null profile for new player entry");
                return;
            }

            new Thread(() -> {
                StreamEmotes.LOGGER.info("User {} ({}) joined the game; loading all emotes.", receivedEntry.profile().getName(), receivedEntry.profile().getId());
                synchronized (StreamEmotes.USER_EMOTE_MAP) {
                    StreamEmotes.USER_EMOTE_MAP.put(receivedEntry.profile().getId(), StreamEmotes.fetchPlayerEmotes(receivedEntry.profile().getId()));
                }
            }).start();
        }
    }
}
