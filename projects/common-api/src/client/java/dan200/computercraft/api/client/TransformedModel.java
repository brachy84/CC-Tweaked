// SPDX-FileCopyrightText: 2020 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0


package dan200.computercraft.api.client;

import com.mojang.math.Transformation;
import dan200.computercraft.impl.client.ClientPlatformHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * A model to render, combined with a transformation matrix to apply.
 */
public sealed interface TransformedModel permits TransformedModel.Baked, TransformedModel.Item {
    record Baked(BakedModel model) implements TransformedModel {
    }

    record Item(ItemStack stack, Transformation transformation) implements TransformedModel {
    }

    static TransformedModel of(BakedModel model) {
        return new TransformedModel.Baked(model);
    }

    /**
     * Look up a model in the model bakery and construct a {@link TransformedModel} with no transformation.
     *
     * @param location The location of the model to load.
     * @return The new {@link TransformedModel} instance.
     */
    static TransformedModel of(ResourceLocation location) {
        var modelManager = Minecraft.getInstance().getModelManager();
        return of(ClientPlatformHelper.get().getModel(modelManager, location));
    }

    static TransformedModel of(ItemStack item, Transformation transform) {
        return new TransformedModel.Item(item, transform);
    }
}
