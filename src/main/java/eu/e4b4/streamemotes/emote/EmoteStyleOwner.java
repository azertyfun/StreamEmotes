package eu.e4b4.streamemotes.emote;

import net.minecraft.text.Style;

public interface EmoteStyleOwner {
    Style twitchemotes$withEmoteStyle(Emote emoteStyle);
    void twitchemotes$setEmoteStyle(Emote emoteStyle);
    Emote twitchemotes$getEmoteStyle();
}
