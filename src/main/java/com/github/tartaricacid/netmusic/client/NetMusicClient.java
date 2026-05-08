package com.github.tartaricacid.netmusic.client;

import com.github.tartaricacid.netmusic.client.audio.ClientMusicPlayer;
import com.github.tartaricacid.netmusic.client.audio.BigMegaphoneClientManager;
import com.github.tartaricacid.netmusic.client.config.ClientVipCookieManager;
import com.github.tartaricacid.netmusic.client.init.ClientReceiverRegistry;
import com.github.tartaricacid.netmusic.client.init.InitContainerGui;
import com.github.tartaricacid.netmusic.client.init.InitModel;
import moddedmite.rustedironcore.api.event.Handlers;
import moddedmite.rustedironcore.api.event.listener.ITickListener;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.Minecraft;
import net.xiaoyu233.fml.reload.event.MITEEvents;

public class NetMusicClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        InitContainerGui.init();
        InitModel.register();
        ClientReceiverRegistry.register();
        MITEEvents.MITE_EVENT_BUS.register(new ClientEventListener());

        Handlers.Tick.register(new ITickListener() {
            @Override
            public void onClientTick(Minecraft client) {
                ClientMusicPlayer.clientTick();
                BigMegaphoneClientManager.clientTick();
                ClientVipCookieManager.clientTick();
            }
        });
    }
}
