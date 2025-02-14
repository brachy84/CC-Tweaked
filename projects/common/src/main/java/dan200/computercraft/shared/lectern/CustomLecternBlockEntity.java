// SPDX-FileCopyrightText: 2024 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package dan200.computercraft.shared.lectern;

import dan200.computercraft.shared.ModRegistry;
import dan200.computercraft.shared.container.BasicContainer;
import dan200.computercraft.shared.container.SingleContainerData;
import dan200.computercraft.shared.media.PrintoutMenu;
import dan200.computercraft.shared.media.items.PrintoutData;
import dan200.computercraft.shared.media.items.PrintoutItem;
import dan200.computercraft.shared.pocket.core.PocketHolder;
import dan200.computercraft.shared.pocket.items.PocketComputerItem;
import dan200.computercraft.shared.util.BlockEntityHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.AbstractList;
import java.util.List;

/**
 * The block entity for our {@link CustomLecternBlock}.
 *
 * @see LecternBlockEntity
 */
public final class CustomLecternBlockEntity extends BlockEntity {
    private static final String NBT_ITEM = "Item";
    private static final String NBT_PAGE = "Page";

    private ItemStack item = ItemStack.EMPTY;
    private int page, pageCount;

    public CustomLecternBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModRegistry.BlockEntities.LECTERN.get(), pos, blockState);
    }

    public ItemStack getItem() {
        return item;
    }

    void setItem(ItemStack item) {
        this.item = item;
        itemChanged();
        BlockEntityHelpers.updateBlock(this);
    }

    int getRedstoneSignal() {
        if (item.getItem() instanceof PrintoutItem) {
            var progress = pageCount > 1 ? (float) page / (pageCount - 1) : 1F;
            return Mth.floor(progress * 14f) + 1;
        }

        return 15;
    }

    /**
     * Called after the item has changed. This sets up the state for the new item.
     */
    private void itemChanged() {
        if (item.getItem() instanceof PrintoutItem) {
            pageCount = PrintoutData.getOrEmpty(item).pages();
            page = Mth.clamp(page, 0, pageCount - 1);
        } else {
            pageCount = page = 0;
        }
    }

    void tick() {
        if (item.getItem() instanceof PocketComputerItem pocket) {
            pocket.tick(item, new PocketHolder.LecternHolder(this), false);
        }
    }

    /**
     * Set the current page, emitting a redstone pulse if needed.
     *
     * @param page The new page.
     */
    private void setPage(int page) {
        if (this.page == page) return;

        this.page = page;
        setChanged();
        if (getLevel() != null) LecternBlock.signalPageChange(getLevel(), getBlockPos(), getBlockState());
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        item = tag.contains(NBT_ITEM, Tag.TAG_COMPOUND) ? ItemStack.parseOptional(registries, tag.getCompound(NBT_ITEM)) : ItemStack.EMPTY;
        page = tag.getInt(NBT_PAGE);
        itemChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        if (!item.isEmpty()) tag.put(NBT_ITEM, item.save(registries));
        if (item.getItem() instanceof PrintoutItem) tag.putInt(NBT_PAGE, page);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        var tag = super.getUpdateTag(registries);
        if (!item.isEmpty()) tag.put(NBT_ITEM, item.save(registries));
        return tag;
    }

    void openMenu(Player player) {
        var item = getItem();
        if (item.getItem() instanceof PrintoutItem) {
            player.openMenu(new SimpleMenuProvider((id, inventory, entity) -> new PrintoutMenu(
                id, new LecternContainer(), 0,
                p -> Container.stillValidBlockEntity(this, p, Container.DEFAULT_DISTANCE_BUFFER),
                new PrintoutContainerData()
            ), getItem().getDisplayName()));
        } else if (item.getItem() instanceof PocketComputerItem pocket) {
            pocket.open(player, item, new PocketHolder.LecternHolder(this), true);
        }
    }

    /**
     * A read-only container storing the lectern's contents.
     */
    private final class LecternContainer implements BasicContainer {
        private final List<ItemStack> itemView = new AbstractList<>() {
            @Override
            public ItemStack get(int index) {
                if (index != 0) throw new IndexOutOfBoundsException("Inventory only has one slot");
                return item;
            }

            @Override
            public int size() {
                return 1;
            }
        };

        @Override
        public List<ItemStack> getItems() {
            return itemView;
        }

        @Override
        public void setChanged() {
            // Should never happen, so a no-op.
        }

        @Override
        public boolean stillValid(Player player) {
            return !isRemoved();
        }
    }

    /**
     * {@link ContainerData} for a {@link PrintoutMenu}. This provides a read/write view of the current page.
     */
    private final class PrintoutContainerData implements SingleContainerData {
        @Override
        public int get() {
            return page;
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) setPage(value);
        }
    }
}
