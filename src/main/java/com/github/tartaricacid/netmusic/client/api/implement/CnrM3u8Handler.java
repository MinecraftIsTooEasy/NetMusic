package com.github.tartaricacid.netmusic.client.api.implement;

import com.github.tartaricacid.netmusic.api.NetEaseMusic;
import com.github.tartaricacid.netmusic.api.NetWorker;
import com.github.tartaricacid.netmusic.util.BigMegaphoneUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CnrM3u8Handler extends M3u8Handler {
    private static final String API = "https://pacc.cnr.cn/ygw/getlivechannel?channelId=%s&111";

    @Override
    public boolean canHandle(URL url) {
        return BigMegaphoneUtil.isCnrPlayUrl(url);
    }

    @Override
    public AudioInputStream handle(URL url) throws UnsupportedAudioFileException, IOException {
        return openM3u8(getM3u8Uri(url));
    }

    private URI getM3u8Uri(URL url) throws IOException {
        String channelId = getQueryParam(url.getQuery(), "channelId");
        if (channelId == null || channelId.trim().isEmpty()) {
            throw new IOException("CNR URL must contain channelId");
        }

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("User-Agent", NetEaseMusic.getUserAgent());
        String text = NetWorker.get(String.format(API, channelId), headers);
        JsonObject root = new JsonParser().parse(text).getAsJsonObject();
        String m3u8Url = root.getAsJsonObject("data")
                .getAsJsonArray("categories").get(0).getAsJsonObject()
                .getAsJsonArray("detail").get(0).getAsJsonObject()
                .getAsJsonArray("other_info11").get(0).getAsJsonObject()
                .get("url").getAsString();
        if (!BigMegaphoneUtil.isValidStreamUrl(m3u8Url)) {
            throw new IOException("Invalid CNR m3u8 URL: " + m3u8Url);
        }
        return URI.create(m3u8Url);
    }

    private static String getQueryParam(String query, String key) {
        if (query == null || query.isEmpty()) {
            return null;
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] split = pair.split("=", 2);
            if (split.length == 2 && key.equals(split[0])) {
                return split[1];
            }
        }
        return null;
    }

    @Override
    public int getPriority() {
        return 200;
    }
}
