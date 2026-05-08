package com.github.tartaricacid.netmusic.client.api;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;

public interface IAudioStreamHandler {
    boolean canHandle(URL url);

    AudioInputStream handle(URL url) throws UnsupportedAudioFileException, IOException;

    default int getPriority() {
        return 0;
    }
}
