package com.github.tartaricacid.netmusic.client.audio;

import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.config.GeneralConfig;

import javax.annotation.Nullable;
import java.net.URL;

public class NetMusicSound {
    private final int x;
    private final int y;
    private final int z;
    private final URL songUrl;
    private final int timeSecond;
    private final int startTick;
    private final float hearDistance;
    private final @Nullable LyricRecord lyricRecord;

    public NetMusicSound(int x, int y, int z, URL songUrl, int timeSecond, @Nullable LyricRecord lyricRecord) {
        this(x, y, z, songUrl, timeSecond, lyricRecord, 0);
    }

    public NetMusicSound(int x, int y, int z, URL songUrl, int timeSecond, @Nullable LyricRecord lyricRecord, int startTick) {
        this(x, y, z, songUrl, timeSecond, lyricRecord, startTick, (float) GeneralConfig.MUSIC_PLAYER_HEAR_DISTANCE);
    }

    public NetMusicSound(int x, int y, int z, URL songUrl, int timeSecond, @Nullable LyricRecord lyricRecord, int startTick, float hearDistance) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.songUrl = songUrl;
        this.timeSecond = timeSecond;
        this.startTick = Math.max(0, startTick);
        this.hearDistance = Math.max(1.0F, hearDistance);
        this.lyricRecord = lyricRecord;
    }

    public URL getSongUrl() {
        return this.songUrl;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public int getTimeSecond() {
        return this.timeSecond;
    }

    public int getStartTick() {
        return this.startTick;
    }

    public float getHearDistance() {
        return this.hearDistance;
    }

    public @Nullable LyricRecord getLyricRecord() {
        return this.lyricRecord;
    }
}
