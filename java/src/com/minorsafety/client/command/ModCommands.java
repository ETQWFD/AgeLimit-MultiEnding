/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.commands.Commands
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RegisterClientCommandsEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package com.minorsafety.client.command;

import com.minorsafety.client.screen.VerificationScreen;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.Commands;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value={Dist.CLIENT}, modid="minorsafety", bus=Mod.EventBusSubscriber.Bus.FORGE)
public class ModCommands {
    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher dispatcher = event.getDispatcher();
        dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_((String)"verify").executes(ctx -> {
            ModCommands.openVerificationScreen();
            return 1;
        }));
        dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_((String)"minorsafety").executes(ctx -> {
            ModCommands.openVerificationScreen();
            return 1;
        }));
        dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_((String)"msverify").executes(ctx -> {
            ModCommands.openVerificationScreen();
            return 1;
        }));
    }

    private static void openVerificationScreen() {
        Minecraft mc = Minecraft.m_91087_();
        if (mc.f_91074_ == null) {
            return;
        }
        mc.execute(() -> {
            if (Minecraft.m_91087_().f_91074_ != null) {
                Minecraft.m_91087_().m_91152_((Screen)new VerificationScreen());
            }
        });
    }
}
