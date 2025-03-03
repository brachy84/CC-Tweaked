// SPDX-FileCopyrightText: 2022 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package dan200.computercraft.client;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.client.sound.SpeakerSound;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.sound.PlayStreamingSourceEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import static dan200.computercraft.client.ForgeClientRegistry.ITEM_FRAME_STATE;

/**
 * Forge-specific dispatch for {@link ClientHooks}.
 */
@EventBusSubscriber(modid = ComputerCraftAPI.MOD_ID, value = Dist.CLIENT)
public final class ForgeClientHooks {
    private ForgeClientHooks() {
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Pre event) {
        ClientHooks.onTick();
    }

    @SubscribeEvent
    public static void onRenderTick(RenderFrameEvent.Pre event) {
        ClientHooks.onRenderTick();
    }

    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) ClientHooks.onWorldUnload();
    }

    @SubscribeEvent
    public static void onDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientHooks.onDisconnect();
    }

    @SubscribeEvent
    public static void drawHighlight(RenderHighlightEvent.Block event) {
        if (ClientHooks.drawHighlight(event.getPoseStack(), event.getMultiBufferSource(), event.getCamera(), event.getTarget())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderText(CustomizeGuiOverlayEvent.DebugText event) {
        ClientHooks.addBlockDebugInfo(event.getRight()::add);
    }

    @SubscribeEvent
    public static void onRenderInHand(RenderHandEvent event) {
        if (ClientHooks.onRenderHeldItem(
            event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(),
            event.getHand(), event.getInterpolatedPitch(), event.getEquipProgress(), event.getSwingProgress(), event.getItemStack()
        )) {
            event.setCanceled(true);
        }
    }


    @SubscribeEvent
    public static void onRenderInFrame(RenderItemInFrameEvent event) {
        var state = event.getItemFrameRenderState().getRenderData(ITEM_FRAME_STATE);
        if (state != null && ClientHooks.onRenderItemFrame(
            event.getPoseStack(), event.getMultiBufferSource(), event.getItemFrameRenderState(), state, event.getPackedLight()
        )) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void playStreaming(PlayStreamingSourceEvent event) {
        if (!(event.getSound() instanceof SpeakerSound sound) || sound.getStream() == null) return;
        ClientHooks.onPlayStreaming(event.getEngine(), event.getChannel(), sound.getStream());
    }
}
