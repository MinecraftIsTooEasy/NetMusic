package com.github.tartaricacid.netmusic.client.renderer;

import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.client.model.ModelMusicPlayer;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import com.github.tartaricacid.netmusic.event.ConfigEvent;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import net.minecraft.*;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;

public class MusicPlayerTileEntityRenderer extends TileEntitySpecialRenderer {

    private static final ResourceLocation TEXTURE = new ResourceLocation("netmusic:textures/blocks/music_player.png");
    private final ModelMusicPlayer model = new ModelMusicPlayer();

    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        if (!(tile instanceof TileEntityMusicPlayer te)) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);

        boolean hasDisc = te.getItem(0) != null;
        ModelRenderer disc = model.getDiscPart();
        disc.showModel = hasDisc;
        if (hasDisc && te.isPlay()) {
            disc.rotateAngleY = (float) ((2 * Math.PI / 40) * (((double) System.currentTimeMillis() / 50) % 40));
        } else {
            disc.rotateAngleY = 0;
        }

        GL11.glScalef(0.75F, 0.75F, 0.75F);
        GL11.glTranslatef(0.5F / 0.75F, 1.5F, 0.5F / 0.75F);

        int metadata = tile.getBlockMetadata();
        float facingAngle = getFacingAngle(metadata);

        GL11.glRotatef(facingAngle, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);

        bindTexture(TEXTURE);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_CULL_FACE);
        model.render(0.0625F);
        GL11.glEnable(GL11.GL_CULL_FACE);

        GL11.glPopMatrix();

        renderLyric(te, x, y, z);
    }

    private static float getFacingAngle(int metadata) {
        switch (metadata & 3) {
            case 0:
                return 180.0F; // south
            case 1:
                return 90.0F;  // west
            case 2:
                return 0.0F;   // north
            case 3:
                return 270.0F; // east
            default:
                return 0.0F;
        }
    }

    private void renderLyric(TileEntityMusicPlayer te, double x, double y, double z) {
        if (!GeneralConfig.ENABLE_PLAYER_LYRICS) {
            return;
        }
        LyricRecord lyricRecord = te.lyricRecord;
        if (lyricRecord == null) {
            return;
        }
        Int2ObjectSortedMap<String> lyrics = lyricRecord.getLyrics();
        if (lyrics == null || lyrics.isEmpty()) {
            return;
        }
        if (!te.isPlay()) {
            te.lyricRecord = null;
            return;
        }

        String current = lyrics.get(lyrics.firstIntKey());
        if (StringUtils.isBlank(current)) {
            current = "";
        }

        String translated = null;
        int currentLyricColor = ConfigEvent.PLAYER_ORIGINAL_COLOR;
        int transLyricColor = ConfigEvent.PLAYER_TRANSLATED_COLOR;

        Int2ObjectSortedMap<String> transLyrics = lyricRecord.getTransLyrics();
        if (transLyrics != null && !transLyrics.isEmpty()) {
            translated = transLyrics.get(transLyrics.firstIntKey());
            if (StringUtils.isBlank(translated)) {
                translated = null;
            }
        } else {
            // Match upstream: when no translation exists, use the translated color for the only line.
            currentLyricColor = ConfigEvent.PLAYER_TRANSLATED_COLOR;
        }

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5D, y + 1.625D, z + 0.5D);

        // Billboard towards camera.
        GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-0.025F, -0.025F, 0.025F);

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Slightly offset to avoid z-fighting when multiple players are around.
        int yOffset = translated == null ? 0 : 6;
        FontRenderer font = this.getFontRenderer();
        int currentWidth = font.getStringWidth(current);
        font.drawStringWithShadow(current, -(currentWidth / 2), -yOffset, currentLyricColor);

        if (translated != null) {
            int transWidth = font.getStringWidth(translated);
            font.drawStringWithShadow(translated, -(transWidth / 2), -yOffset - 12, transLyricColor);
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }
}
