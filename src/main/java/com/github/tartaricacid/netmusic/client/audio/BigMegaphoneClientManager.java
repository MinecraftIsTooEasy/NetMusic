package com.github.tartaricacid.netmusic.client.audio;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.tileentity.TileEntityBigMegaphone;
import net.minecraft.Minecraft;
import net.minecraft.StatCollector;
import net.minecraft.TileEntity;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class BigMegaphoneClientManager {
    private static final int STREAM_SECONDS = 31536000;
    private static final long RETRY_INTERVAL_TICKS = 60L;
    private static final Map<Key, TrackedBroadcast> TRACKED = new HashMap<Key, TrackedBroadcast>();

    private BigMegaphoneClientManager() {
    }

    public static void start(int x, int y, int z, long sessionId, String url, String name, int range) {
        if (StringUtils.isBlank(url)) {
            return;
        }
        Key key = new Key(x, y, z);
        TrackedBroadcast tracked = TRACKED.get(key);
        if (tracked != null) {
            if (tracked.sessionId > sessionId) {
                return;
            }
            if (tracked.sessionId == sessionId
                    && tracked.url.equals(url)
                    && tracked.name.equals(name)
                    && tracked.range == range
                    && ClientMusicPlayer.isPlayingAtSource(x, y, z, tracked.sourceId)) {
                return;
            }
            ClientMusicPlayer.stopAt(x, y, z, "big_megaphone_restart");
        }

        TrackedBroadcast next = new TrackedBroadcast(x, y, z, sessionId, url, name, Math.max(1, range));
        TRACKED.put(key, next);
        play(next);
    }

    public static void stop(int x, int y, int z, long sessionId) {
        Key key = new Key(x, y, z);
        TrackedBroadcast tracked = TRACKED.get(key);
        if (tracked == null || tracked.sessionId > sessionId) {
            return;
        }
        ClientMusicPlayer.stopAt(x, y, z, "big_megaphone_stop");
        TRACKED.remove(key);
    }

    public static void clientTick() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.theWorld == null || mc.thePlayer == null) {
            clearAll();
            return;
        }

        long now = mc.theWorld.getTotalWorldTime();
        Iterator<Map.Entry<Key, TrackedBroadcast>> iterator = TRACKED.entrySet().iterator();
        while (iterator.hasNext()) {
            TrackedBroadcast tracked = iterator.next().getValue();
            TileEntity tile = mc.theWorld.getBlockTileEntity(tracked.x, tracked.y, tracked.z);
            if (!(tile instanceof TileEntityBigMegaphone megaphone) || !megaphone.isBroadcasting()) {
                ClientMusicPlayer.stopAt(tracked.x, tracked.y, tracked.z, "big_megaphone_removed");
                iterator.remove();
                continue;
            }
            if (!isPlayerInRange(mc, tracked)) {
                ClientMusicPlayer.stopAt(tracked.x, tracked.y, tracked.z, "big_megaphone_out_of_range");
                continue;
            }
            if (!ClientMusicPlayer.isPlayingAtSource(tracked.x, tracked.y, tracked.z, tracked.sourceId)
                    && now >= tracked.nextRetryTick) {
                tracked.nextRetryTick = now + RETRY_INTERVAL_TICKS;
                play(tracked);
            }
        }
    }

    public static void clearAll() {
        for (TrackedBroadcast tracked : TRACKED.values()) {
            ClientMusicPlayer.stopAt(tracked.x, tracked.y, tracked.z, "big_megaphone_clear");
        }
        TRACKED.clear();
    }

    private static void play(TrackedBroadcast tracked) {
        try {
            URL streamUrl = new URL(tracked.url);
            ClientMusicPlayer.play(new NetMusicSound(tracked.x, tracked.y, tracked.z, streamUrl,
                            STREAM_SECONDS, null, 0, tracked.range),
                    tracked.sourceId, toPlaybackSessionId(tracked.sessionId));
            showRecordMessage(tracked.name);
        } catch (Exception e) {
            NetMusic.LOGGER.error("Failed to play big megaphone stream: {}", tracked.url, e);
            showRecordMessage(StatCollector.translateToLocal("message.netmusic.big_megaphone.play_error"));
        }
    }

    private static boolean isPlayerInRange(Minecraft mc, TrackedBroadcast tracked) {
        if (mc == null || mc.thePlayer == null) {
            return false;
        }
        double dx = mc.thePlayer.posX - (tracked.x + 0.5D);
        double dy = mc.thePlayer.posY - (tracked.y + 0.5D);
        double dz = mc.thePlayer.posZ - (tracked.z + 0.5D);
        return dx * dx + dy * dy + dz * dz <= (double) tracked.range * tracked.range;
    }

    private static void showRecordMessage(String text) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft != null && minecraft.ingameGUI != null && StringUtils.isNotBlank(text)) {
            minecraft.ingameGUI.setRecordPlayingMessage(text);
        }
    }

    private static int toPlaybackSessionId(long sessionId) {
        long safe = Math.max(1L, sessionId);
        return (int) (safe % Integer.MAX_VALUE);
    }

    private static final class TrackedBroadcast {
        private final int x;
        private final int y;
        private final int z;
        private final long sessionId;
        private final String url;
        private final String name;
        private final int range;
        private final String sourceId;
        private long nextRetryTick;

        private TrackedBroadcast(int x, int y, int z, long sessionId, String url, String name, int range) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.sessionId = sessionId;
            this.url = url == null ? "" : url.trim();
            this.name = name == null ? "" : name.trim();
            this.range = range;
            this.sourceId = "big_megaphone:" + x + "," + y + "," + z + ":" + sessionId + ":" + this.url;
        }
    }

    private static final class Key {
        private final int x;
        private final int y;
        private final int z;

        private Key(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Key other)) {
                return false;
            }
            return this.x == other.x && this.y == other.y && this.z == other.z;
        }

        @Override
        public int hashCode() {
            int result = Integer.hashCode(this.x);
            result = 31 * result + Integer.hashCode(this.y);
            result = 31 * result + Integer.hashCode(this.z);
            return result;
        }
    }
}
