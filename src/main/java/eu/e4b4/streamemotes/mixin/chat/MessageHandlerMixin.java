package eu.e4b4.streamemotes.mixin.chat;

import com.mojang.authlib.GameProfile;
import eu.e4b4.streamemotes.StreamEmotes;
import eu.e4b4.streamemotes.emote.Emote;
import eu.e4b4.streamemotes.emote.EmoteStyleOwner;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.client.network.message.MessageTrustStatus;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(MessageHandler.class)
public class MessageHandlerMixin {
    @Inject(method = "onChatMessage", at = @At(value = "HEAD"), cancellable = true)
    public void onChatMessage(SignedMessage message, GameProfile sender, MessageType.Parameters params, CallbackInfo ci) {
        MessageHandlerInvoker mh = (MessageHandlerInvoker) (this);

        Text text = params.applyChatDecoration(message.getContent());

        new Thread(() -> {
            // We will add the message to the chat _ASYNCHRONOUSLY_. This is because we may have to go over the network to fetch the user's emotes.

            MutableText textUpdated = Text.of("").copy();
            text.visit((style, string) -> {
                List<String> words = new ArrayList<>();
                StringBuilder split = new StringBuilder();
                for (int i = 0; i < string.length(); i++) {
                    char character = string.charAt(i);
                    split.append(character);
                    if (character == ' ' || i == string.length() - 1) {
                        words.add(split.toString());
                        split = new StringBuilder();
                    }
                }
                synchronized(StreamEmotes.USER_EMOTE_MAP) {
                    for (String word : words) {
                        Emote emote = StreamEmotes.getEmote(word.trim(), sender.getId());
                        // Emote emote = TwitchEmotes.getEmote(word.trim(), UUID.fromString("dba5d39b-d48b-4b1f-8f99-7e0f4950c684"));
                        if (emote != null) {
                            textUpdated.append(Text.literal("_").styled(s -> ((EmoteStyleOwner) style).twitchemotes$withEmoteStyle(emote)));
                            String deleted = word.replace(word.trim(), "");
                            if (!deleted.isEmpty()) {
                                textUpdated.append(Text.literal(deleted).setStyle(style));
                            }
                        } else {
                            textUpdated.append(Text.literal(word).setStyle(style));
                        }
                    }
                }
                return Optional.empty();
            }, Style.EMPTY);

            mh.callProcess(message.signature(), () -> {
                mh.getClient().inGameHud.getChatHud().addMessage(textUpdated, message.signature(), MessageTrustStatus.NOT_SECURE.createIndicator(message));

                ClientPlayNetworkHandler clientPlayNetworkHandler = mh.getClient().getNetworkHandler();
                if (clientPlayNetworkHandler != null) {
                    clientPlayNetworkHandler.acknowledge(message, true);
                }

                return true;
            });
        }).start();

        ci.cancel();
    }
}
