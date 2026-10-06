/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderGuiEvent$Post
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.minorsafety.client;

import com.minorsafety.client.session.ClientSession;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value={Dist.CLIENT})
public class HudHandler {
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.m_91087_();
        if (mc.f_91074_ == null || mc.f_91080_ != null) {
            return;
        }
        ClientSession session = ClientSession.INSTANCE;
        if (session.getState() == ClientSession.State.PLAYING_MINOR) {
            String text = "\u672a\u6210\u5e74\u4fdd\u62a4 - \u5269\u4f59\u6e38\u73a9\u65f6\u95f4: " + HudHandler.format(session.getRemainingTicks());
            event.getGuiGraphics().m_280488_(mc.f_91062_, text, 8, 8, 0xFFFFFF);
        } else if (session.isLocked()) {
            String text = session.getState() == ClientSession.State.TIME_UP ? "\u672a\u6210\u5e74\u4fdd\u62a4 - \u6e38\u73a9\u65f6\u95f4\u5df2\u7ed3\u675f\uff0c\u7269\u54c1\u680f\u5df2\u9501\u5b9a" : "\u672a\u6210\u5e74\u4fdd\u62a4 - \u8bf7\u5148\u5b8c\u6210\u8eab\u4efd\u9a8c\u8bc1";
            event.getGuiGraphics().m_280488_(mc.f_91062_, text, 8, 8, 0xFF5555);
        }
    }

    private static String format(int ticks) {
        int totalSec = Math.max(0, ticks / 20);
        return String.format("%d:%02d", totalSec / 60, totalSec % 60);
    }
}
