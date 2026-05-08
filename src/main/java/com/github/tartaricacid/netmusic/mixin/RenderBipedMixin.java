package com.github.tartaricacid.netmusic.mixin;

import com.github.tartaricacid.netmusic.client.renderer.HeldItemRenderContext;
import net.minecraft.EntityLiving;
import net.minecraft.RenderBiped;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderBiped.class)
public abstract class RenderBipedMixin {
    @Inject(method = "func_130005_c", at = @At("HEAD"))
    private void netmusic$pushThirdPersonItemContext(EntityLiving entity, float partialTicks, CallbackInfo ci) {
        HeldItemRenderContext.push(HeldItemRenderContext.Context.THIRD_PERSON);
    }

    @Inject(method = "func_130005_c", at = @At("RETURN"))
    private void netmusic$popThirdPersonItemContext(EntityLiving entity, float partialTicks, CallbackInfo ci) {
        HeldItemRenderContext.pop();
    }
}
