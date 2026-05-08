package com.github.tartaricacid.netmusic.client.api;

import com.github.tartaricacid.netmusic.client.api.implement.CnrM3u8Handler;
import com.github.tartaricacid.netmusic.client.api.implement.M3u8Handler;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class AudioStreamHandlerManager {

    private static final List<IAudioStreamHandler> HANDLERS = new ArrayList<>();

    static {
        registerHandler(new CnrM3u8Handler());
        registerHandler(new M3u8Handler());
        HANDLERS.sort((left, right) -> Integer.compare(right.getPriority(), left.getPriority()));
    }

    private static void registerHandler(IAudioStreamHandler handler) {
        HANDLERS.add(handler);
    }

    public static boolean canHandle(URL url) {
        for (IAudioStreamHandler handler : HANDLERS) {
            if (handler.canHandle(url)) {
                return true;
            }
        }
        return false;
    }

    public static AudioInputStream handle(URL url) throws UnsupportedAudioFileException, IOException {
        for (IAudioStreamHandler handler : HANDLERS) {
            if (handler.canHandle(url)) {
                return handler.handle(url);
            }
        }
        throw new UnsupportedAudioFileException("No stream handler found for URL: " + url);
    }
}
