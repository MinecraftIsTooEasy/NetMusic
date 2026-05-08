package com.github.tartaricacid.netmusic.client.renderer;

import com.github.tartaricacid.netmusic.client.model.ModelMusicPlayer;
import net.minecraft.Minecraft;
import net.minecraft.ResourceLocation;
import org.lwjgl.opengl.GL11;

public final class MusicPlayerItemRenderer {

    private static final ResourceLocation MUSIC_PLAYER_TEXTURE = new ResourceLocation("netmusic:textures/blocks/music_player.png");

    private static final ModelMusicPlayer musicPlayerModel = new ModelMusicPlayer();

    public static void renderBlockAsItem() {
        GL11.glScalef(4.0F / 3.0F, 4.0F / 3.0F, 4.0F / 3.0F);
        GL11.glTranslatef(0.5F - 0.5F / 0.75F, 0.0F, 0.5F - 0.5F / 0.75F);
        GL11.glScalef(0.75F, 0.75F, 0.75F);
        GL11.glTranslatef(0.5F / 0.75F, 1.5F, 0.5F / 0.75F);
        GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);

        drawModel();
    }

    public static void renderItemIntoGui() {
        float guiTy = -2.5F * 0.0625F;
        float guiRx = 22.5F;
        float guiRy = 112.5F;
        float guiScale = 0.5F;

        GL11.glTranslatef(0.0F, guiTy, 0.0F);
        GL11.glRotatef(guiRx, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(guiRy, 0.0F, 1.0F, 0.0F);
        GL11.glScalef(guiScale, guiScale, guiScale);

        float pivotX = -0.5F;
        float pivotY = -1.5F;
        float pivotZ = -0.5F;

        GL11.glTranslatef(pivotX, pivotY, pivotZ);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);

        drawModel();
    }

    private static void drawModel() {
        Minecraft.getMinecraft().getTextureManager().bindTexture(MUSIC_PLAYER_TEXTURE);
        musicPlayerModel.getDiscPart().showModel = false;
        musicPlayerModel.render(0.0625F);
    }
}
