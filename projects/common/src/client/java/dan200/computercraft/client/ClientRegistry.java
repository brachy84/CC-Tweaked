// Copyright Daniel Ratcliffe, 2011-2022. Do not distribute without permission.
//
// SPDX-License-Identifier: LicenseRef-CCPL

package dan200.computercraft.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.serialization.MapCodec;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.client.turtle.RegisterTurtleUpgradeModeller;
import dan200.computercraft.api.client.turtle.TurtleUpgradeModeller;
import dan200.computercraft.client.gui.*;
import dan200.computercraft.client.item.colour.PocketComputerLight;
import dan200.computercraft.client.item.model.TurtleOverlayModel;
import dan200.computercraft.client.item.model.TurtleUpgradeModel;
import dan200.computercraft.client.item.properties.PocketComputerStateProperty;
import dan200.computercraft.client.item.properties.TurtleShowElfOverlay;
import dan200.computercraft.client.render.CustomLecternRenderer;
import dan200.computercraft.client.render.TurtleBlockEntityRenderer;
import dan200.computercraft.client.render.monitor.MonitorBlockEntityRenderer;
import dan200.computercraft.client.turtle.TurtleModemModeller;
import dan200.computercraft.client.turtle.TurtleUpgradeModellers;
import dan200.computercraft.shared.ModRegistry;
import dan200.computercraft.shared.command.CommandComputerCraft;
import dan200.computercraft.shared.computer.core.ServerContext;
import dan200.computercraft.shared.computer.inventory.AbstractComputerMenu;
import dan200.computercraft.shared.turtle.TurtleOverlay;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import java.io.File;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Registers client-side objects, such as {@link BlockEntityRendererProvider}s and
 * {@link MenuScreens.ScreenConstructor}.
 * <p>
 * The functions in this class should be called from a loader-specific class.
 *
 * @see ModRegistry The common registry for actual game objects.
 */
public final class ClientRegistry {
    private ClientRegistry() {
    }

    /**
     * Register any client-side objects which don't have to be done on the main thread.
     */
    public static void register() {
        BlockEntityRenderers.register(ModRegistry.BlockEntities.MONITOR_NORMAL.get(), MonitorBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModRegistry.BlockEntities.MONITOR_ADVANCED.get(), MonitorBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModRegistry.BlockEntities.TURTLE_NORMAL.get(), TurtleBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModRegistry.BlockEntities.TURTLE_ADVANCED.get(), TurtleBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModRegistry.BlockEntities.LECTERN.get(), CustomLecternRenderer::new);
    }

    public static void registerMenuScreens(RegisterMenuScreen register) {
        register.<AbstractComputerMenu, ComputerScreen<AbstractComputerMenu>>register(ModRegistry.Menus.COMPUTER.get(), ComputerScreen::new);
        register.<AbstractComputerMenu, NoTermComputerScreen<AbstractComputerMenu>>register(ModRegistry.Menus.POCKET_COMPUTER_NO_TERM.get(), NoTermComputerScreen::new);
        register.register(ModRegistry.Menus.TURTLE.get(), TurtleScreen::new);

        register.register(ModRegistry.Menus.PRINTER.get(), PrinterScreen::new);
        register.register(ModRegistry.Menus.DISK_DRIVE.get(), DiskDriveScreen::new);
        register.register(ModRegistry.Menus.PRINTOUT.get(), PrintoutScreen::new);
    }

    public interface RegisterMenuScreen {
        <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(MenuType<? extends M> type, MenuScreens.ScreenConstructor<M, U> factory);
    }

    public static void registerTurtleModellers(RegisterTurtleUpgradeModeller register) {
        register.register(ModRegistry.TurtleUpgradeTypes.SPEAKER.get(), TurtleUpgradeModeller.sided(
            ResourceLocation.fromNamespaceAndPath(ComputerCraftAPI.MOD_ID, "block/turtle_speaker_left"),
            ResourceLocation.fromNamespaceAndPath(ComputerCraftAPI.MOD_ID, "block/turtle_speaker_right")
        ));
        register.register(ModRegistry.TurtleUpgradeTypes.WORKBENCH.get(), TurtleUpgradeModeller.sided(
            ResourceLocation.fromNamespaceAndPath(ComputerCraftAPI.MOD_ID, "block/turtle_crafting_table_left"),
            ResourceLocation.fromNamespaceAndPath(ComputerCraftAPI.MOD_ID, "block/turtle_crafting_table_right")
        ));
        register.register(ModRegistry.TurtleUpgradeTypes.WIRELESS_MODEM.get(), new TurtleModemModeller());
        register.register(ModRegistry.TurtleUpgradeTypes.TOOL.get(), TurtleUpgradeModeller.flatItem());
    }

