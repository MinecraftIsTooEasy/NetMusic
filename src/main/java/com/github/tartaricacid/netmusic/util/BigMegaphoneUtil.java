package com.github.tartaricacid.netmusic.util;

import java.net.URI;
import java.net.URL;
import java.util.Locale;

public final class BigMegaphoneUtil {
    private static final String CNR_PLAY_URL = "https://apicnrapp.cnr.cn/html/play.html";

    private BigMegaphoneUtil() {
    }

    public static boolean isValidStreamUrl(String url) {
        try {
            if (url == null || url.trim().isEmpty()) {
                return false;
            }
            String trimmed = url.trim();
            if (trimmed.startsWith(CNR_PLAY_URL)) {
                return true;
            }
            return isM3u8Url(URI.create(trimmed).toURL());
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isM3u8Url(URL url) {
        if (url == null) {
            return false;
        }
        String protocol = url.getProtocol();
        if (!"http".equalsIgnoreCase(protocol) && !"https".equalsIgnoreCase(protocol)) {
            return false;
        }
        String path = url.getPath();
        return path != null && path.toLowerCase(Locale.ROOT).endsWith(".m3u8");
    }

    public static boolean isCnrPlayUrl(URL url) {
        return url != null && url.toString().startsWith(CNR_PLAY_URL);
    }

    public static int clampRange(int range, int maxRange) {
        int max = Math.max(1, maxRange);
        if (range < 1) {
            return 1;
        }
        return Math.min(range, max);
    }

    public static int getStartRange(int maxRange) {
        return Math.max(1, (int) Math.floor(maxRange * 0.8D));
    }
}
