package com.github.tartaricacid.netmusic.client.init;

import com.github.tartaricacid.netmusic.network.packet.BigMegaphoneStartPacket;
import com.github.tartaricacid.netmusic.network.packet.BigMegaphoneStopPacket;
import com.github.tartaricacid.netmusic.network.packet.GetMusicListPacket;
import com.github.tartaricacid.netmusic.network.packet.MusicToClientPacket;
import com.github.tartaricacid.netmusic.network.packet.MusicPlayerStatePacket;
import com.github.tartaricacid.netmusic.network.packet.OpenMenuPacket;
import moddedmite.rustedironcore.network.PacketReader;
import moddedmite.rustedironcore.network.PacketSupplier;
import net.minecraft.ResourceLocation;

public class ClientReceiverRegistry {
    public static void register() {
        registerReceiver(MusicToClientPacket.ID, MusicToClientPacket::new);
        registerReceiver(MusicPlayerStatePacket.ID, MusicPlayerStatePacket::new);
        registerReceiver(GetMusicListPacket.ID, GetMusicListPacket::new);
        registerReceiver(OpenMenuPacket.ID, OpenMenuPacket::new);
        registerReceiver(BigMegaphoneStartPacket.ID, BigMegaphoneStartPacket::new);
        registerReceiver(BigMegaphoneStopPacket.ID, BigMegaphoneStopPacket::new);
    }

    public static void registerReceiver(ResourceLocation channel, PacketSupplier packetSupplier) {
        PacketReader.registerClientPacketReader(channel, packetSupplier);
    }
}
