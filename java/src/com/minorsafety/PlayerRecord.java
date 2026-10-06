/*
 * Decompiled with CFR 0.152.
 */
package com.minorsafety;

import java.util.UUID;

public class PlayerRecord {
    private UUID uuid;
    private String name;
    private int age;
    private long lastVerifiedAt;

    public PlayerRecord() {
    }

    public PlayerRecord(UUID uuid, String name, int age) {
        this.uuid = uuid;
        this.name = name;
        this.age = age;
        this.lastVerifiedAt = System.currentTimeMillis();
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return this.age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public long getLastVerifiedAt() {
        return this.lastVerifiedAt;
    }

    public void setLastVerifiedAt(long lastVerifiedAt) {
        this.lastVerifiedAt = lastVerifiedAt;
    }

    public boolean isMinor() {
        return this.age < 18;
    }
}
