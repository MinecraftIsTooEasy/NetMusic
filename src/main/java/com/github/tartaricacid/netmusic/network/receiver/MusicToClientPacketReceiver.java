package com.github.tartaricacid.netmusic.network.receiver;

import com.github.tartaricacid.netmusic.network.packet.MusicToClientPacket;
import net.minecraft.Minecraft;

public final class MusicToClientPacketReceiver {

    public static void handle(MusicToClientPacket packet) {
        if (packet == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft != null && minecraft.thePlayer != null) {
            packet.apply(minecraft.thePlayer);
        }
    }
}
