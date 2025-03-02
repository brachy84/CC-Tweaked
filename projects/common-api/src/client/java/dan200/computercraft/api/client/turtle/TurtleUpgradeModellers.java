// SPDX-FileCopyrightText: 2022 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package dan200.computercraft.api.client.turtle;

import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import dan200.computercraft.api.client.TransformedModel;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleSide;
import net.minecraft.core.component.DataComponentPatch;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

final class TurtleUpgradeModellers {
    private static final Transformation leftTransform = getMatrixFor(TurtleSide.LEFT);
    private static final Transformation rightTransform = getMatrixFor(TurtleSide.RIGHT);

    private static Transformation getMatrixFor(TurtleSide side) {
        var pose = new Matrix4f();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.rotate(Axis.YN.rotationDegrees(90f));
        pose.rotate(Axis.ZP.rotationDegrees(90f));
        pose.translate(0.0f, 0.0f, side == TurtleSide.RIGHT ? -0.4065f : 0.4065f);
        return new Transformation(pose);
    }

    static final TurtleUpgradeModeller<ITurtleUpgrade> UPGRADE_ITEM = new UpgradeItemModeller();

    private static final class UpgradeItemModeller implements TurtleUpgradeModeller<ITurtleUpgrade> {
        @Override
        public TransformedModel getModel(ITurtleUpgrade upgrade, @Nullable ITurtleAccess turtle, TurtleSide side, DataComponentPatch data) {
            return TransformedModel.of(upgrade.getUpgradeItem(data), side == TurtleSide.LEFT ? leftTransform : rightTransform);
        }
    }
}
