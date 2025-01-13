package me.falu.twitchemotes.mixin.chat;

import com.google.common.collect.Maps;
import me.falu.twitchemotes.TwitchEmotes;
import me.falu.twitchemotes.emote.Emote;
import me.falu.twitchemotes.emote.EmoteStyleOwner;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.*;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
    @Unique private final Map<ChatHudLine, String> messageIds = new HashMap<>();
    @Unique private final Map<ChatHudLine.Visible, String> visibleMessageIds = new HashMap<>();

    @Unique
    private MutableText transformText(Text prefix, Text content, Map<String, Emote> specific) {
        MutableText message = prefix.copy();
        content.visit((style, string) -> {
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
            for (String word : words) {
                Emote emote = TwitchEmotes.getEmote(word.trim(), specific);
                if (emote != null) {
                    message.append(Text.literal("_").styled(s -> ((EmoteStyleOwner) style).twitchemotes$withEmoteStyle(emote)));
                    String deleted = word.replace(word.trim(), "");
                    if (!deleted.isEmpty()) {
                        message.append(Text.literal(deleted).setStyle(style));
                    }
                } else {
                    message.append(Text.literal(word).setStyle(style));
                }
            }
            return Optional.empty();
        }, Style.EMPTY);
        return message;
    }

    @ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private Text transformMessageText(Text text) {
        return this.transformText(Text.literal(""), text, Maps.newHashMap());
    }
}
