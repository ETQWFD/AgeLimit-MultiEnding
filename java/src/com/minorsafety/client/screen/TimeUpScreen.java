/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 */
package com.minorsafety.client.screen;

import com.minorsafety.client.session.ClientSession;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TimeUpScreen
extends Screen {
    private static final Component TITLE = Component.m_237113_((String)"\u672a\u6210\u5e74\u4fdd\u62a4 - \u6e38\u73a9\u65f6\u95f4\u5df2\u7ed3\u675f");
    private static final Component MESSAGE = Component.m_237113_((String)"\u4eca\u65e5\u6e38\u73a9\u65f6\u95f4\u5df2\u8fbe\u5230\u9650\u5236\uff0c\u7269\u54c1\u680f\u7b49\u5df2\u88ab\u9501\u5b9a\u3002");
    private static final Component GROWN_UP = Component.m_237113_((String)"\u6211\u957f\u5927\u4e86");

    public TimeUpScreen() {
        super(TITLE);
    }

    protected void m_7856_() {
        int cx = this.f_96543_ / 2;
        int cy = this.f_96544_ / 2;
        this.m_142416_((GuiEventListener)Button.m_253074_((Component)GROWN_UP, btn -> ClientSession.INSTANCE.requestReVerification()).m_252987_(cx - 60, cy + 30, 120, 20).m_253136_());
    }

    public void m_88315_(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.m_280273_(graphics);
        int cx = this.f_96543_ / 2;
        int cy = this.f_96544_ / 2;
        graphics.m_280653_(this.f_96547_, TITLE, cx, cy - 40, 0xFFFFFF);
        graphics.m_280653_(this.f_96547_, MESSAGE, cx, cy - 20, 0xA0A0A0);
        super.m_88315_(graphics, mouseX, mouseY, partialTick);
    }

    public boolean m_7043_() {
        return true;
    }
}
