// SPDX-FileCopyrightText: 2022 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package dan200.computercraft.client;

import com.google.common.reflect.TypeToken;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.client.turtle.RegisterTurtleModellersEvent;
import dan200.computercraft.client.model.ExtraModels;
import dan200.computercraft.client.render.ExtendedItemFrameRenderState;
import dan200.computercraft.client.turtle.TurtleUpgradeModellers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;


/**
 * Registers textures and models for items.
 */
@EventBusSubscriber(modid = ComputerCraftAPI.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ForgeClientRegistry {
    static final ContextKey<ExtendedItemFrameRenderState> ITEM_FRAME_STATE = new ContextKey<>(ResourceLocation.fromNamespaceAndPath(ComputerCraftAPI.MOD_ID, "item_frame"));

    private static final Object lock = new Object();
    private static boolean gatheredModellers = false;

    private ForgeClientRegistry() {
    }

    /**
     * Turtle upgrade modellers must be loaded before we gather additional models.
     * <p>
     * Unfortunately, due to the nature of parallel mod loading (resource loading and mod setup events are fired in
     * parallel), there's no way to guarantee this using existing events. Instead, we piggyback off
     * {@link ModelEvent.RegisterAdditional}, registering models the first time the event is fired.
     */
    private static void gatherModellers() {
        if (gatheredModellers) return;
        synchronized (lock) {
            if (gatheredModellers) return;

            gatheredModellers = true;
            ModLoader.postEvent(new RegisterTurtleModellersEvent(TurtleUpgradeModellers::register));
        }
    }

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        gatherModellers();
        var extraModels = ExtraModels.loadAll(Minecraft.getInstance().getResourceManager());
        ClientRegistry.registerExtraModels(event::register, extraModels);
    }

    @SubscribeEvent
    public static void onTurtleModellers(RegisterTurtleModellersEvent event) {
        ClientRegistry.registerTurtleModellers(event);
    }

    @SubscribeEvent
    public static void registerItemModels(RegisterItemModelsEvent event) {
        ClientRegistry.registerItemModels(event::register);
    }

    @SubscribeEvent
    public static void registerItemColours(RegisterColorHandlersEvent.ItemTintSources event) {
        ClientRegistry.registerItemColours(event::register);
    }

    @SubscribeEvent
    public static void registerSelectItemProperties(RegisterSelectItemModelPropertyEvent event) {
        ClientRegistry.registerSelectItemProperties(event::register);
    }

    @SubscribeEvent
    public static void registerConditionalItemProperties(RegisterConditionalItemModelPropertyEvent event) {
        ClientRegistry.registerConditionalItemProperties(event::register);
    }

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        ClientRegistry.registerMenuScreens(event::register);
    }

    @SubscribeEvent
    public static void registerReloadListeners(AddClientReloadListenersEvent event) {
        ClientRegistry.registerReloadListeners(event::addListener, Minecraft.getInstance());
    }

    @SubscribeEvent
    public static void registerRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.<ItemFrame, ItemFrameRenderState>registerEntityModifier(new TypeToken<>() {
        }, (e, s) -> {
            var data = s.getRenderData(ITEM_FRAME_STATE);
            if (data == null) s.setRenderData(ITEM_FRAME_STATE, data = new ExtendedItemFrameRenderState());
            data.setup(e.getItem());
        });
    }

    @SubscribeEvent
    public static void setupClient(FMLClientSetupEvent event) {
        ClientRegistry.register();
    }
}
