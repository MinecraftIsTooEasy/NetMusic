package com.github.tartaricacid.netmusic.network.packet;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.audio.BigMegaphoneClientManager;
import moddedmite.rustedironcore.network.Packet;
import moddedmite.rustedironcore.network.PacketByteBuf;
import net.minecraft.EntityPlayer;
import net.minecraft.ResourceLocation;

public class BigMegaphoneStopPacket implements Packet {

    public static final ResourceLocation ID = new ResourceLocation(NetMusic.MOD_ID, "big_megaphone_stop");

    private final int x;
    private final int y;
    private final int z;
    private final long sessionId;

    public BigMegaphoneStopPacket(PacketByteBuf buf) {
        this(buf.readInt(), buf.readInt(), buf.readInt(), buf.readLong());
    }

    public BigMegaphoneStopPacket(int x, int y, int z, long sessionId) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.sessionId = sessionId;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
        buf.writeLong(this.sessionId);
    }

    @Override
    public void apply(EntityPlayer entityPlayer) {
        if (entityPlayer == null || entityPlayer.worldObj == null || !entityPlayer.worldObj.isRemote) {
            return;
        }
        BigMegaphoneClientManager.stop(this.x, this.y, this.z, this.sessionId);
    }

    @Override
    public ResourceLocation getChannel() {
        return ID;
    }
}
