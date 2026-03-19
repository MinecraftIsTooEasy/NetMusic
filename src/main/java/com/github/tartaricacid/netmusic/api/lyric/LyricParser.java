package com.github.tartaricacid.netmusic.api.lyric;

import com.github.tartaricacid.netmusic.api.pojo.NetEaseMusicLyric;
import com.google.gson.Gson;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LyricParser {
    private static final Gson GSON = new Gson();
    private static final Pattern TIME_TAG_PATTERN = Pattern.compile("\\[(\\d+):(\\d+)(?:[.:](\\d{1,3}))?]");
    private static final Pattern META_TAG_PATTERN = Pattern.compile("^\\[([A-Za-z]+):(.*)]$");
    private static final int META_LINE_STEP_TICK = 20;

    @Nullable
    public static LyricRecord parseLyric(String json, String songName) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        NetEaseMusicLyric rawLyric = GSON.fromJson(json, NetEaseMusicLyric.class);
        if (rawLyric == null) {
            return null;
        }
        if (rawLyric.code() != 200) {
            return null;
        }
        NetEaseMusicLyric.Lyric original = rawLyric.original();
        if (original == null || StringUtils.isBlank(original.lyric())) {
            return null;
        }
        Int2ObjectSortedMap<String> splitOriginal = splitLyric(original.lyric());
        if (splitOriginal.isEmpty()) {
            return null;
        }

        // 如果第 0 tick 没有歌词，则添加歌曲名称作为第一行歌词
        if (!splitOriginal.containsKey(0)) {
            splitOriginal.put(0, songName);
        }

        NetEaseMusicLyric.Lyric translated = rawLyric.translation();
        if (translated != null && StringUtils.isNotBlank(translated.lyric())) {
            Int2ObjectSortedMap<String> splitTranslated = splitLyric(translated.lyric());
            if (splitTranslated.isEmpty()) {
                return new LyricRecord(splitOriginal);
            }
            // 如果第 0 tick 没有歌词，则添加歌曲名称作为第一行歌词
            if (!splitTranslated.containsKey(0)) {
                splitTranslated.put(0, songName);
            }
            return new LyricRecord(splitOriginal, splitTranslated);
        } else {
            return new LyricRecord(splitOriginal);
        }
    }

    @Nullable
    public static LyricRecord parseLrcText(String originalLrc, @Nullable String translatedLrc, String songName) {
        if (StringUtils.isBlank(originalLrc)) {
            return null;
        }
        Int2ObjectSortedMap<String> splitOriginal = splitLyric(originalLrc);
        if (splitOriginal.isEmpty()) {
            return null;
        }
        if (!splitOriginal.containsKey(0)) {
            splitOriginal.put(0, songName);
        }

        if (StringUtils.isNotBlank(translatedLrc)) {
            Int2ObjectSortedMap<String> splitTranslated = splitLyric(translatedLrc);
            if (!splitTranslated.isEmpty()) {
                if (!splitTranslated.containsKey(0)) {
                    splitTranslated.put(0, songName);
                }
                return new LyricRecord(splitOriginal, splitTranslated);
            }
        }
        return new LyricRecord(splitOriginal);
    }

    private static Int2ObjectSortedMap<String> splitLyric(String lrcContent) {
        Int2ObjectSortedMap<String> lyrics = new Int2ObjectRBTreeMap<>();

        if (StringUtils.isBlank(lrcContent)) {
            return lyrics;
        }

        List<String> introLines = new ArrayList<>();
        String[] lines = lrcContent.split("\n");
        for (String rawLine : lines) {
            String line = StringUtils.trimToEmpty(rawLine);
            if (line.isEmpty()) {
                continue;
            }

            Matcher timeMatcher = TIME_TAG_PATTERN.matcher(line);
            List<Integer> ticks = new ArrayList<>(2);
            while (timeMatcher.find()) {
                ticks.add(toTick(timeMatcher.group(1), timeMatcher.group(2), timeMatcher.group(3)));
            }
            if (!ticks.isEmpty()) {
                String text = TIME_TAG_PATTERN.matcher(line).replaceAll("").trim();
                for (int tick : ticks) {
                    mergeLyricLine(lyrics, tick, text);
                }
                continue;
            }

            String metaLine = normalizeMetaLine(line);
            if (StringUtils.isNotBlank(metaLine)) {
                introLines.add(metaLine);
            }
        }
        addIntroMetaLines(lyrics, introLines);
        return lyrics;
    }

    private static void addIntroMetaLines(Int2ObjectSortedMap<String> lyrics, List<String> introLines) {
        if (introLines == null || introLines.isEmpty()) {
            return;
        }
        int firstTick = lyrics.isEmpty() ? Integer.MAX_VALUE : lyrics.firstIntKey();
        int tick = 0;
        for (String line : introLines) {
            if (StringUtils.isBlank(line)) {
                continue;
            }
            if (firstTick != Integer.MAX_VALUE && tick >= firstTick) {
                mergeLyricLine(lyrics, firstTick, line);
                continue;
            }
            while (lyrics.containsKey(tick) && (firstTick == Integer.MAX_VALUE || tick < firstTick)) {
                tick += META_LINE_STEP_TICK;
            }
            if (firstTick != Integer.MAX_VALUE && tick >= firstTick) {
                mergeLyricLine(lyrics, firstTick, line);
            } else {
                lyrics.put(tick, line);
                tick += META_LINE_STEP_TICK;
            }
        }
    }

    @Nullable
    private static String normalizeMetaLine(String line) {
        Matcher metaMatcher = META_TAG_PATTERN.matcher(line);
        if (metaMatcher.matches()) {
            String key = StringUtils.trimToEmpty(metaMatcher.group(1)).toLowerCase(Locale.ROOT);
            String value = StringUtils.trimToEmpty(metaMatcher.group(2));
            if (StringUtils.isBlank(value) || "offset".equals(key)) {
                return null;
            }
            return formatMetaTag(key, value);
        }
        if (line.startsWith("[") && line.endsWith("]")) {
            return null;
        }
        return line;
    }

    private static String formatMetaTag(String key, String value) {
        if ("ti".equals(key)) {
            return value;
        }
        if (value.indexOf(':') >= 0) {
            return value;
        }
        return key.toUpperCase(Locale.ROOT) + ": " + value;
    }

    private static void mergeLyricLine(Int2ObjectSortedMap<String> lyrics, int tick, String text) {
        int safeTick = Math.max(0, tick);
        String incoming = StringUtils.trimToEmpty(text);
        String existing = lyrics.get(safeTick);
        if (existing == null) {
            if (StringUtils.isBlank(incoming)) {
                return;
            }
            lyrics.put(safeTick, incoming);
            return;
        }
        if (StringUtils.isBlank(incoming)) {
            return;
        }
        if (StringUtils.isBlank(existing)) {
            lyrics.put(safeTick, incoming);
            return;
        }
        if (existing.contains(incoming)) {
            return;
        }
        lyrics.put(safeTick, existing + " / " + incoming);
    }

    private static int toTick(String minuteText, String secondText, @Nullable String fractionText) {
        long minutes;
        long seconds;
        try {
            minutes = Long.parseLong(StringUtils.trimToEmpty(minuteText));
            seconds = Long.parseLong(StringUtils.trimToEmpty(secondText));
        } catch (NumberFormatException e) {
            return 0;
        }

        int millis = 0;
        if (StringUtils.isNotBlank(fractionText)) {
            String fraction = StringUtils.trimToEmpty(fractionText);
            if (fraction.length() == 1) {
                millis = Integer.parseInt(fraction) * 100;
            } else if (fraction.length() == 2) {
                millis = Integer.parseInt(fraction) * 10;
            } else {
                millis = Integer.parseInt(StringUtils.substring(fraction, 0, 3));
            }
        }

        long totalMs = Math.max(0L, (minutes * 60L + seconds) * 1000L + millis);
        long tick = totalMs / 50L;
        return (int) Math.min(Integer.MAX_VALUE, tick);
    }
}
