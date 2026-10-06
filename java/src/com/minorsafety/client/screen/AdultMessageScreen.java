/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.util.FormattedCharSequence
 */
package com.minorsafety.client.screen;

import com.minorsafety.client.msg.AgeMessageProvider;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

public class AdultMessageScreen
extends Screen {
    private static final Component TITLE = Component.m_237113_((String)"\u6210\u5e74\u9a8c\u8bc1\u901a\u8fc7\uff01");
    private static final Component OK = Component.m_237113_((String)"\u597d\u561e\uff0c\u73a9\u53bb\u4e86\uff01");
    private final String message;
    private List<FormattedCharSequence> lines;

    public AdultMessageScreen(int age) {
        super(TITLE);
        this.message = AgeMessageProvider.getMessage(age);
    }

    protected void m_7856_() {
        int cx = this.f_96543_ / 2;
        int cy = this.f_96544_ / 2;
        this.m_142416_((GuiEventListener)Button.m_253074_((Component)OK, btn -> this.m_7379_()).m_252987_(cx - 60, cy + 90, 120, 20).m_253136_());
        if (this.message != null) {
            this.lines = this.f_96547_.m_92923_((FormattedText)Component.m_237113_((String)this.message), this.f_96543_ - 120);
        }
    }

    public void m_88315_(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.m_280273_(graphics);
        int cx = this.f_96543_ / 2;
        int cy = this.f_96544_ / 2 - 60;
        graphics.m_280653_(this.f_96547_, TITLE, cx, cy, 16770634);
        if (this.lines != null && !this.lines.isEmpty()) {
            int y = cy + 30;
            for (FormattedCharSequence line : this.lines) {
                graphics.m_280364_(this.f_96547_, line, cx, y, 0xFFFFFF);
                Objects.requireNonNull(this.f_96547_);
                y += 9 + 2;
            }
        }
        super.m_88315_(graphics, mouseX, mouseY, partialTick);
    }

    public boolean m_7043_() {
        return true;
    }
}
