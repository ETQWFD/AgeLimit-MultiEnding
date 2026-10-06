/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.reflect.TypeToken
 *  com.mojang.logging.LogUtils
 *  net.minecraftforge.fml.loading.FMLPaths
 *  org.slf4j.Logger
 */
package com.minorsafety;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.minorsafety.PlayerRecord;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

public enum PlayerDataStorage {
    INSTANCE;

    private static final Logger LOGGER;
    private static final Gson GSON;
    private static final Type MAP_TYPE;
    private Path filePath;
    private final Map<String, PlayerRecord> records = new ConcurrentHashMap<String, PlayerRecord>();

    public void initPath() {
        if (this.filePath != null) {
            return;
        }
        this.filePath = FMLPaths.CONFIGDIR.get().resolve("minorsafety").resolve("players.json");
        this.load();
    }

    public Map<String, PlayerRecord> getRecords() {
        return this.records;
    }

    public PlayerRecord get(UUID uuid) {
        return this.records.get(uuid.toString());
    }

    public void put(PlayerRecord record) {
        this.records.put(record.getUuid().toString(), record);
        this.save();
    }

    private void load() {
        if (this.filePath == null || !Files.exists(this.filePath, new LinkOption[0])) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(this.filePath, StandardCharsets.UTF_8);){
            Map loaded = (Map)GSON.fromJson((Reader)reader, MAP_TYPE);
            if (loaded != null) {
                this.records.clear();
                this.records.putAll(loaded);
            }
        }
        catch (IOException e) {
            LOGGER.error("Failed to load players.json", (Throwable)e);
        }
    }

    private void save() {
        if (this.filePath == null) {
            return;
        }
        try {
            Files.createDirectories(this.filePath.getParent(), new FileAttribute[0]);
            try (BufferedWriter writer = Files.newBufferedWriter(this.filePath, StandardCharsets.UTF_8, new OpenOption[0]);){
                GSON.toJson(this.records, MAP_TYPE, (Appendable)writer);
            }
        }
        catch (IOException e) {
            LOGGER.error("Failed to save players.json", (Throwable)e);
        }
    }

    static {
        LOGGER = LogUtils.getLogger();
        GSON = new GsonBuilder().setPrettyPrinting().create();
        MAP_TYPE = new TypeToken<Map<String, PlayerRecord>>(){}.getType();
    }
}
