package com.github.tartaricacid.netmusic.mixin;

import com.github.tartaricacid.netmusic.client.renderer.HeldItemRenderContext;
import net.minecraft.AbstractClientPlayer;
import net.minecraft.RenderPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPlayer.class)
public abstract class RenderPlayerMixin {
    @Inject(method = "renderSpecials", at = @At("HEAD"))
    private void netmusic$pushThirdPersonItemContext(AbstractClientPlayer player, float partialTicks, CallbackInfo ci) {
        HeldItemRenderContext.push(HeldItemRenderContext.Context.THIRD_PERSON);
    }

    @Inject(method = "renderSpecials", at = @At("RETURN"))
    private void netmusic$popThirdPersonItemContext(AbstractClientPlayer player, float partialTicks, CallbackInfo ci) {
        HeldItemRenderContext.pop();
    }
}
