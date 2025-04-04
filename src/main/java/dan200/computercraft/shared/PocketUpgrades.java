/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.pocket.IPocketUpgrade;
import dan200.computercraft.shared.util.InventoryUtil;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public final class PocketUpgrades {

    private static final Map<String, IPocketUpgrade> upgrades = new HashMap<>();
    private static final IdentityHashMap<IPocketUpgrade, String> upgradeOwners = new IdentityHashMap<>();

    private PocketUpgrades() {
    }

    public static void register(@Nonnull IPocketUpgrade upgrade) {
        Objects.requireNonNull(upgrade, "upgrade cannot be null");

        String id = upgrade.getUpgradeID().toString();
        IPocketUpgrade existing = upgrades.get(id);
        if (existing != null) {
            throw new IllegalStateException("Error registering '" +
                                                upgrade.getUnlocalisedAdjective() +
                                                " pocket computer'. UpgradeID '" +
                                                id +
                                                "' is already registered by '" +
                                                existing.getUnlocalisedAdjective() +
                                                " pocket computer'");
        }

        upgrades.put(id, upgrade);

        ModContainer mc = Loader.instance().activeModContainer();
        if (mc != null && mc.getModId() != null) upgradeOwners.put(upgrade, mc.getModId());
    }

    public static IPocketUpgrade get(String id) {
        // Fix a typo in the advanced modem upgrade's name. I'm sorry, I realise this is horrible.
        if (id.equals("computercraft:advanved_modem")) id = "computercraft:advanced_modem";

        return upgrades.get(id);
    }

    public static IPocketUpgrade get(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) return null;

        for (IPocketUpgrade upgrade : upgrades.values()) {
            ItemStack craftingStack = upgrade.getCraftingItem();
            if (!craftingStack.isEmpty() && InventoryUtil.areItemsSimilar(stack, craftingStack)) {
                return upgrade;
            }
        }

        return null;
    }

    @Nullable
    public static String getOwner(IPocketUpgrade upgrade) {
        return upgradeOwners.get(upgrade);
    }

    public static Iterable<IPocketUpgrade> getVanillaUpgrades() {
        List<IPocketUpgrade> vanilla = new ArrayList<>();
        vanilla.add(ComputerCraft.PocketUpgrades.wirelessModem);
        vanilla.add(ComputerCraft.PocketUpgrades.advancedModem);
        vanilla.add(ComputerCraft.PocketUpgrades.speaker);
        return vanilla;
    }

    public static Iterable<IPocketUpgrade> getUpgrades() {
        return Collections.unmodifiableCollection(upgrades.values());
    }
}
