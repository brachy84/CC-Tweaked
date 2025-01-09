// SPDX-FileCopyrightText: 2017 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package dan200.computercraft.api.pocket;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.upgrades.UpgradeBase;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * A peripheral which can be equipped to the back side of a pocket computer.
 * <p>
 * Pocket upgrades are defined in two stages. First, on creates a {@link IPocketUpgrade} subclass and corresponding
 * {@link PocketUpgradeSerialiser} instance, which are then registered in a Minecraft registry.
 * <p>
 * You then write a JSON file in your mod's {@literal data/} folder. This is then parsed when the world is loaded, and
 * the upgrade registered internally.
 */
public interface IPocketUpgrade extends UpgradeBase {
    /**
     * Creates a peripheral for the pocket computer.
     * <p>
     * The peripheral created will be stored for the lifetime of the upgrade, will be passed an argument to
     * {@link #update(IPocketAccess, IPeripheral)} and will be attached, detached and have methods called in the same
     * manner as an ordinary peripheral.
     *
     * @param access The access object for the pocket item stack.
     * @return The newly created peripheral.
     * @see #update(IPocketAccess, IPeripheral)
     */
    @Nullable
    IPeripheral createPeripheral(IPocketAccess access);

    /**
     * Called when the pocket computer item stack updates.
     *
     * @param access     The access object for the pocket item stack.
     * @param peripheral The peripheral for this upgrade.
     * @see #createPeripheral(IPocketAccess)
     */
    default void update(IPocketAccess access, @Nullable IPeripheral peripheral) {
    }

    /**
     * Called when the pocket computer is right clicked.
     *
     * @param world      The world the computer is in.
     * @param access     The access object for the pocket item stack.
     * @param peripheral The peripheral for this upgrade.
     * @return {@code true} to stop the GUI from opening, otherwise false. You should always provide some code path
     * which returns {@code false}, such as requiring the player to be sneaking - otherwise they will be unable to
     * access the GUI.
     * @see #createPeripheral(IPocketAccess)
     */
    default boolean onRightClick(Level world, IPocketAccess access, @Nullable IPeripheral peripheral) {
        return false;
    }
}
