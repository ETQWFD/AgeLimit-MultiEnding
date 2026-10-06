/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.minecraftforge.fml.common.Mod
 *  org.slf4j.Logger
 */
package com.minorsafety;

import com.minorsafety.PlayerDataStorage;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(value="minorsafety")
public class MinorSafetyMod {
    public static final String MODID = "minorsafety";
    public static final int ADULT_AGE = 18;
    public static final int MIN_AGE = 1;
    public static final int MAX_AGE = 2147482617;
    public static final int MINOR_PLAY_TICKS = 6000;
    public static final Logger LOGGER = LogUtils.getLogger();

    public MinorSafetyMod() {
        LOGGER.info("Loading {} mod", (Object)MODID);
        PlayerDataStorage.INSTANCE.initPath();
    }
}
