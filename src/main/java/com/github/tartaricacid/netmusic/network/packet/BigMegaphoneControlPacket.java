package com.github.tartaricacid.netmusic.network.packet;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.tileentity.TileEntityBigMegaphone;
import com.github.tartaricacid.netmusic.util.BigMegaphoneUtil;
import moddedmite.rustedironcore.network.Packet;
import moddedmite.rustedironcore.network.PacketByteBuf;
import net.minecraft.EntityPlayer;
import net.minecraft.ResourceLocation;
import net.minecraft.TileEntity;
import org.apache.commons.lang3.StringUtils;

public class BigMegaphoneControlPacket implements Packet {

    public static final ResourceLocation ID = new ResourceLocation(NetMusic.MOD_ID, "big_megaphone_control");

    private final int x;
    private final int y;
    private final int z;
    private final String url;
    private final String name;
    private final int range;
    private final Action action;

    public BigMegaphoneControlPacket(PacketByteBuf buf) {
        this(buf.readInt(), buf.readInt(), buf.readInt(), buf.readString(), buf.readString(), buf.readInt(), Action.fromOrdinal(buf.readInt()));
    }

    public BigMegaphoneControlPacket(int x, int y, int z, String url, String name, int range, Action action) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.url = url == null ? "" : url;
        this.name = name == null ? "" : name;
        this.range = range;
        this.action = action == null ? Action.SAVE : action;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
        buf.writeString(this.url);
        buf.writeString(this.name);
        buf.writeInt(this.range);
        buf.writeInt(this.action.ordinal());
    }

    @Override
    public void apply(EntityPlayer entityPlayer) {
        if (entityPlayer == null || entityPlayer.worldObj == null || entityPlayer.worldObj.isRemote) {
            return;
        }
        if (entityPlayer.getDistanceSq(this.x + 0.5D, this.y + 0.5D, this.z + 0.5D) > 64.0D) {
            return;
        }
        TileEntity tile = entityPlayer.worldObj.getBlockTileEntity(this.x, this.y, this.z);
        if (!(tile instanceof TileEntityBigMegaphone megaphone)) {
            return;
        }
        if (this.action == Action.STOP) {
            megaphone.stopBroadcast();
            return;
        }
        if (!BigMegaphoneUtil.isValidStreamUrl(this.url) || StringUtils.isBlank(this.name)) {
            return;
        }
        boolean changed = megaphone.applyConfig(this.url, this.name, this.range);
        if (this.action == Action.START) {
            megaphone.startBroadcast();
        } else if (changed && megaphone.isBroadcasting()) {
            megaphone.startBroadcast();
        }
    }

    @Override
    public ResourceLocation getChannel() {
        return ID;
    }

    public enum Action {
        SAVE,
        START,
        STOP;

        public static Action fromOrdinal(int ordinal) {
            Action[] values = values();
            if (ordinal < 0 || ordinal >= values.length) {
                return SAVE;
            }
            return values[ordinal];
        }
    }
}
