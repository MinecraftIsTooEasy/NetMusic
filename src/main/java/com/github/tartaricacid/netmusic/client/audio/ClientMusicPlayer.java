package com.github.tartaricacid.netmusic.client.audio;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.NetWorker;
import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.client.api.AudioStreamHandlerManager;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import net.minecraft.Minecraft;
import net.minecraft.TileEntity;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public final class ClientMusicPlayer {
    private static final Object LOCK = new Object();

    private static final Map<PlaybackKey, PlaybackState> PLAYBACKS = new HashMap<>();
    private static final Map<PlaybackKey, PendingPlayback> PENDING_PLAYBACKS = new HashMap<>();
    private static volatile int playSession;
    private static final Random RANDOM = new Random();

    private ClientMusicPlayer() {}

    private static final class PlaybackKey {
        private final int x;
        private final int y;
        private final int z;

        private PlaybackKey(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private static PlaybackKey of(int x, int y, int z) {
            return new PlaybackKey(x, y, z);
        }

        private static PlaybackKey of(NetMusicSound sound) {
            return of(sound.getX(), sound.getY(), sound.getZ());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof PlaybackKey other)) {
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

    private static final class PlaybackState {
        private final PlaybackKey key;
        private final NetMusicSound sound;
        private final int session;
        private final int playbackSessionId;
        private final String sourceId;
        private volatile boolean stopRequested;
        private volatile float dynamicVolume = 1.0F;
        private volatile boolean gamePaused;
        private volatile boolean streamStarted;
        private volatile int currentTick;
        private volatile String stopRequestReason = "";
        private Thread thread;

        private PlaybackState(PlaybackKey key, NetMusicSound sound, int session, int playbackSessionId,
                              String sourceId, int startTick) {
            this.key = key;
            this.sound = sound;
            this.session = session;
            this.playbackSessionId = Math.max(0, playbackSessionId);
            this.sourceId = sourceId == null ? "" : sourceId;
            this.currentTick = Math.max(0, startTick);
        }
    }

    private static final class PendingPlayback {
        private final String sourceId;
        private final long untilMs;

        private PendingPlayback(String sourceId, long untilMs) {
            this.sourceId = sourceId == null ? "" : sourceId;
            this.untilMs = untilMs;
        }
    }

    public static void play(NetMusicSound sound) {
        play(sound, null, 0);
    }

    public static void play(NetMusicSound sound, String sourceId) {
        play(sound, sourceId, 0);
    }

    public static void play(NetMusicSound sound, String sourceId, int playbackSessionId) {
        if (sound == null) {
            return;
        }
        PlaybackKey key = PlaybackKey.of(sound);
        PlaybackState previous;
        PlaybackState state;
        synchronized (LOCK) {
            previous = PLAYBACKS.remove(key);
            int session = ++playSession;
            state = new PlaybackState(key, sound, session, playbackSessionId,
                    normalizeSourceId(sourceId, sound), sound.getStartTick());
            state.dynamicVolume = (float) GeneralConfig.MUSIC_PLAYER_VOLUME;
            PLAYBACKS.put(key, state);
            PENDING_PLAYBACKS.remove(key);
            if (GeneralConfig.ENABLE_DEBUG_MODE) {
                NetMusic.LOGGER.info("[NetMusic Debug][Player] start pos=({}, {}, {}) session={} playbackSession={} startTick={} timeSecond={} source={}",
                        sound.getX(), sound.getY(), sound.getZ(), session, state.playbackSessionId,
                        state.currentTick, sound.getTimeSecond(), state.sourceId);
            }
            state.thread = new Thread(() -> stream(state), "NetMusic-Player-" + sound.getX() + "-" + sound.getY() + "-" + sound.getZ());
            state.thread.setDaemon(true);
            state.thread.start();
        }
        stopState(previous, "replace_play");
    }

    public static void stopAll(String reason) {
        List<PlaybackState> states;
        synchronized (LOCK) {
            states = new ArrayList<>(PLAYBACKS.values());
            PLAYBACKS.clear();
            PENDING_PLAYBACKS.clear();
        }
        for (PlaybackState state : states) {
            stopState(state, reason);
        }
    }

    public static void stop() {
        stop("manual");
    }

    public static void stop(String reason) {
        stopAll(reason);
    }

    public static void stopAt(int x, int y, int z, String reason) {
        PlaybackState state;
        synchronized (LOCK) {
            PlaybackKey key = PlaybackKey.of(x, y, z);
            state = PLAYBACKS.remove(key);
            PENDING_PLAYBACKS.remove(key);
        }
        stopState(state, reason);
    }

    private static void stopState(PlaybackState state, String reason) {
        if (state == null) {
            return;
        }
        state.stopRequested = true;
        state.stopRequestReason = reason == null ? "unknown" : reason;
        Thread thread = state.thread;
        if (thread != null) {
            thread.interrupt();
        }
    }

    public static boolean isPlaying() {
        synchronized (LOCK) {
            return !PLAYBACKS.isEmpty();
        }
    }

    public static boolean isPlayingAt(int x, int y, int z) {
        synchronized (LOCK) {
            return PLAYBACKS.containsKey(PlaybackKey.of(x, y, z));
        }
    }

    public static boolean isPlayingAtSource(int x, int y, int z, String sourceId) {
        String normalized = normalizeSourceId(sourceId, null);
        synchronized (LOCK) {
            PlaybackState state = PLAYBACKS.get(PlaybackKey.of(x, y, z));
            return state != null && normalized.equals(state.sourceId);
        }
    }

    public static boolean isPlayingAtSession(int x, int y, int z, int playbackSessionId) {
        int safeSession = Math.max(0, playbackSessionId);
        if (safeSession == 0) {
            return false;
        }
        synchronized (LOCK) {
            PlaybackState state = PLAYBACKS.get(PlaybackKey.of(x, y, z));
            return state != null && safeSession == state.playbackSessionId;
        }
    }

    public static int getCurrentPlaybackSessionAt(int x, int y, int z) {
        synchronized (LOCK) {
            PlaybackState state = PLAYBACKS.get(PlaybackKey.of(x, y, z));
            return state == null ? 0 : state.playbackSessionId;
        }
    }

    public static boolean isPendingAtSource(int x, int y, int z, String sourceId) {
        String normalized = normalizeSourceId(sourceId, null);
        long now = System.currentTimeMillis();
        synchronized (LOCK) {
            PlaybackKey key = PlaybackKey.of(x, y, z);
            PendingPlayback pending = PENDING_PLAYBACKS.get(key);
            if (pending == null) {
                return false;
            }
            if (now >= pending.untilMs) {
                PENDING_PLAYBACKS.remove(key);
                return false;
            }
            return normalized.equals(pending.sourceId);
        }
    }

    public static boolean isPlayingOrPendingAtSource(int x, int y, int z, String sourceId) {
        return isPlayingAtSource(x, y, z, sourceId) || isPendingAtSource(x, y, z, sourceId);
    }

    public static void markPendingPlayback(int x, int y, int z, String sourceId, long ttlMs) {
        String normalized = normalizeSourceId(sourceId, null);
        long ttl = Math.max(500L, ttlMs);
        synchronized (LOCK) {
            PENDING_PLAYBACKS.put(PlaybackKey.of(x, y, z), new PendingPlayback(normalized, System.currentTimeMillis() + ttl));
        }
    }

    public static int getCurrentTickAt(int x, int y, int z) {
        synchronized (LOCK) {
            PlaybackState state = PLAYBACKS.get(PlaybackKey.of(x, y, z));
            return state == null ? -1 : state.currentTick;
        }
    }

    public static boolean syncTickAtSource(int x, int y, int z, String sourceId, int targetTick) {
        return syncServerTickAtSource(x, y, z, sourceId, targetTick);
    }

    public static boolean syncServerTickAt(int x, int y, int z, int targetTick) {
        int safeTick = Math.max(0, targetTick);
        synchronized (LOCK) {
            PlaybackState state = PLAYBACKS.get(PlaybackKey.of(x, y, z));
            if (state == null) {
                return false;
            }
            state.currentTick = safeTick;
            return true;
        }
    }

    public static boolean syncServerTickAtSource(int x, int y, int z, String sourceId, int targetTick) {
        String normalized = normalizeSourceId(sourceId, null);
        int safeTick = Math.max(0, targetTick);
        synchronized (LOCK) {
            PlaybackState state = PLAYBACKS.get(PlaybackKey.of(x, y, z));
            if (state == null) {
                return false;
            }
            if (!normalized.equals(state.sourceId)) {
                return false;
            }
            state.currentTick = safeTick;
            return true;
        }
    }

    /**
     * Runs on the client thread once per tick).
     * Server-authoritative mode: client never advances playback progress locally.
     */
    public static void clientTick() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) {
            stopAllWithDebug("minecraft_null");
            return;
        }
        if (mc.theWorld == null || mc.thePlayer == null) {
            stopAllWithDebug("world_or_player_null");
            return;
        }

        List<PlaybackState> states;
        synchronized (LOCK) {
            states = new ArrayList<>(PLAYBACKS.values());
        }

        if (states.isEmpty()) {
            return;
        }

        if (mc.isGamePaused) {
            for (PlaybackState state : states) {
                state.gamePaused = true;
            }
            return;
        }

        for (PlaybackState state : states) {
            NetMusicSound sound = state.sound;
            state.gamePaused = false;

            TileEntity te = mc.theWorld.getBlockTileEntity(sound.getX(), sound.getY(), sound.getZ());
            boolean isMusicPlayer = te instanceof com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
            boolean isBigMegaphone = te instanceof com.github.tartaricacid.netmusic.tileentity.TileEntityBigMegaphone;
            if (!isMusicPlayer && !isBigMegaphone) {
                stopAtWithDebug(sound.getX(), sound.getY(), sound.getZ(), "source_removed");
                continue;
            }

            double dx = mc.thePlayer.posX - (sound.getX() + 0.5D);
            double dy = mc.thePlayer.posY - (sound.getY() + 0.5D);
            double dz = mc.thePlayer.posZ - (sound.getZ() + 0.5D);
            double distSq = dx * dx + dy * dy + dz * dz;
            float distance = (float) Math.sqrt(distSq);
            float maxHearDistance = Math.max(1.0F, sound.getHearDistance());
            float attenuation = Math.max(0.0F, 1.0F - distance / maxHearDistance);
            state.dynamicVolume = clampVolume((float) GeneralConfig.MUSIC_PLAYER_VOLUME * attenuation);

            if (attenuation > 0.0F && mc.theWorld.getTotalWorldTime() % 8L == 0L) {
                for (int i = 0; i < 2; i++) {
                    mc.theWorld.spawnParticle(net.minecraft.EnumParticle.note,
                            sound.getX() + RANDOM.nextDouble(),
                            sound.getY() + 1.0D + RANDOM.nextDouble(),
                            sound.getZ() + RANDOM.nextDouble(),
                            RANDOM.nextGaussian(), RANDOM.nextGaussian(), RANDOM.nextInt(3));
                }
            }

            LyricRecord lyricRecord = sound.getLyricRecord();
            if (lyricRecord != null) {
                lyricRecord.updateCurrentLine(state.currentTick);
            }
            if (te instanceof com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer musicPlayer) {
                musicPlayer.lyricRecord = lyricRecord;
            }
        }
    }

    private static void stopAllWithDebug(String reason) {
        if (GeneralConfig.ENABLE_DEBUG_MODE) {
            NetMusic.LOGGER.info("[NetMusic Debug][Player] stop reason={}", reason);
        }
        stopAll(reason);
    }

    private static void stopAtWithDebug(int x, int y, int z, String reason) {
        if (GeneralConfig.ENABLE_DEBUG_MODE) {
            NetMusic.LOGGER.info("[NetMusic Debug][Player] stop reason={} pos=({}, {}, {})", reason, x, y, z);
        }
        stopAt(x, y, z, reason);
    }

    private static void stream(PlaybackState state) {
        NetMusicSound sound = state.sound;
        int session = state.session;
        long timeoutAt = System.currentTimeMillis() + Math.max(sound.getTimeSecond(), 1) * 1000L + 3000L;
        AudioInputStream openedStream = openAudioInputStream(sound.getSongUrl());
        if (openedStream == null) {
            removeStateIfCurrent(state);
            return;
        }
        try (AudioInputStream compressed = openedStream) {
            AudioFormat base = compressed.getFormat();
            AudioFormat decoded = chooseDecodedPcmFormat(base);
            if (decoded == null) {
                throw new IllegalArgumentException("Unsupported conversion from " + base + " to PCM");
            }

            try (AudioInputStream pcm = AudioSystem.getAudioInputStream(decoded, compressed)) {
                AudioFormat finalFormat = applyStereoConfig(decoded);
                AudioFormat pcmFormat = pcm.getFormat();
                AudioFormat playbackFormat = AudioSystem.isConversionSupported(finalFormat, pcmFormat) ? finalFormat : pcmFormat;
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, playbackFormat);
                try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
                    try (AudioInputStream finalPcm = AudioSystem.getAudioInputStream(playbackFormat, pcm)) {
                        int targetTick = Math.max(0, sound.getStartTick());
                        skipToStartTick(finalPcm, playbackFormat, targetTick);
                        synchronized (LOCK) {
                            if (PLAYBACKS.get(state.key) == state) {
                                state.currentTick = targetTick;
                            }
                        }
                        line.open(playbackFormat);
                        line.start();
                        synchronized (LOCK) {
                            if (PLAYBACKS.get(state.key) == state) {
                                state.streamStarted = true;
                            }
                        }
                        byte[] buffer = new byte[8192];
                        int read;
                        boolean paused = false;
                        String stopReason = "loop_exit";
                        while (isStateCurrent(state) && !state.stopRequested && !Thread.currentThread().isInterrupted()
                                && System.currentTimeMillis() < timeoutAt) {
                            if (state.gamePaused) {
                                if (!paused) {
                                    line.stop();
                                    paused = true;
                                }
                                try {
                                    Thread.sleep(50L);
                                } catch (InterruptedException interruptedException) {
                                    Thread.currentThread().interrupt();
                                    break;
                                }
                                continue;
                            } else if (paused) {
                                line.start();
                                paused = false;
                            }
                            read = finalPcm.read(buffer, 0, buffer.length);
                            if (read == -1) {
                                stopReason = "eof";
                                break;
                            }
                            if (!isStateCurrent(state) || state.stopRequested || Thread.currentThread().isInterrupted()) {
                                stopReason = state.stopRequested ? "stop_requested" : "session_changed";
                                break;
                            }
                            applyPcmVolume(buffer, read, state.dynamicVolume, playbackFormat.getSampleSizeInBits(), playbackFormat.isBigEndian());
                            line.write(buffer, 0, read);
                        }
                        if (!isStateCurrent(state)) {
                            stopReason = "session_changed";
                        } else if (state.stopRequested) {
                            stopReason = "stop_requested";
                        } else if (Thread.currentThread().isInterrupted()) {
                            stopReason = "thread_interrupted";
                        } else if (System.currentTimeMillis() >= timeoutAt) {
                            stopReason = "timeout";
                        }
                        String stopRequestDetail = state.stopRequestReason;
                        if (GeneralConfig.ENABLE_DEBUG_MODE) {
                            NetMusic.LOGGER.info("[NetMusic Debug][Player] stream_end reason={} stop_request={} pos=({}, {}, {}) session={} playbackSession={} source={} tick={} timeSecond={}",
                                    stopReason, stopRequestDetail, sound.getX(), sound.getY(), sound.getZ(), session,
                                    state.playbackSessionId, state.sourceId, state.currentTick, sound.getTimeSecond());
                        }
                        if ("eof".equals(stopReason) || "timeout".equals(stopReason)) {
                            line.drain();
                        } else {
                            line.stop();
                            line.flush();
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (!state.stopRequested) {
                NetMusic.LOGGER.error("Failed to stream music: {}", sound.getSongUrl(), e);
            }
        } finally {
            removeStateIfCurrent(state);
        }
    }

    private static void removeStateIfCurrent(PlaybackState state) {
        synchronized (LOCK) {
            if (PLAYBACKS.get(state.key) == state) {
                PLAYBACKS.remove(state.key);
            }
        }
        state.streamStarted = false;
    }

    private static boolean isStateCurrent(PlaybackState state) {
        synchronized (LOCK) {
            return PLAYBACKS.get(state.key) == state;
        }
    }

    private static InputStream createSourceStream(URL songUrl) {
        if (songUrl == null) {
            return null;
        }
        try {
            if ("file".equalsIgnoreCase(songUrl.getProtocol())) {
                return new FileInputStream(new java.io.File(songUrl.toURI()));
            }
            if (AudioStreamHandlerManager.canHandle(songUrl)) {
                return AudioStreamHandlerManager.handle(songUrl);
            }
            if (shouldUseDirectHttpStream(songUrl)) {
                return openDirectHttpStream(songUrl);
            }
            return new ChunkedAudioStream(songUrl, NetWorker.getProxyFromConfig());
        } catch (Exception e) {
            NetMusic.LOGGER.error("Failed to open audio source: {}", songUrl, e);
            return null;
        }
    }

    private static AudioInputStream openAudioInputStream(URL songUrl) {
        InputStream source = createSourceStream(songUrl);
        if (source == null) {
            return null;
        }
        try {
            if (source instanceof AudioInputStream audioStream) {
                return audioStream;
            }
            InputStream remote = new MusicBufferedInputStream(source);
            return AudioSystem.getAudioInputStream(prepareAudioStream(remote));
        } catch (Exception e) {
            try {
                source.close();
            } catch (Exception ignored) {
            }
            NetMusic.LOGGER.error("Failed to decode audio source: {}", songUrl, e);
            return null;
        }
    }

    private static InputStream prepareAudioStream(InputStream input) {
        try {
            skipID3(input);
        } catch (Exception ignored) {
            // Best-effort: ID3 skipping is only needed for some MP3 streams.
        }
        return input;
    }

    private static boolean shouldUseDirectHttpStream(URL songUrl) {
        String host = songUrl.getHost();
        if (host == null) {
            return false;
        }
        String lower = host.toLowerCase(Locale.ROOT);
        return lower.contains("qq.com");
    }

    private static InputStream openDirectHttpStream(URL songUrl) throws Exception {
        URLConnection connection = songUrl.openConnection(NetWorker.getProxyFromConfig());
        connection.setUseCaches(false);
        connection.setConnectTimeout(12000);
        // Keep long-lived VIP streams stable while still allowing timeout-based recovery.
        connection.setReadTimeout(45000);
        applyDirectRequestHeaders(connection, songUrl);
        if (GeneralConfig.ENABLE_DEBUG_MODE) {
            NetMusic.LOGGER.info("[NetMusic Debug] Use direct stream for host={} url={}", songUrl.getHost(), songUrl);
        }
        return connection.getInputStream();
    }

    private static void applyDirectRequestHeaders(URLConnection connection, URL songUrl) {
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:115.0) Gecko/20100101 Firefox/115.0");
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Connection", "keep-alive");
        String host = songUrl.getHost() == null ? "" : songUrl.getHost().toLowerCase(Locale.ROOT);
        if (host.contains("qq.com")) {
            connection.setRequestProperty("Referer", "https://y.qq.com/");
            connection.setRequestProperty("Origin", "https://y.qq.com");
            if (GeneralConfig.hasQqVipCookie()) {
                connection.setRequestProperty("Cookie", GeneralConfig.QQ_VIP_COOKIE);
            }
            return;
        }
        if (host.contains("music.163.com") && GeneralConfig.hasNeteaseVipCookie()) {
            connection.setRequestProperty("Cookie", GeneralConfig.NETEASE_VIP_COOKIE);
        }
    }

    private static AudioFormat chooseDecodedPcmFormat(AudioFormat source) {
        int channels = Math.max(1, source.getChannels());
        float sampleRate = source.getSampleRate() > 0 ? source.getSampleRate() : 44100.0F;
        float[] sampleRates = new float[]{sampleRate, 48000.0F, 44100.0F};
        int[] candidateBits = new int[]{16, 24, 32};
        for (float rate : sampleRates) {
            if (rate <= 0 || isAlmostDuplicateRate(rate, sampleRate) && rate != sampleRate) {
                continue;
            }
            for (int bits : candidateBits) {
                if (bits <= 0 || bits % 8 != 0) {
                    continue;
                }
                AudioFormat candidate = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, rate, bits,
                        channels, channels * (bits / 8), rate, false);
                if (AudioSystem.isConversionSupported(candidate, source)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private static boolean isAlmostDuplicateRate(float rateA, float rateB) {
        return Math.abs(rateA - rateB) < 1.0F;
    }

    private static AudioFormat applyStereoConfig(AudioFormat base) {
        // Respect config semantics directly: stereo=true outputs 2 channels, else mono.
        int sampleBits = base.getSampleSizeInBits() > 0 ? base.getSampleSizeInBits() : 16;
        if (com.github.tartaricacid.netmusic.config.GeneralConfig.ENABLE_STEREO) {
            return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, base.getSampleRate(), sampleBits, 2, 2 * Math.max(1, sampleBits / 8), base.getSampleRate(), false);
        }
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, base.getSampleRate(), sampleBits, 1, Math.max(1, sampleBits / 8), base.getSampleRate(), false);
    }

    private static void applyPcmVolume(byte[] buffer, int length, float volume, int sampleBits, boolean bigEndian) {
        if (buffer == null || length <= 1) {
            return;
        }
        float clampedVolume = clampVolume(volume);
        if (Math.abs(clampedVolume - 1.0F) < 1.0e-4F) {
            return;
        }
        if (sampleBits == 16) {
            applyVolume16(buffer, length, clampedVolume, bigEndian);
            return;
        }
        if (sampleBits == 24) {
            applyVolume24(buffer, length, clampedVolume, bigEndian);
        }
    }

    private static void applyVolume16(byte[] buffer, int length, float volume, boolean bigEndian) {
        for (int i = 0; i + 1 < length; i += 2) {
            int sample;
            if (bigEndian) {
                int hi = buffer[i];
                int lo = buffer[i + 1] & 0xFF;
                sample = (short) ((hi << 8) | lo);
            } else {
                int lo = buffer[i] & 0xFF;
                int hi = buffer[i + 1];
                sample = (short) ((hi << 8) | lo);
            }
            int scaled = Math.round(sample * volume);
            if (scaled > Short.MAX_VALUE) {
                scaled = Short.MAX_VALUE;
            } else if (scaled < Short.MIN_VALUE) {
                scaled = Short.MIN_VALUE;
            }
            if (bigEndian) {
                buffer[i] = (byte) ((scaled >> 8) & 0xFF);
                buffer[i + 1] = (byte) (scaled & 0xFF);
            } else {
                buffer[i] = (byte) (scaled & 0xFF);
                buffer[i + 1] = (byte) ((scaled >> 8) & 0xFF);
            }
        }
    }

    private static void applyVolume24(byte[] buffer, int length, float volume, boolean bigEndian) {
        for (int i = 0; i + 2 < length; i += 3) {
            int sample;
            if (bigEndian) {
                sample = ((buffer[i] & 0xFF) << 16)
                        | ((buffer[i + 1] & 0xFF) << 8)
                        | (buffer[i + 2] & 0xFF);
            } else {
                sample = (buffer[i] & 0xFF)
                        | ((buffer[i + 1] & 0xFF) << 8)
                        | ((buffer[i + 2] & 0xFF) << 16);
            }
            if ((sample & 0x800000) != 0) {
                sample |= 0xFF000000;
            }

            int scaled = Math.round(sample * volume);
            if (scaled > 0x7FFFFF) {
                scaled = 0x7FFFFF;
            } else if (scaled < -0x800000) {
                scaled = -0x800000;
            }

            if (bigEndian) {
                buffer[i] = (byte) ((scaled >> 16) & 0xFF);
                buffer[i + 1] = (byte) ((scaled >> 8) & 0xFF);
                buffer[i + 2] = (byte) (scaled & 0xFF);
            } else {
                buffer[i] = (byte) (scaled & 0xFF);
                buffer[i + 1] = (byte) ((scaled >> 8) & 0xFF);
                buffer[i + 2] = (byte) ((scaled >> 16) & 0xFF);
            }
        }
    }

    private static float clampVolume(float volume) {
        if (volume < 0.0F) {
            return 0.0F;
        }
        if (volume > 2.0F) {
            return 2.0F;
        }
        return volume;
    }

    private static String normalizeSourceId(String sourceId, NetMusicSound sound) {
        String value = sourceId;
        if ((value == null || value.trim().isEmpty()) && sound != null && sound.getSongUrl() != null) {
            value = sound.getSongUrl().toString();
        }
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static void skipToStartTick(AudioInputStream stream, AudioFormat format, int startTick) throws java.io.IOException {
        if (stream == null || format == null || startTick <= 0) {
            return;
        }
        int frameSize = Math.max(1, format.getFrameSize());
        float frameRate = format.getFrameRate() > 0 ? format.getFrameRate() : format.getSampleRate();
        if (frameRate <= 0) {
            return;
        }
        double seconds = startTick / 20.0D;
        long targetBytes = (long) Math.floor(seconds * frameRate * frameSize);
        if (targetBytes <= 0L) {
            return;
        }
        try {
            skipFully(stream, targetBytes);
        } catch (RuntimeException runtimeException) {
            // Some decoders (notably certain FLAC SPI chains) don't support stable skip semantics.
            // Don't abort playback; just start from the beginning instead of crashing stream setup.
            NetMusic.LOGGER.warn("Failed to seek audio stream to start tick {}, fallback to start-from-beginning", startTick, runtimeException);
        }
    }

    private static void skipFully(InputStream input, long bytes) throws java.io.IOException {
        long remaining = bytes;
        byte[] discard = new byte[4096];
        while (remaining > 0) {
            long skipped;
            try {
                skipped = input.skip(remaining);
            } catch (RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (skipped > 0) {
                remaining -= skipped;
                continue;
            }
            int read;
            try {
                read = input.read(discard, 0, (int) Math.min(discard.length, remaining));
            } catch (RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (read <= 0) {
                break;
            }
            remaining -= read;
        }
    }

    private static void skipID3(InputStream inputStream) throws java.io.IOException {
        if (!inputStream.markSupported()) {
            return;
        }
        inputStream.mark(10);
        byte[] header = new byte[10];
        int read = inputStream.read(header, 0, 10);
        if (read < 10) {
            inputStream.reset();
            return;
        }
        if (header[0] == 'I' && header[1] == 'D' && header[2] == '3') {
            int size = ((header[6] & 0x7F) << 21)
                    | ((header[7] & 0x7F) << 14)
                    | ((header[8] & 0x7F) << 7)
                    | (header[9] & 0x7F);
            int skipped = 0;
            int skip;
            do {
                skip = (int) inputStream.skip(size - skipped);
                if (skip != 0) {
                    skipped += skip;
                }
            } while (skipped < size && skip != 0);
        } else {
            inputStream.reset();
        }
    }
}
