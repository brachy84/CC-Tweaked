// SPDX-FileCopyrightText: 2022 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package dan200.computercraft.impl;

import com.mojang.serialization.Codec;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.detail.BlockReference;
import dan200.computercraft.api.detail.DetailRegistry;
import dan200.computercraft.api.filesystem.Mount;
import dan200.computercraft.api.filesystem.WritableMount;
import dan200.computercraft.api.lua.GenericSource;
import dan200.computercraft.api.lua.ILuaAPIFactory;
import dan200.computercraft.api.media.PrintoutContents;
import dan200.computercraft.api.network.PacketNetwork;
import dan200.computercraft.api.network.wired.WiredElement;
import dan200.computercraft.api.network.wired.WiredNode;
import dan200.computercraft.api.pocket.IPocketUpgrade;
import dan200.computercraft.api.redstone.BundledRedstoneProvider;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleRefuelHandler;
import dan200.computercraft.api.upgrades.UpgradeType;
import dan200.computercraft.impl.upgrades.TurtleToolSpec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * Backing interface for {@link ComputerCraftAPI}
 * <p>
 * Do <strong>NOT</strong> directly reference this class. It exists for internal use by the API.
 */
@ApiStatus.Internal
public interface ComputerCraftAPIService {
    static ComputerCraftAPIService get() {
        var instance = Instance.INSTANCE;
        return instance == null ? Services.raise(ComputerCraftAPIService.class, Instance.ERROR) : instance;
    }

    String getInstalledVersion();

    int createUniqueNumberedSaveDir(MinecraftServer server, String parentSubPath);

    WritableMount createSaveDirMount(MinecraftServer server, String subPath, long capacity);

    @Nullable
    Mount createResourceMount(MinecraftServer server, String domain, String subPath);

    void registerGenericSource(GenericSource source);

    void registerBundledRedstoneProvider(BundledRedstoneProvider provider);

    int getBundledRedstoneOutput(Level world, BlockPos pos, Direction side);

    PacketNetwork getWirelessNetwork(MinecraftServer server);

    void registerAPIFactory(ILuaAPIFactory factory);

    WiredNode createWiredNodeForElement(WiredElement element);

    void registerRefuelHandler(TurtleRefuelHandler handler);

    ResourceKey<Registry<UpgradeType<? extends ITurtleUpgrade>>> turtleUpgradeRegistryId();

    Codec<ITurtleUpgrade> turtleUpgradeCodec();

    ResourceKey<Registry<UpgradeType<? extends IPocketUpgrade>>> pocketUpgradeRegistryId();

    ITurtleUpgrade createTurtleTool(TurtleToolSpec spec);

    Codec<IPocketUpgrade> pocketUpgradeCodec();

    DetailRegistry<ItemStack> getItemStackDetailRegistry();

    DetailRegistry<BlockReference> getBlockInWorldDetailRegistry();

    @Nullable
    PrintoutContents getPrintoutContents(ItemStack stack);

    final class Instance {
        static final @Nullable ComputerCraftAPIService INSTANCE;
        static final @Nullable Throwable ERROR;

        static {
            var helper = Services.tryLoad(ComputerCraftAPIService.class);
            INSTANCE = helper.instance();
            ERROR = helper.error();
        }

        private Instance() {
        }
    }
}
