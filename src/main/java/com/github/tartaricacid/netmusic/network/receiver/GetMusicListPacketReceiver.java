package com.github.tartaricacid.netmusic.network.receiver;

import com.github.tartaricacid.netmusic.network.packet.GetMusicListPacket;
import net.minecraft.Minecraft;

public final class GetMusicListPacketReceiver {

    public static void handle(GetMusicListPacket packet) {
        if (packet == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft != null && minecraft.thePlayer != null) {
            packet.apply(minecraft.thePlayer);
        }
    }
}