    public static void registerReloadListeners(BiConsumer<ResourceLocation, PreparableReloadListener> register, Minecraft minecraft) {
        register.accept(ResourceLocation.fromNamespaceAndPath(ComputerCraftAPI.MOD_ID, "sprites"), GuiSprites.initialise(minecraft.getTextureManager()));
    }

    private static final ResourceLocation[] EXTRA_MODELS = {
        TurtleOverlay.ELF_MODEL,
        TurtleBlockEntityRenderer.NORMAL_TURTLE_MODEL,
        TurtleBlockEntityRenderer.ADVANCED_TURTLE_MODEL,
        TurtleBlockEntityRenderer.COLOUR_TURTLE_MODEL,
    };

    public static void registerExtraModels(Consumer<ResourceLocation> register, Collection<ResourceLocation> extraModels) {
        for (var model : EXTRA_MODELS) register.accept(model);
        extraModels.forEach(register);
        TurtleUpgradeModellers.getDependencies().forEach(register);
    }

    public static void registerItemModels(BiConsumer<ResourceLocation, MapCodec<? extends ItemModel.Unbaked>> register) {
        register.accept(TurtleOverlayModel.ID, TurtleOverlayModel.CODEC);
        register.accept(TurtleUpgradeModel.ID, TurtleUpgradeModel.CODEC);
    }

    public static void registerItemColours(BiConsumer<ResourceLocation, MapCodec<? extends ItemTintSource>> register) {
        register.accept(PocketComputerLight.ID, PocketComputerLight.CODEC);
    }

    public static void registerSelectItemProperties(BiConsumer<ResourceLocation, SelectItemModelProperty.Type<?, ?>> register) {
        register.accept(PocketComputerStateProperty.ID, PocketComputerStateProperty.TYPE);
    }

    public static void registerConditionalItemProperties(BiConsumer<ResourceLocation, MapCodec<? extends ConditionalItemModelProperty>> register) {
        register.accept(TurtleShowElfOverlay.ID, TurtleShowElfOverlay.CODEC);
    }

    /**
     * Register client-side commands.
     *
     * @param dispatcher The dispatcher to register the commands to.
     * @param sendError  A function to send an error message.
     * @param <T>        The type of the client-side command context.
     */
    public static <T> void registerClientCommands(CommandDispatcher<T> dispatcher, BiConsumer<T, Component> sendError) {
        dispatcher.register(LiteralArgumentBuilder.<T>literal(CommandComputerCraft.CLIENT_OPEN_FOLDER)
            .requires(x -> Minecraft.getInstance().getSingleplayerServer() != null)
            .then(RequiredArgumentBuilder.<T, Integer>argument("computer_id", IntegerArgumentType.integer(0))
                .executes(c -> handleOpenComputerCommand(c.getSource(), sendError, c.getArgument("computer_id", Integer.class)))
            ));
    }

    /**
     * Handle the {@link CommandComputerCraft#CLIENT_OPEN_FOLDER} command.
     *
     * @param context   The command context.
     * @param sendError A function to send an error message.
     * @param id        The computer's id.
     * @param <T>       The type of the client-side command context.
     * @return {@code 1} if a folder was opened, {@code 0} otherwise.
     */
    private static <T> int handleOpenComputerCommand(T context, BiConsumer<T, Component> sendError, int id) {
        var server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null) {
            sendError.accept(context, Component.literal("Not on a single-player server"));
            return 0;
        }

        var file = new File(ServerContext.get(server).storageDir().toFile(), "computer/" + id);
        if (!file.isDirectory()) {
            sendError.accept(context, Component.literal("Computer's folder does not exist"));
            return 0;
        }

        Util.getPlatform().openFile(file);
        return 1;
    }
}
