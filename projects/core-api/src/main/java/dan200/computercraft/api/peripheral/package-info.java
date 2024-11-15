// SPDX-FileCopyrightText: 2024 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

/**
 * Peripherals for blocks and upgrades.
 * <p>
 * A peripheral is an external device that a computer can interact with. Peripherals can be supplied by both a block (or
 * block entity), or from {@linkplain dan200.computercraft.api.turtle.ITurtleUpgrade#createPeripheral(dan200.computercraft.api.turtle.ITurtleAccess, dan200.computercraft.api.turtle.TurtleSide) turtle}
 * or {@linkplain dan200.computercraft.api.pocket.IPocketUpgrade#createPeripheral(dan200.computercraft.api.pocket.IPocketAccess) pocket}
 * upgrades.
 *
 * <h2>Creating peripherals for blocks</h2>
 * One of the most common things you'll want to do with ComputerCraft's API is register new peripherals. This is
 * relatively simple once you know how to do it, but may be a bit confusing the first time round.
 * <p>
 * There are currently two possible ways to define a peripheral in ComputerCraft:
 * <ul>
 *     <li>
 * <p>
 *         <strong>With a {@linkplain dan200.computercraft.api.peripheral.GenericPeripheral generic peripheral}:</strong>
 *         Generic peripherals are a way to add peripheral methods to any block entity, in a trait-based manner. This
 *         allows multiple mods to add methods to the same block entity.
 * <p>
 *         This is the recommended approach if you just want to add a couple of methods, and do not need any advanced
 *         functionality.
 *     </li>
 *     <li>
 * <p>
 *         <strong>With an {@link dan200.computercraft.api.peripheral.IPeripheral}:</strong> If your peripheral needs
 *         more advanced behaviour, such as knowing which computers it is attached to, then you can use an
 *         {@link dan200.computercraft.api.peripheral.IPeripheral}.
 * <p>
 *          These peripherals are currently <strong>NOT</strong> compatible with the generic peripheral system, so
 *          methods added by other mods (including CC's built-in inventory methods) will not be available.
 *     </li>
 * </ul>
 * <p>
 * In the following examples, we'll write a peripheral method that returns the remaining burn time of a furnace, and
 * demonstrate how to register this peripheral.
 *
 * <h3>Creating a generic peripheral</h3>
 * First, we'll need to create a new {@code final} class, that implements {@link dan200.computercraft.api.peripheral.GenericPeripheral}.
 * You'll need to implement {@link dan200.computercraft.api.peripheral.GenericPeripheral#id()}, which should just return
 * some namespaced-string with your mod id.
 * <p>
 * Then, we can start adding methods to your block entity. Each method should take its target type as the first
 * argument, which in this case is a {@code AbstractFurnaceBlockEntity}. We then annotate this method with
 * {@link dan200.computercraft.api.lua.LuaFunction} to expose it to computers.
 * <p>
 * {@snippet lang="java" :
 * import dan200.computercraft.api.lua.LuaFunction;
 * import dan200.computercraft.api.peripheral.GenericPeripheral;
 * import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
 *
 * public final class FurnacePeripheral implements GenericPeripheral {
 *     @Override
 *     public String id() {
 *         return "mymod:furnace";
 *     }
 *
 *     @LuaFunction(mainThread = true)
 *     public int getBurnTime(AbstractFurnaceBlockEntity furnace) {
 *         return furnace.litTime;
 *     }
 * }
 * }
 * <p>
 * Finally, we need to register our peripheral, so that ComputerCraft is aware of it:
 * <p>
 * {@snippet lang="java" :
 * import dan200.computercraft.api.ComputerCraftAPI;
 *
 * public class ComputerCraftCompat {
 *     public static void register() {
 *         ComputerCraftAPI.registerGenericSource(new FurnacePeripheral());
 *     }
 * }
 * }
 *
 * <h3>Creating a {@code IPeripheral}</h3>
 * First, we'll need to create a new class that implements {@link dan200.computercraft.api.peripheral.IPeripheral}. This
 * requires a couple of boilerplate methods: one to get the type of the peripheral, and an equality function.
 * <p>
 * We can then start adding peripheral methods to our class. Each method should be {@code final}, and annotated with
 * {@link dan200.computercraft.api.lua.LuaFunction}.
 * <p>
 * {@snippet lang="java" :
 * import dan200.computercraft.api.lua.LuaFunction;
 * import dan200.computercraft.api.peripheral.IPeripheral;
 * import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
 * import org.jetbrains.annotations.Nullable;
 *
 * public class FurnacePeripheral implements IPeripheral {
 *     private final AbstractFurnaceBlockEntity furnace;
 *
 *     public FurnacePeripheral(AbstractFurnaceBlockEntity furnace) {
 *         this.furnace = furnace;
 *     }
 *
 *     @Override
 *     public String getType() {
 *         return "furnace";
 *     }
 *
 *     @LuaFunction(mainThread = true)
 *     public final int getBurnTime() {
 *         return furnace.litTime;
 *     }
 *
 *     @Override
 *     public boolean equals(@Nullable IPeripheral other) {
 *         return this == other || other instanceof FurnacePeripheral p && furnace == p.furnace;
 *     }
 * }
 * }
 * <p>
 * Finally, we'll need to register our peripheral. This is done with capabilities on Forge, or the block lookup API on
 * Fabric.
 *
 * <h4>Registering {@code IPeripheral} on Forge</h4>
 * Registering a peripheral on Forge can be done by using the capability API, via {@code PeripheralCapability}.
 *
 * {@snippet lang="java" :
 * import dan200.computercraft.api.peripheral.PeripheralCapability;
 * import net.minecraft.world.level.block.entity.BlockEntityType;
 * import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
 *
 * public class ComputerCraftCompat {
 *     public static void register(RegisterCapabilitiesEvent event) {
 *         event.registerBlockEntity(PeripheralCapability.get(), BlockEntityType.FURNACE, (f, s) -> new FurnacePeripheral(f));
 *         event.registerBlockEntity(PeripheralCapability.get(), BlockEntityType.BLAST_FURNACE, (f, s) -> new FurnacePeripheral(f));
 *         event.registerBlockEntity(PeripheralCapability.get(), BlockEntityType.SMOKER, (f, s) -> new FurnacePeripheral(f));
 *     }
 * }
 * }
 *
 * <h4>Registering {@code IPeripheral} on Fabric</h4>
 * Registering a peripheral on Fabric can be done using the block lookup API, via {@code PeripheralLookup}.
 *
 * {@snippet lang="java" :
 * import dan200.computercraft.api.peripheral.PeripheralLookup;
 * import dan200.computercraft.example.FurnacePeripheral;
 * import net.minecraft.world.level.block.entity.BlockEntityType;
 *
 * public class ComputerCraftCompat {
 *     public static void register() {
 *         PeripheralLookup.get().registerForBlockEntity((f, s) -> new FurnacePeripheral(f), BlockEntityType.FURNACE);
 *         PeripheralLookup.get().registerForBlockEntity((f, s) -> new FurnacePeripheral(f), BlockEntityType.BLAST_FURNACE);
 *         PeripheralLookup.get().registerForBlockEntity((f, s) -> new FurnacePeripheral(f), BlockEntityType.SMOKER);
 *     }
 * }
 * }
 */
package dan200.computercraft.api.peripheral;
