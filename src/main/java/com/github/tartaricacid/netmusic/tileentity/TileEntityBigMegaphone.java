package com.github.tartaricacid.netmusic.tileentity;

import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.github.tartaricacid.netmusic.network.packet.BigMegaphoneStartPacket;
import com.github.tartaricacid.netmusic.network.packet.BigMegaphoneStopPacket;
import com.github.tartaricacid.netmusic.util.BigMegaphoneUtil;
import net.minecraft.NBTTagCompound;
import net.minecraft.Packet;
import net.minecraft.Packet132TileEntityData;
import net.minecraft.ServerPlayer;
import net.minecraft.TileEntity;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public class TileEntityBigMegaphone extends TileEntity {
    private static final String URL_TAG = "StreamUrl";
    private static final String NAME_TAG = "DisplayName";
    private static final String RANGE_TAG = "MaxRange";
    private static final String BROADCASTING_TAG = "Broadcasting";
    private static final String SESSION_TAG = "SessionId";
    private static final int DEFAULT_MAX_RANGE = 96;
    private static final int DEFAULT_SCAN_INTERVAL = 20;

    private String streamUrl = "";
    private String displayName = "";
    private int maxRange = DEFAULT_MAX_RANGE;
    private boolean broadcasting;
    private boolean lastRedstoneSignal;
    private long sessionId;
    private final Set<UUID> listeners = new HashSet<UUID>();

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.streamUrl = nbt.getString(URL_TAG);
        this.displayName = nbt.getString(NAME_TAG);
        this.maxRange = BigMegaphoneUtil.clampRange(nbt.hasKey(RANGE_TAG) ? nbt.getInteger(RANGE_TAG) : DEFAULT_MAX_RANGE, DEFAULT_MAX_RANGE);
        this.broadcasting = nbt.getBoolean(BROADCASTING_TAG);
        this.sessionId = Math.max(0L, nbt.getLong(SESSION_TAG));
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setString(URL_TAG, this.streamUrl);
        nbt.setString(NAME_TAG, this.displayName);
        nbt.setInteger(RANGE_TAG, this.maxRange);
        nbt.setBoolean(BROADCASTING_TAG, this.broadcasting);
        nbt.setLong(SESSION_TAG, this.sessionId);
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (this.worldObj == null || this.worldObj.isRemote) {
            return;
        }
        if (!this.broadcasting) {
            if (!this.listeners.isEmpty()) {
                stopAllListeners();
            }
            return;
        }
        if (this.worldObj.getTotalWorldTime() % DEFAULT_SCAN_INTERVAL == 0L) {
            refreshAudience();
        }
    }

    public void onRedstoneSignalChanged(boolean hasSignal) {
        if (this.worldObj == null || this.worldObj.isRemote || this.lastRedstoneSignal == hasSignal) {
            return;
        }
        this.lastRedstoneSignal = hasSignal;
        if (hasSignal) {
            if (this.broadcasting) {
                stopBroadcast();
            } else {
                startBroadcast();
            }
        }
    }

    public boolean applyConfig(String streamUrl, String displayName, int maxRange) {
        String nextUrl = streamUrl == null ? "" : streamUrl.trim();
        String nextName = displayName == null ? "" : displayName.trim();
        int nextRange = BigMegaphoneUtil.clampRange(maxRange, DEFAULT_MAX_RANGE);
        boolean changed = !this.streamUrl.equals(nextUrl) || !this.displayName.equals(nextName) || this.maxRange != nextRange;
        this.streamUrl = nextUrl;
        this.displayName = nextName;
        this.maxRange = nextRange;
        this.setChanged();
        return changed;
    }

    public void startBroadcast() {
        if (this.worldObj == null || this.worldObj.isRemote || !BigMegaphoneUtil.isValidStreamUrl(this.streamUrl)
                || this.displayName.trim().isEmpty()) {
            return;
        }
        stopAllListeners();
        this.broadcasting = true;
        this.sessionId++;
        this.setChanged();
        refreshAudience();
    }

    public void stopBroadcast() {
        if (this.worldObj != null && !this.worldObj.isRemote) {
            stopAllListeners();
        } else {
            this.listeners.clear();
        }
        this.broadcasting = false;
        this.setChanged();
    }

    public void onBlockRemoved() {
        stopBroadcast();
    }

    public String getStreamUrl() {
        return this.streamUrl;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public int getMaxRange() {
        return this.maxRange;
    }

    public boolean isBroadcasting() {
        return this.broadcasting;
    }

    public long getSessionId() {
        return this.sessionId;
    }

    public void setChanged() {
        this.onInventoryChanged();
        if (this.worldObj != null) {
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
            this.worldObj.markBlockForRenderUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
    }

    private void stopAllListeners() {
        if (this.worldObj == null || this.worldObj.playerEntities == null) {
            this.listeners.clear();
            return;
        }
        for (UUID uuid : this.listeners) {
            ServerPlayer player = findServerPlayer(uuid);
            if (player != null) {
                NetworkHandler.sendToClientPlayer(new BigMegaphoneStopPacket(this.xCoord, this.yCoord, this.zCoord, this.sessionId), player);
            }
        }
        this.listeners.clear();
    }

    private void refreshAudience() {
        if (this.worldObj == null || this.worldObj.playerEntities == null || !this.broadcasting) {
            return;
        }
        int startRange = BigMegaphoneUtil.getStartRange(this.maxRange);
        double startRangeSq = (double) startRange * startRange;
        double stopRangeSq = (double) this.maxRange * this.maxRange;
        double centerX = this.xCoord + 0.5D;
        double centerY = this.yCoord + 0.5D;
        double centerZ = this.zCoord + 0.5D;

        Set<UUID> currentPlayers = new HashSet<UUID>();
        for (Object obj : this.worldObj.playerEntities) {
            if (!(obj instanceof ServerPlayer player)) {
                continue;
            }
            UUID uuid = player.getUniqueIDSilent();
            currentPlayers.add(uuid);
            double distanceSq = player.getDistanceSq(centerX, centerY, centerZ);
            if (this.listeners.contains(uuid)) {
                if (distanceSq > stopRangeSq) {
                    NetworkHandler.sendToClientPlayer(new BigMegaphoneStopPacket(this.xCoord, this.yCoord, this.zCoord, this.sessionId), player);
                    this.listeners.remove(uuid);
                }
                continue;
            }
            if (distanceSq <= startRangeSq) {
                NetworkHandler.sendToClientPlayer(new BigMegaphoneStartPacket(this.xCoord, this.yCoord, this.zCoord,
                        this.sessionId, this.streamUrl, this.displayName, this.maxRange), player);
                this.listeners.add(uuid);
            }
        }

        Iterator<UUID> iterator = this.listeners.iterator();
        while (iterator.hasNext()) {
            if (!currentPlayers.contains(iterator.next())) {
                iterator.remove();
            }
        }
    }

    private ServerPlayer findServerPlayer(UUID uuid) {
        if (uuid == null || this.worldObj == null || this.worldObj.playerEntities == null) {
            return null;
        }
        for (Object obj : this.worldObj.playerEntities) {
            if (obj instanceof ServerPlayer player && uuid.equals(player.getUniqueIDSilent())) {
                return player;
            }
        }
        return null;
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        this.writeToNBT(nbt);
        return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 1, nbt);
    }
}
