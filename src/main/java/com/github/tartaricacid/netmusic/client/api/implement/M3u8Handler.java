package com.github.tartaricacid.netmusic.client.api.implement;

import com.github.tartaricacid.netmusic.api.NetEaseMusic;
import com.github.tartaricacid.netmusic.client.api.IAudioStreamHandler;
import com.github.tartaricacid.netmusic.util.BigMegaphoneUtil;
import net.sourceforge.jaad.m3u8.M3U8InputStream;
import net.sourceforge.jaad.spi.javasound.TSAudioFileReader;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.function.Function;
import java.util.function.Supplier;

public class M3u8Handler implements IAudioStreamHandler {
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .version(HttpClient.Version.HTTP_1_1)
            .build();
    private static final Duration M3U8_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration TS_TIMEOUT = Duration.ofSeconds(10);

    @Override
    public boolean canHandle(URL url) {
        return BigMegaphoneUtil.isM3u8Url(url);
    }

    @Override
    public AudioInputStream handle(URL url) throws UnsupportedAudioFileException, IOException {
        return openM3u8(URI.create(url.toString()));
    }

    protected AudioInputStream openM3u8(URI uri) throws UnsupportedAudioFileException, IOException {
        Supplier<HttpRequest> playlistRequest = new Supplier<HttpRequest>() {
            @Override
            public HttpRequest get() {
                return HttpRequest.newBuilder(uri)
                        .timeout(M3U8_TIMEOUT)
                        .header("User-Agent", NetEaseMusic.getUserAgent())
                        .GET()
                        .build();
            }
        };
        Function<URI, HttpRequest> tsSegmentRequest = new Function<URI, HttpRequest>() {
            @Override
            public HttpRequest apply(URI tsUri) {
                return HttpRequest.newBuilder(tsUri)
                        .timeout(TS_TIMEOUT)
                        .header("User-Agent", NetEaseMusic.getUserAgent())
                        .GET()
                        .build();
            }
        };
        M3U8InputStream m3u8InputStream = new M3U8InputStream(HTTP_CLIENT, playlistRequest, tsSegmentRequest);
        BufferedInputStream buffered = new BufferedInputStream(m3u8InputStream, 5 * 1024 * 1024);
        return new TSAudioFileReader().getAudioInputStream(buffered);
    }

    @Override
    public int getPriority() {
        return 100;
    }
}
