/*
 * This file is part of the public ComputerCraft API - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. This API may be redistributed unmodified and in full only.
 * For help using the API, and posting your mods, visit the forums at computercraft.info.
 */
package dan200.computercraft.api.pocket;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * A base class for {@link IPocketUpgrade}s.
 * One does not have to use this, but it does provide a convenient template.
 */
public abstract class AbstractPocketUpgrade implements IPocketUpgrade {

    private final ResourceLocation id;
    private final String adjective;
    private final ItemStack stack;

    protected AbstractPocketUpgrade(ResourceLocation id, String adjective, ItemStack stack) {
        this.id = id;
        this.adjective = adjective;
        this.stack = stack;
    }

    protected AbstractPocketUpgrade(ResourceLocation id, ItemStack stack) {
        this(id, "upgrade." + id + ".adjective", stack);
    }

    @Nonnull
    @Override
    public final ResourceLocation getUpgradeID() {
        return id;
    }

    @Nonnull
    @Override
    public final String getUnlocalisedAdjective() {
        return adjective;
    }

    @Nonnull
    @Override
    public final ItemStack getCraftingItem() {
        return stack;
    }
}
