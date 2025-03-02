// SPDX-FileCopyrightText: 2023 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package dan200.computercraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dan200.computercraft.client.platform.ClientPlatformHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Utilities for rendering {@link BakedModel}s and {@link BakedQuad}s.
 */
public final class ModelRenderer {
    private ModelRenderer() {
    }

    /**
     * Render a list of {@linkplain BakedQuad quads} to a buffer.
     * <p>
     * This is not intended to be used directly, but instead by {@link ClientPlatformHelper#renderBakedModel(PoseStack, MultiBufferSource, BakedModel, int, int, int[])}. The
     * implementation here is identical to {@link ItemRenderer#renderQuadList(PoseStack, VertexConsumer, List, int[], int, int)}.
     *
     * @param transform     The current matrix transformation to apply.
     * @param buffer        The buffer to draw to.
     * @param quads         The quads to draw.
     * @param lightmapCoord The current packed lightmap coordinate.
     * @param overlayLight  The current overlay light.
     * @param tints         Block colour tints to apply to the model.
     */
    public static void renderQuads(PoseStack transform, VertexConsumer buffer, List<BakedQuad> quads, int lightmapCoord, int overlayLight, int @Nullable [] tints) {
        var matrix = transform.last();

        for (var bakedquad : quads) {
            float r = 1.0f, g = 1.0f, b = 1.0f, a = 1.0f;
            if (tints != null && bakedquad.isTinted()) {
                var idx = bakedquad.getTintIndex();
                if (idx >= 0 && idx < tints.length) {
                    var tint = tints[bakedquad.getTintIndex()];
                    r = ARGB.red(tint) / 255.0f;
                    g = ARGB.green(tint) / 255.0f;
                    b = ARGB.blue(tint) / 255.0f;
                    a = ARGB.alpha(tint) / 255.0f;
                }
            }

            buffer.putBulkData(matrix, bakedquad, r, g, b, a, lightmapCoord, overlayLight);
        }
    }
}
