package com.github.tartaricacid.netmusic.client.init;

import com.github.tartaricacid.netmusic.init.InitBlocks;
import moddedmite.rustedironcore.api.event.Handlers;
import moddedmite.rustedironcore.api.event.listener.IJsonModelListener;
import moddedmite.rustedironcore.api.model.JsonModelRegistry;

public class InitModel extends Handlers {

    public static void register() {

        JsonModel.register(new IJsonModelListener() {
            @Override
            public void onJsonModelRegister(JsonModelRegistry registry) {
                registry.registerBlockState(InitBlocks.CD_BURNER, "netmusic:cd_burner");
                registry.registerBlockState(InitBlocks.COMPUTER, "netmusic:computer");
                registry.registerItemModel(InitBlocks.CD_BURNER, "netmusic:cd_burner");
                registry.registerItemModel(InitBlocks.COMPUTER, "netmusic:computer");
                registry.registerBlockState(InitBlocks.MUSIC_PLAYER, "netmusic:music_player");
                registry.registerItemModel(InitBlocks.MUSIC_PLAYER, "netmusic:music_player");
                registry.registerBlockState(InitBlocks.BIG_MEGAPHONE, "netmusic:big_megaphone");
                registry.registerItemModel(InitBlocks.BIG_MEGAPHONE, "netmusic:big_megaphone");
            }
        });
    }
}
