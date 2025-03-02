// SPDX-FileCopyrightText: 2025 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package dan200.computercraft.client.item.model;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.DelegateBakedModel;

/**
 * A {@link BakedModel} that wraps another model, but providing different {@link ItemTransforms}.
 */
class BakedModelWithTransform extends DelegateBakedModel {
    private final ItemTransforms transforms;

    BakedModelWithTransform(BakedModel bakedModel, ItemTransforms transforms) {
        super(bakedModel);
        this.transforms = transforms;
    }

    static void addLayer(ItemStackRenderState state, BakedModel model, ItemTransforms transforms) {
        state.newLayer().setupBlockModel(new BakedModelWithTransform(model, transforms), Sheets.translucentItemSheet());
    }

    @Override
    public ItemTransforms getTransforms() {
        return transforms;
    }
}
