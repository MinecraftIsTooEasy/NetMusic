package com.github.tartaricacid.netmusic.client;

import com.github.tartaricacid.netmusic.client.renderer.BigMegaphoneTileEntityRenderer;
import com.github.tartaricacid.netmusic.client.renderer.MusicPlayerTileEntityRenderer;
import com.github.tartaricacid.netmusic.tileentity.TileEntityBigMegaphone;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import com.google.common.eventbus.Subscribe;
import net.xiaoyu233.fml.reload.event.TileEntityRendererRegisterEvent;

public class ClientEventListener {

    @Subscribe
    public void onTileEntityRendererRegister(TileEntityRendererRegisterEvent event) {
        event.register(TileEntityMusicPlayer.class, new MusicPlayerTileEntityRenderer());
        event.register(TileEntityBigMegaphone.class, new BigMegaphoneTileEntityRenderer());
    }
}
