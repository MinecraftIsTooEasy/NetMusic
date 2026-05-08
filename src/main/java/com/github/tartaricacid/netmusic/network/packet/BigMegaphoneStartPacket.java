package com.github.tartaricacid.netmusic.network.packet;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.audio.BigMegaphoneClientManager;
import moddedmite.rustedironcore.network.Packet;
import moddedmite.rustedironcore.network.PacketByteBuf;
import net.minecraft.EntityPlayer;
import net.minecraft.ResourceLocation;

public class BigMegaphoneStartPacket implements Packet {

    public static final ResourceLocation ID = new ResourceLocation(NetMusic.MOD_ID, "big_megaphone_start");

    private final int x;
    private final int y;
    private final int z;
    private final long sessionId;
    private final String url;
    private final String name;
    private final int range;

    public BigMegaphoneStartPacket(PacketByteBuf buf) {
        this(buf.readInt(), buf.readInt(), buf.readInt(), buf.readLong(), buf.readString(), buf.readString(), buf.readInt());
    }

    public BigMegaphoneStartPacket(int x, int y, int z, long sessionId, String url, String name, int range) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.sessionId = sessionId;
        this.url = url == null ? "" : url;
        this.name = name == null ? "" : name;
        this.range = range;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
        buf.writeLong(this.sessionId);
        buf.writeString(this.url);
        buf.writeString(this.name);
        buf.writeInt(this.range);
    }

    @Override
    public void apply(EntityPlayer entityPlayer) {
        if (entityPlayer == null || entityPlayer.worldObj == null || !entityPlayer.worldObj.isRemote) {
            return;
        }
        BigMegaphoneClientManager.start(this.x, this.y, this.z, this.sessionId, this.url, this.name, this.range);
    }

    @Override
    public ResourceLocation getChannel() {
        return ID;
    }
}
