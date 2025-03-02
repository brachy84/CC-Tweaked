// SPDX-FileCopyrightText: 2022 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package dan200.computercraft.client;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.client.FabricComputerCraftAPIClient;
import dan200.computercraft.client.model.ExtraModels;
import dan200.computercraft.core.util.Nullability;
import dan200.computercraft.impl.Services;
import dan200.computercraft.shared.ModRegistry;
import dan200.computercraft.shared.config.ConfigSpec;
import dan200.computercraft.shared.network.NetworkMessages;
import dan200.computercraft.shared.network.client.ClientNetworkContext;
import dan200.computercraft.shared.platform.FabricConfigFile;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.minecraft.world.phys.BlockHitResult;

import java.util.concurrent.CompletableFuture;

import static dan200.computercraft.core.util.Nullability.assertNonNull;

public class ComputerCraftClient {
    public static void init() {
        var clientNetwork = Services.load(ClientNetworkContext.class);
        for (var type : NetworkMessages.getClientbound()) {
            ClientPlayNetworking.registerGlobalReceiver(
                type.type(), (packet, responseSender) -> packet.handle(clientNetwork)
            );
        }

        ClientRegistry.register();
        ClientRegistry.registerTurtleModellers(FabricComputerCraftAPIClient::registerTurtleUpgradeModeller);
        ClientRegistry.registerMenuScreens(MenuScreens::register);
        ClientRegistry.registerItemModels(ItemModels.ID_MAPPER::put);
        ClientRegistry.registerItemColours(ItemTintSources.ID_MAPPER::put);
        ClientRegistry.registerSelectItemProperties(SelectItemModelProperties.ID_MAPPER::put);
        ClientRegistry.registerConditionalItemProperties(ConditionalItemModelProperties.ID_MAPPER::put);

        PreparableModelLoadingPlugin.register(
            (resources, executor) -> CompletableFuture.supplyAsync(() -> ExtraModels.loadAll(resources), executor),
            (state, context) -> ClientRegistry.registerExtraModels(context::addModels, state)
        );

        BlockRenderLayerMap.INSTANCE.putBlock(ModRegistry.Blocks.COMPUTER_NORMAL.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModRegistry.Blocks.COMPUTER_COMMAND.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModRegistry.Blocks.COMPUTER_ADVANCED.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModRegistry.Blocks.MONITOR_NORMAL.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModRegistry.Blocks.MONITOR_ADVANCED.get(), RenderType.cutout());

        ClientTickEvents.START_CLIENT_TICK.register(client -> ClientHooks.onTick());
        // This isn't 100% consistent with Forge, but not worth a mixin.
        WorldRenderEvents.START.register(context -> ClientHooks.onRenderTick());
        WorldRenderEvents.BLOCK_OUTLINE.register((context, hitResult) -> {
            var hit = Minecraft.getInstance().hitResult;
            if (hit instanceof BlockHitResult blockHit && blockHit.getBlockPos().equals(hitResult.blockPos())) {
                return !ClientHooks.drawHighlight(Nullability.assertNonNull(context.matrixStack()), assertNonNull(context.consumers()), context.camera(), blockHit);
            } else {
                return true;
            }
        });

        ClientCommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess) -> ClientRegistry.registerClientCommands(dispatcher, FabricClientCommandSource::sendError)
        );

        ((FabricConfigFile) ConfigSpec.clientSpec).load(FabricLoader.getInstance().getConfigDir().resolve(ComputerCraftAPI.MOD_ID + "-client.toml"));
    }
}
