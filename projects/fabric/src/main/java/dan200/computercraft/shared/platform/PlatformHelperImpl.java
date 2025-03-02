// SPDX-FileCopyrightText: 2022 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package dan200.computercraft.shared.platform;

import com.google.auto.service.AutoService;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.ArgumentType;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.media.IMedia;
import dan200.computercraft.api.media.MediaLookup;
import dan200.computercraft.api.network.wired.WiredElement;
import dan200.computercraft.api.network.wired.WiredElementLookup;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.PeripheralLookup;
import dan200.computercraft.impl.Peripherals;
import dan200.computercraft.mixin.ArgumentTypeInfosAccessor;
import dan200.computercraft.shared.config.ConfigFile;
import dan200.computercraft.shared.network.container.ContainerData;
import dan200.computercraft.shared.util.InventoryUtil;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

@AutoService(PlatformHelper.class)
public class PlatformHelperImpl implements PlatformHelper {
    @Override
    public boolean isModLoaded(String id) {
        return FabricLoader.getInstance().isModLoaded(id);
    }

    @Override
    public ConfigFile.Builder createConfigBuilder() {
        return new FabricConfigFile.Builder();
    }

    @SuppressWarnings("unchecked")
    private static <T> Registry<T> getRegistry(ResourceKey<Registry<T>> id) {
        var registry = (Registry<T>) BuiltInRegistries.REGISTRY.getValue(id.location());
        if (registry == null) throw new IllegalArgumentException("Unknown registry " + id);
        return registry;
    }

    @Override
    public <T> RegistrationHelper<T> createRegistrationHelper(ResourceKey<Registry<T>> registry) {
        return new RegistrationHelperImpl<>(getRegistry(registry));
    }

    @Override
    public <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>, I extends ArgumentTypeInfo<A, T>> I registerArgumentTypeInfo(Class<A> klass, I info) {
        ArgumentTypeInfosAccessor.classMap().put(klass, info);
        return info;
    }

    @Override
    public <C extends AbstractContainerMenu, T extends ContainerData> MenuType<C> createMenuType(StreamCodec<RegistryFriendlyByteBuf, T> codec, ContainerData.Factory<C, T> factory) {
        return new ExtendedScreenHandlerType<>(factory::create, codec);
    }

    @Override
    public void openMenu(Player player, Component title, MenuConstructor menu, ContainerData data) {
        player.openMenu(new WrappedMenuProvider<>(title, menu, data));
    }

    @Override
    public ComponentAccess<IPeripheral> createPeripheralAccess(BlockEntity owner, Consumer<Direction> invalidate) {
        return new PeripheralAccessImpl(owner);
    }

    @Override
    public ComponentAccess<WiredElement> createWiredElementAccess(BlockEntity owner, Consumer<Direction> invalidate) {
        return new ComponentAccessImpl<>(owner, WiredElementLookup.get());
    }

    @Override
    public boolean hasWiredElementIn(Level level, BlockPos pos, Direction direction) {
        return WiredElementLookup.get().find(level, pos.relative(direction), direction.getOpposite()) != null;
    }

