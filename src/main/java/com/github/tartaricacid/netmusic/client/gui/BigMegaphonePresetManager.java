package com.github.tartaricacid.netmusic.client.gui;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.util.BigMegaphoneUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BigMegaphonePresetManager {
    private static final String RESOURCE_PATH = "assets/netmusic/broadcasting_presets.json";
    private static final Gson GSON = new Gson();
    private static final Type PRESET_LIST_TYPE = new TypeToken<List<PresetStation>>() {}.getType();
    private static final List<PresetStation> STATIONS = loadStations();

    private BigMegaphonePresetManager() {
    }

    public static List<PresetStation> getStations() {
        return STATIONS;
    }

    private static List<PresetStation> loadStations() {
        InputStream stream = BigMegaphonePresetManager.class.getClassLoader().getResourceAsStream(RESOURCE_PATH);
        if (stream == null) {
            return Collections.emptyList();
        }

        try (InputStream input = stream;
             InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            List<PresetStation> raw = GSON.fromJson(reader, PRESET_LIST_TYPE);
            if (raw == null || raw.isEmpty()) {
                return Collections.emptyList();
            }
            List<PresetStation> stations = new ArrayList<PresetStation>();
            for (PresetStation preset : raw) {
                if (preset == null) {
                    continue;
                }
                String name = preset.name == null ? "" : preset.name.trim();
                String url = preset.url == null ? "" : preset.url.trim();
                if (name.isEmpty() || !BigMegaphoneUtil.isValidStreamUrl(url)) {
                    continue;
                }
                stations.add(new PresetStation(name, url));
            }
            return Collections.unmodifiableList(stations);
        } catch (Exception e) {
            NetMusic.LOGGER.warn("Failed to load big megaphone presets from {}", RESOURCE_PATH, e);
            return Collections.emptyList();
        }
    }

    public static final class PresetStation {
        public final String name;
        public final String url;

        public PresetStation(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }
}
