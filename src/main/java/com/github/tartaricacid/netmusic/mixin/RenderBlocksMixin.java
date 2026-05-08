package com.github.tartaricacid.netmusic.mixin;

import com.github.tartaricacid.netmusic.block.BlockBigMegaphone;
import com.github.tartaricacid.netmusic.block.BlockMusicPlayer;
import com.github.tartaricacid.netmusic.client.renderer.BigMegaphoneItemRenderer;
import com.github.tartaricacid.netmusic.client.renderer.HeldItemRenderContext;
import com.github.tartaricacid.netmusic.client.renderer.MusicPlayerItemRenderer;
import net.minecraft.Block;
import net.minecraft.RenderBlocks;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderBlocks.class)
public abstract class RenderBlocksMixin {

    @Inject(method = "renderBlockAsItem", at = @At("HEAD"), cancellable = true)
    private void netmusic$renderCustomBlockAsItem(Block block, int metadata, float brightness, CallbackInfo ci) {
        if (!(block instanceof BlockMusicPlayer) && !(block instanceof BlockBigMegaphone)) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);

        if (block instanceof BlockMusicPlayer) {
            MusicPlayerItemRenderer.renderBlockAsItem();
        } else {
            switch (HeldItemRenderContext.current()) {
                case FIRST_PERSON -> BigMegaphoneItemRenderer.renderFirstPerson();
                case THIRD_PERSON -> BigMegaphoneItemRenderer.renderThirdPerson();
                default -> BigMegaphoneItemRenderer.renderBlockAsItem();
            }
        }

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();
        ci.cancel();
    }
}
