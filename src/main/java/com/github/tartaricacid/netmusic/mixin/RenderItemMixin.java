package com.github.tartaricacid.netmusic.mixin;

import com.github.tartaricacid.netmusic.block.BlockBigMegaphone;
import com.github.tartaricacid.netmusic.block.BlockMusicPlayer;
import com.github.tartaricacid.netmusic.client.renderer.BigMegaphoneItemRenderer;
import com.github.tartaricacid.netmusic.client.renderer.MusicPlayerItemRenderer;
import net.minecraft.Block;
import net.minecraft.FontRenderer;
import net.minecraft.ItemStack;
import net.minecraft.RenderItem;
import net.minecraft.TextureManager;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderItem.class)
public abstract class RenderItemMixin {

    @Shadow
    public float zLevel;

    @Inject(method = "renderItemIntoGUI", at = @At("HEAD"), cancellable = true)
    private void netmusic$renderCustomBlockInGui(FontRenderer fontRenderer, TextureManager textureManager, ItemStack stack, int x, int y, CallbackInfo ci) {
        if (stack == null || !stack.isBlock()) {
            return;
        }
        Block block = Block.blocksList[stack.itemID];
        if (!(block instanceof BlockMusicPlayer) && !(block instanceof BlockBigMegaphone)) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);

        GL11.glTranslatef(x + 10.0F, y - 13.0F, this.zLevel);
        GL11.glScalef(16.0F, -16.0F, 16.0F);

        if (block instanceof BlockMusicPlayer) {
            MusicPlayerItemRenderer.renderItemIntoGui();
        } else {
            BigMegaphoneItemRenderer.renderItemIntoGui();
        }

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();
        ci.cancel();
    }
}
