/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.player.Input
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.MovementInputUpdateEvent
 *  net.minecraftforge.client.event.ScreenEvent$Opening
 *  net.minecraftforge.event.entity.living.LivingAttackEvent
 *  net.minecraftforge.event.entity.player.AttackEntityEvent
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$LeftClickBlock
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$RightClickBlock
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$RightClickItem
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.minorsafety.client;

import com.minorsafety.client.screen.TimeUpScreen;
import com.minorsafety.client.screen.VerificationScreen;
import com.minorsafety.client.session.ClientSession;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.Input;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value={Dist.CLIENT})
public class GameplayLockHandler {
    private static boolean isAllowedScreen(Screen screen) {
        return screen instanceof VerificationScreen || screen instanceof TimeUpScreen;
    }

    private static boolean shouldBlock(Player player) {
        ClientSession session = ClientSession.INSTANCE;
        Minecraft mc = Minecraft.m_91087_();
        if (player != null && mc.f_91074_ != null && player != mc.f_91074_) {
            return false;
        }
        return session.isLocked() || session.getState() == ClientSession.State.UNVERIFIED || session.getState() == ClientSession.State.TIME_UP;
    }

    @SubscribeEvent
    public static void onMovement(MovementInputUpdateEvent event) {
        if (!Minecraft.m_91087_().m_18695_()) {
            return;
        }
        if (!GameplayLockHandler.shouldBlock(event.getEntity())) {
            return;
        }
        Input input = event.getInput();
        if (input != null) {
            input.f_108567_ = 0.0f;
            input.f_108566_ = 0.0f;
            input.f_108568_ = false;
            input.f_108569_ = false;
            input.f_108570_ = false;
            input.f_108571_ = false;
            input.f_108572_ = false;
            input.f_108573_ = false;
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (GameplayLockHandler.shouldBlock(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (GameplayLockHandler.shouldBlock(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (GameplayLockHandler.shouldBlock(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (GameplayLockHandler.shouldBlock(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        Player p;
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof Player && GameplayLockHandler.shouldBlock(p = (Player)livingEntity)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        Minecraft mc = Minecraft.m_91087_();
        if (mc.f_91074_ == null) {
            return;
        }
        ClientSession session = ClientSession.INSTANCE;
        if (session.getState() == ClientSession.State.UNVERIFIED) {
            if (event.getNewScreen() instanceof VerificationScreen) {
                return;
            }
            event.setNewScreen(null);
            return;
        }
        if (session.getState() == ClientSession.State.TIME_UP) {
            if (GameplayLockHandler.isAllowedScreen(event.getNewScreen())) {
                return;
            }
            event.setNewScreen(null);
        }
    }
}
