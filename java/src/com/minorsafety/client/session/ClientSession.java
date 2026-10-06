/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  org.slf4j.Logger
 */
package com.minorsafety.client.session;

import com.minorsafety.PlayerDataStorage;
import com.minorsafety.PlayerRecord;
import com.minorsafety.client.screen.AdultMessageScreen;
import com.minorsafety.client.screen.TimeUpScreen;
import com.minorsafety.client.screen.VerificationScreen;
import com.mojang.logging.LogUtils;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.slf4j.Logger;

public enum ClientSession {
    INSTANCE;

    private static final Logger LOGGER;
    private State state = State.UNVERIFIED;
    private UUID currentPlayerId;
    private String currentName = "";
    private int currentAge = 0;
    private int remainingTicks = 0;
    private boolean locked = false;

    public void onLogin(UUID playerId, String initialName) {
        this.currentPlayerId = playerId;
        this.currentName = initialName != null ? initialName : "";
        this.currentAge = 0;
        this.remainingTicks = 0;
        this.locked = true;
        this.state = State.UNVERIFIED;
        PlayerRecord prev = PlayerDataStorage.INSTANCE.get(playerId);
        if (prev != null && prev.getName() != null && !prev.getName().isEmpty()) {
            this.currentName = prev.getName();
        }
        LOGGER.info("Player logged in, session UNVERIFIED: {}", (Object)playerId);
    }

    public void onLogout() {
        this.state = State.UNVERIFIED;
        this.currentPlayerId = null;
        this.currentName = "";
        this.currentAge = 0;
        this.remainingTicks = 0;
        this.locked = false;
    }

    public String verify(String nameInput, String ageInput) {
        int age;
        if (nameInput == null || nameInput.trim().isEmpty()) {
            return "\u59d3\u540d\u4e0d\u80fd\u4e3a\u7a7a";
        }
        try {
            age = Integer.parseInt(ageInput.trim());
        }
        catch (NumberFormatException e) {
            return "\u8bf7\u8f93\u5165\u6709\u6548\u7684\u5e74\u9f84\uff08\u6570\u5b57\uff09";
        }
        if (age < 1 || age > 2147482617) {
            return "\u5e74\u9f84\u9700\u5728 1~2147482617 \u4e4b\u95f4";
        }
        this.currentName = nameInput.trim();
        this.currentAge = age;
        if (this.currentPlayerId != null) {
            PlayerDataStorage.INSTANCE.put(new PlayerRecord(this.currentPlayerId, this.currentName, age));
        }
        if (this.currentAge >= 18) {
            this.state = State.VERIFIED_ADULT;
            this.remainingTicks = 0;
            this.locked = false;
            Minecraft mc = Minecraft.m_91087_();
            if (mc.m_18695_()) {
                mc.m_91152_((Screen)new AdultMessageScreen(this.currentAge));
            } else {
                mc.execute(() -> mc.m_91152_((Screen)new AdultMessageScreen(this.currentAge)));
            }
        } else {
            this.state = State.PLAYING_MINOR;
            this.remainingTicks = 6000;
            this.locked = false;
        }
        LOGGER.info("Verification success: name={}, age={}, state={}", new Object[]{this.currentName, age, this.state});
        return null;
    }

    public void requestReVerification() {
        Minecraft.m_91087_().execute(() -> Minecraft.m_91087_().m_91152_((Screen)new VerificationScreen()));
    }

    public boolean tick() {
        if (this.state == State.PLAYING_MINOR) {
            if (this.remainingTicks > 0) {
                --this.remainingTicks;
            }
            if (this.remainingTicks <= 0) {
                this.state = State.TIME_UP;
                this.locked = true;
                Minecraft mc = Minecraft.m_91087_();
                if (mc.f_91074_ != null && mc.m_18695_()) {
                    mc.m_91152_((Screen)new TimeUpScreen());
                }
                return true;
            }
        }
        return false;
    }

    public boolean isLocked() {
        return this.locked;
    }

    public State getState() {
        return this.state;
    }

    public String getCurrentName() {
        return this.currentName;
    }

    public int getRemainingTicks() {
        return this.remainingTicks;
    }

    public int getCurrentAge() {
        return this.currentAge;
    }

    static {
        LOGGER = LogUtils.getLogger();
    }

    public static enum State {
        UNVERIFIED,
        VERIFIED_ADULT,
        PLAYING_MINOR,
        TIME_UP;

    }
}
