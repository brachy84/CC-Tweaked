/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.upgrades;

import dan200.computercraft.api.AbstractTurtleUpgrade;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.api.turtle.TurtleUpgradeType;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.PeripheralItemFactory;
import dan200.computercraft.shared.peripheral.speaker.SpeakerPeripheral;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.vecmath.Matrix4f;

public class TurtleSpeaker extends AbstractTurtleUpgrade {

    private static class Peripheral extends SpeakerPeripheral {

        ITurtleAccess turtle;

        Peripheral(ITurtleAccess turtle) {
            this.turtle = turtle;
        }

        @Override
        public World getWorld() {
            return turtle.getWorld();
        }

        @Override
        public Vec3d getPosition() {
            BlockPos pos = turtle.getPosition();
            return new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        }

        @Override
        public boolean equals(IPeripheral other) {
            return this == other || (other instanceof Peripheral && turtle == ((Peripheral) other).turtle);
        }
    }

    @SideOnly(Side.CLIENT) private ModelResourceLocation m_leftModel;

    @SideOnly(Side.CLIENT) private ModelResourceLocation m_rightModel;

    public TurtleSpeaker(ResourceLocation id, int legacyId) {
        super(id, legacyId, TurtleUpgradeType.Peripheral, PeripheralItemFactory.create(PeripheralType.Speaker, null, 1));
    }

    @Override
    public IPeripheral createPeripheral(@Nonnull ITurtleAccess turtle, @Nonnull TurtleSide side) {
        return new TurtleSpeaker.Peripheral(turtle);
    }

    @SideOnly(Side.CLIENT)
    private void loadModelLocations() {
        if (m_leftModel == null) {
            m_leftModel = new ModelResourceLocation("computercraft:turtle_speaker_upgrade_left", "inventory");
            m_rightModel = new ModelResourceLocation("computercraft:turtle_speaker_upgrade_right", "inventory");
        }
    }

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    public Pair<IBakedModel, Matrix4f> getModel(ITurtleAccess turtle, @Nonnull TurtleSide side) {
        loadModelLocations();
        ModelManager modelManager = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager();

        if (side == TurtleSide.Left) {
            return Pair.of(modelManager.getModel(m_leftModel), null);
        } else {
            return Pair.of(modelManager.getModel(m_rightModel), null);
        }
    }

    @Override
    public void update(@Nonnull ITurtleAccess turtle, @Nonnull TurtleSide turtleSide) {
        IPeripheral turtlePeripheral = turtle.getPeripheral(turtleSide);
        if (turtlePeripheral instanceof Peripheral peripheral) {
            peripheral.update();
        }
    }
}
