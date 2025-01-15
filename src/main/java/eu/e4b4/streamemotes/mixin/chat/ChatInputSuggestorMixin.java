package eu.e4b4.streamemotes.mixin.chat;

import eu.e4b4.streamemotes.StreamEmotes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.network.ClientCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;

@Mixin(ChatInputSuggestor.class)
public class ChatInputSuggestorMixin {
    @Redirect(method = "refresh", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientCommandSource;getChatSuggestions()Ljava/util/Collection;"))
    private Collection<String> suggestEmotes(ClientCommandSource instance) {
        return StreamEmotes.USER_EMOTE_MAP.get(MinecraftClient.getInstance().player.getUuid()).keySet();
    }
}
