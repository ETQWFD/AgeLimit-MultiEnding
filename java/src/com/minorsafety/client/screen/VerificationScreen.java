/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 */
package com.minorsafety.client.screen;

import com.minorsafety.client.session.ClientSession;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

public class VerificationScreen
extends Screen {
    private static final Component TITLE = Component.m_237113_((String)"\u672a\u6210\u5e74\u4eba\u4fdd\u62a4 - \u8eab\u4efd\u9a8c\u8bc1");
    private static final Component NAME_LABEL = Component.m_237113_((String)"\u8bf7\u8f93\u5165\u59d3\u540d:");
    private static final Component AGE_LABEL = Component.m_237113_((String)"\u8bf7\u8f93\u5165\u5e74\u9f84:");
    private static final Component CONFIRM = Component.m_237113_((String)"\u786e\u8ba4");
    private EditBox nameBox;
    private EditBox ageBox;
    private String errorMessage = "";

    public VerificationScreen() {
        super(TITLE);
    }

    protected void m_7856_() {
        int cx = this.f_96543_ / 2;
        int baseY = this.f_96544_ / 2 - 40;
        String defaultName = ClientSession.INSTANCE.getCurrentName();
        this.nameBox = new EditBox(this.f_96547_, cx - 90, baseY, 180, 20, NAME_LABEL);
        this.nameBox.m_94199_(32);
        if (defaultName != null && !defaultName.isEmpty()) {
            this.nameBox.m_94144_(defaultName);
        }
        this.m_142416_((GuiEventListener)this.nameBox);
        this.ageBox = new EditBox(this.f_96547_, cx - 90, baseY + 32, 180, 20, AGE_LABEL);
        this.ageBox.m_94199_(10);
        this.ageBox.m_94153_(s -> s.matches("\\d{0,10}"));
        this.m_142416_((GuiEventListener)this.ageBox);
        this.m_142416_((GuiEventListener)Button.m_253074_((Component)CONFIRM, btn -> this.onSubmit()).m_252987_(cx - 60, baseY + 68, 120, 20).m_253136_());
        this.m_264313_((GuiEventListener)this.nameBox);
    }

    private void onSubmit() {
        String err = ClientSession.INSTANCE.verify(this.nameBox.m_94155_(), this.ageBox.m_94155_());
        if (err != null) {
            this.errorMessage = err;
            return;
        }
        if (ClientSession.INSTANCE.getCurrentAge() < 18) {
            this.m_7379_();
        }
    }

    public void m_88315_(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.m_280273_(graphics);
        int cx = this.f_96543_ / 2;
        int baseY = this.f_96544_ / 2 - 60;
        graphics.m_280653_(this.f_96547_, TITLE, cx, baseY, 0xFFFFFF);
        int nameBoxY = this.f_96544_ / 2 - 40;
        int ageBoxY = nameBoxY + 32;
        int labelVOffset = 6;
        int nameLabelX = cx - 90 - this.f_96547_.m_92852_((FormattedText)NAME_LABEL) - 6;
        graphics.m_280430_(this.f_96547_, NAME_LABEL, nameLabelX, nameBoxY + labelVOffset, 0xA0A0A0);
        int ageLabelX = cx - 90 - this.f_96547_.m_92852_((FormattedText)AGE_LABEL) - 6;
        graphics.m_280430_(this.f_96547_, AGE_LABEL, ageLabelX, ageBoxY + labelVOffset, 0xA0A0A0);
        if (!this.errorMessage.isEmpty()) {
            graphics.m_280653_(this.f_96547_, (Component)Component.m_237113_((String)this.errorMessage), cx, baseY + 110, 0xFF5555);
        }
        super.m_88315_(graphics, mouseX, mouseY, partialTick);
    }

    public boolean m_7043_() {
        return true;
    }
}
