// SPDX-FileCopyrightText: 2022 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package dan200.computercraft.client.platform;

import com.google.auto.service.AutoService;
import com.mojang.blaze3d.vertex.PoseStack;
import dan200.computercraft.client.render.ModelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

@AutoService(dan200.computercraft.impl.client.ClientPlatformHelper.class)
public class ClientPlatformHelperImpl implements ClientPlatformHelper {
    private static final RandomSource random = RandomSource.create(0);
    private static final Direction[] directions = Arrays.copyOf(Direction.values(), 7);

    @Override
    public BakedModel getModel(ModelManager manager, ResourceLocation resourceLocation) {
        return manager.getStandaloneModel(resourceLocation);
    }

    @Override
    public void renderBakedModel(PoseStack transform, MultiBufferSource buffers, BakedModel model, int lightmapCoord, int overlayLight, int @Nullable [] tints) {
        var renderType = model.getRenderType(ItemStack.EMPTY);
        var buffer = buffers.getBuffer(renderType);
        for (var face : directions) {
            random.setSeed(42);
            var quads = model.getQuads(null, face, random, ModelData.EMPTY, renderType);
            ModelRenderer.renderQuads(transform, buffer, quads, lightmapCoord, overlayLight, tints);
        }
    }
}