    @Override
    public ContainerTransfer.Slotted wrapContainer(Container container) {
        return FabricContainerTransfer.of(InventoryStorage.of(container, null));
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public @Nullable ContainerTransfer getContainer(ServerLevel level, BlockPos pos, Direction side) {
        var storage = ItemStorage.SIDED.find(level, pos, side);
        if (storage != null) return FabricContainerTransfer.of(storage);

        var entity = InventoryUtil.getEntityContainer(level, pos, side);
        return entity == null ? null : FabricContainerTransfer.of(InventoryStorage.of(entity, side));
    }

    @Override
    public RecipeIngredients getRecipeIngredients() {
        return new RecipeIngredients(
            ConventionalItemTags.REDSTONE_DUSTS,
            ConventionalItemTags.STRINGS,
            ConventionalItemTags.LEATHERS,
            ConventionalItemTags.GLASS_PANES,
            ConventionalItemTags.GOLD_INGOTS,
            ConventionalItemTags.STORAGE_BLOCKS_GOLD,
            ConventionalItemTags.IRON_INGOTS,
            ConventionalItemTags.DYES,
            ConventionalItemTags.ENDER_PEARLS,
            ConventionalItemTags.WOODEN_CHESTS
        );
    }

    @Override
    public List<TagKey<Item>> getDyeTags() {
        return List.of(
            ConventionalItemTags.WHITE_DYES,
            ConventionalItemTags.ORANGE_DYES,
            ConventionalItemTags.MAGENTA_DYES,
            ConventionalItemTags.LIGHT_BLUE_DYES,
            ConventionalItemTags.YELLOW_DYES,
            ConventionalItemTags.LIME_DYES,
            ConventionalItemTags.PINK_DYES,
            ConventionalItemTags.GRAY_DYES,
            ConventionalItemTags.LIGHT_GRAY_DYES,
            ConventionalItemTags.CYAN_DYES,
            ConventionalItemTags.PURPLE_DYES,
            ConventionalItemTags.BLUE_DYES,
            ConventionalItemTags.BROWN_DYES,
            ConventionalItemTags.GREEN_DYES,
            ConventionalItemTags.RED_DYES,
            ConventionalItemTags.BLACK_DYES
        );
    }

    @Override
    public int getBurnTime(MinecraftServer server, ItemStack stack) {
        return server.fuelValues().burnDuration(stack);
    }

    @Override
    public CreativeModeTab.Builder newCreativeModeTab() {
        return FabricItemGroup.builder();
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return stack.getRecipeRemainder();
    }

    @Override
    public boolean onNotifyNeighbour(Level level, BlockPos pos, BlockState block, Direction direction) {
        return true;
    }

    @Override
    public ServerPlayer createFakePlayer(ServerLevel world, GameProfile name) {
        return new FakePlayer(world, name);
    }

    @Override
    public boolean hasToolUsage(ItemStack stack) {
        var item = stack.getItem();
        return item instanceof ShovelItem || stack.is(ItemTags.SHOVELS) ||
            item instanceof HoeItem || stack.is(ItemTags.HOES);
    }

    @Override
    public InteractionResult canAttackEntity(ServerPlayer player, Entity entity) {
        return AttackEntityCallback.EVENT.invoker().interact(player, player.level(), InteractionHand.MAIN_HAND, entity, null);
    }

    @Override
    public boolean interactWithEntity(ServerPlayer player, Entity entity, Vec3 hitPos) {
        return UseEntityCallback.EVENT.invoker().interact(player, entity.level(), InteractionHand.MAIN_HAND, entity, new EntityHitResult(entity, hitPos)).consumesAction()
            || entity.interactAt(player, hitPos.subtract(entity.position()), InteractionHand.MAIN_HAND).consumesAction()
            || player.interactOn(entity, InteractionHand.MAIN_HAND).consumesAction();
    }

    @Override
    public UseOnResult useOn(ServerPlayer player, ItemStack stack, BlockHitResult hit) {
        var result = UseBlockCallback.EVENT.invoker().interact(player, player.level(), InteractionHand.MAIN_HAND, hit);
        if (result != InteractionResult.PASS) return new UseOnResult.Handled(result);
        return new UseOnResult.Continue(true, true);
    }

    @Override
    @SuppressWarnings("NullAway") // NullAway doesn't like the null here.
    public @Nullable IMedia getMedia(ItemStack stack) {
        return MediaLookup.get().find(stack, null);
    }

    private static final class RegistrationHelperImpl<T> implements RegistrationHelper<T> {
        private final Registry<T> registry;
        private final List<RegistryEntryImpl<? extends T>> entries = new ArrayList<>();

        private RegistrationHelperImpl(Registry<T> registry) {
            this.registry = registry;
        }

        @Override
        public <U extends T> RegistryEntry<U> register(String name, Supplier<U> create) {
            var entry = new RegistryEntryImpl<>(ResourceLocation.fromNamespaceAndPath(ComputerCraftAPI.MOD_ID, name), create);
            entries.add(entry);
            return entry;
        }

        @Override
        public void register() {
            for (var entry : entries) entry.register(registry);
        }
    }

    private static final class RegistryEntryImpl<T> implements RegistryEntry<T> {
        private final ResourceLocation id;
        private final Supplier<T> supplier;
        private @Nullable T instance;

        RegistryEntryImpl(ResourceLocation id, Supplier<T> supplier) {
            this.id = id;
            this.supplier = supplier;
        }

        void register(Registry<? super T> registry) {
            Registry.register(registry, id, instance = supplier.get());
        }

        @Override
        public ResourceLocation id() {
            return id;
        }

        @Override
        public T get() {
            if (instance == null) throw new IllegalStateException(id + " has not been constructed yet");
            return instance;
        }
    }

    private record WrappedMenuProvider<T extends ContainerData>(
        Component title, MenuConstructor menu, T data
    ) implements ExtendedScreenHandlerFactory<T> {
        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
            return menu.createMenu(id, inventory, player);
        }

        @Override
        public Component getDisplayName() {
            return title;
        }

        @Override
        public T getScreenOpeningData(ServerPlayer player) {
            return data;
        }
    }

    private static class ComponentAccessImpl<T> implements ComponentAccess<T> {
        private final BlockEntity owner;
        private final BlockApiLookup<T, Direction> lookup;
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final BlockApiCache<T, Direction>[] caches = new BlockApiCache[6];

        private ComponentAccessImpl(BlockEntity owner, BlockApiLookup<T, Direction> lookup) {
            this.owner = owner;
            this.lookup = lookup;
        }

        @Nullable
        @Override
        public T get(Direction direction) {
            var cache = caches[direction.ordinal()];
            if (cache == null) {
                cache = caches[direction.ordinal()] = BlockApiCache.create(lookup, getLevel(), owner.getBlockPos().relative(direction));
            }

            return cache.find(direction.getOpposite());
        }

        private ServerLevel getLevel() {
            return Objects.requireNonNull((ServerLevel) owner.getLevel(), "Block entity is not in a level");
        }
    }

    private static final class PeripheralAccessImpl extends ComponentAccessImpl<IPeripheral> {
        private PeripheralAccessImpl(BlockEntity owner) {
            super(owner, PeripheralLookup.get());
        }

        @Nullable
        @Override
        public IPeripheral get(Direction direction) {
            var result = super.get(direction);
            if (result != null) return result;

            var cache = caches[direction.ordinal()];
            return Peripherals.getGenericPeripheral(cache.getWorld(), cache.getPos(), direction.getOpposite(), cache.getBlockEntity());
        }
    }
}
