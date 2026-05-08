package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.network.packet.BigMegaphoneControlPacket;
import com.github.tartaricacid.netmusic.network.packet.SetMusicIDPacket;
import com.github.tartaricacid.netmusic.network.packet.SyncVipCookiePacket;
import moddedmite.rustedironcore.network.PacketReader;
import moddedmite.rustedironcore.network.PacketSupplier;
import net.minecraft.ResourceLocation;

public class ServerReceiverRegistry {

    public static void register() {
        registerReceiver(SetMusicIDPacket.ID, SetMusicIDPacket::new);
        registerReceiver(SyncVipCookiePacket.ID, SyncVipCookiePacket::new);
        registerReceiver(BigMegaphoneControlPacket.ID, BigMegaphoneControlPacket::new);
    }

    public static void registerReceiver(ResourceLocation channelName, PacketSupplier packetSupplier) {
        PacketReader.registerServerPacketReader(channelName, packetSupplier);
    }
}
