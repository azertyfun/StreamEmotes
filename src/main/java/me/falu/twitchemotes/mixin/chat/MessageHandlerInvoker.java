package me.falu.twitchemotes.mixin.chat;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.time.Instant;
import java.util.function.BooleanSupplier;

@Mixin(MessageHandler.class)
public interface MessageHandlerInvoker {
    @Invoker public void callProcess(@Nullable MessageSignatureData signature, BooleanSupplier processor);

    @Invoker public boolean callProcessChatMessageInternal(
            MessageType.Parameters params, SignedMessage message, Text decorated, GameProfile sender, boolean onlyShowSecureChat, Instant receptionTimestamp
    );

    @Accessor public MinecraftClient getClient();
}
