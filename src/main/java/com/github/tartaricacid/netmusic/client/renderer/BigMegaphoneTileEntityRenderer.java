package com.github.tartaricacid.netmusic.client.renderer;

import com.github.tartaricacid.netmusic.client.model.ModelBigMegaphone;
import com.github.tartaricacid.netmusic.tileentity.TileEntityBigMegaphone;
import net.minecraft.ResourceLocation;
import net.minecraft.TileEntity;
import net.minecraft.TileEntitySpecialRenderer;
import org.lwjgl.opengl.GL11;

public class BigMegaphoneTileEntityRenderer extends TileEntitySpecialRenderer {
    private static final ResourceLocation TEXTURE_OFF = new ResourceLocation("netmusic:textures/block/big_megaphone_off.png");
    private static final ResourceLocation TEXTURE_ON = new ResourceLocation("netmusic:textures/block/big_megaphone_on.png");
    private final ModelBigMegaphone model = new ModelBigMegaphone();

    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        if (!(tile instanceof TileEntityBigMegaphone megaphone)) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5D, y + 1.5D, z + 0.5D);
        GL11.glRotatef(getFacingAngle(tile.getBlockMetadata()), 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);

        bindTexture(megaphone.isBroadcasting() ? TEXTURE_ON : TEXTURE_OFF);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_CULL_FACE);
        this.model.render(0.0625F);
        GL11.glEnable(GL11.GL_CULL_FACE);

        GL11.glPopMatrix();
    }

    private static float getFacingAngle(int metadata) {
        switch (metadata & 3) {
            case 0:
                return 180.0F;
            case 1:
                return 90.0F;
            case 2:
                return 0.0F;
            case 3:
                return 270.0F;
            default:
                return 0.0F;
        }
    }
}
