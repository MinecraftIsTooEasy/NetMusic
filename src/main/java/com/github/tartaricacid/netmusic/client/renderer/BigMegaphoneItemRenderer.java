package com.github.tartaricacid.netmusic.client.renderer;

import com.github.tartaricacid.netmusic.client.model.ModelBigMegaphone;
import net.minecraft.Minecraft;
import net.minecraft.ResourceLocation;
import org.lwjgl.opengl.GL11;

public final class BigMegaphoneItemRenderer {
    private static final ResourceLocation BIG_MEGAPHONE_TEXTURE = new ResourceLocation("netmusic:textures/block/big_megaphone_off.png");
    private static final ModelBigMegaphone BIG_MEGAPHONE_MODEL = new ModelBigMegaphone();

    private BigMegaphoneItemRenderer() {}

    public static void renderBlockAsItem() {
        GL11.glTranslatef(0.5F, 1.5F, 0.5F);
        GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        GL11.glScalef(0.5F, 0.5F, 0.5F);

        drawModel();
    }

    public static void renderItemIntoGui() {
        applyDisplayTransform(22.5F, 22.5F, 0.0F, -2.0F, -7.5F, 0.0F, 0.45F);

        drawModel();
    }

    public static void renderFirstPerson() {
        applyDisplayTransform(0.0F, -45.0F, 30.0F, 1.75F, 1.75F, 1.75F, 0.25F);

        drawModel();
    }

    public static void renderThirdPerson() {
        applyDisplayTransform(45.0F, -115.0F, 0.0F, 0.0F, -0.5F, -0.5F, 0.375F);

        drawModel();
    }

    private static void applyDisplayTransform(float rotationX, float rotationY, float rotationZ,
                                              float translationX, float translationY, float translationZ,
                                              float scale) {
        GL11.glTranslatef(translationX * 0.0625F, translationY * 0.0625F, translationZ * 0.0625F);
        GL11.glRotatef(rotationX, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(rotationY + 90.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(rotationZ, 0.0F, 0.0F, 1.0F);
        GL11.glScalef(scale, scale, scale);
        GL11.glTranslatef(0.0F, -1.5F, 0.0F);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
    }

    private static void drawModel() {
        Minecraft.getMinecraft().getTextureManager().bindTexture(BIG_MEGAPHONE_TEXTURE);
        BIG_MEGAPHONE_MODEL.render(0.0625F);
    }
}
