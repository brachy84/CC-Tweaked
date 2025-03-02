// SPDX-FileCopyrightText: 2018 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package dan200.computercraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dan200.computercraft.shared.ModRegistry;
import dan200.computercraft.shared.media.items.PrintoutData;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.world.item.ItemStack;

import static dan200.computercraft.client.render.PrintoutRenderer.*;
import static dan200.computercraft.client.render.text.FixedWidthFontRenderer.FONT_HEIGHT;
import static dan200.computercraft.client.render.text.FixedWidthFontRenderer.FONT_WIDTH;
import static dan200.computercraft.shared.media.items.PrintoutData.LINES_PER_PAGE;
import static dan200.computercraft.shared.media.items.PrintoutData.LINE_LENGTH;

/**
 * Emulates map and item-frame rendering for printouts.
 */
public final class PrintoutItemRenderer extends ItemMapLikeRenderer {
    public static final PrintoutItemRenderer INSTANCE = new PrintoutItemRenderer();

    private PrintoutItemRenderer() {
    }

    @Override
    protected void renderItem(PoseStack transform, MultiBufferSource render, ItemStack stack, int light) {
        transform.mulPose(Axis.XP.rotationDegrees(180f));
        transform.scale(0.42f, 0.42f, -0.42f);
        transform.translate(-0.5f, -0.48f, 0.0f);

        drawPrintout(transform, render, PrintoutData.getOrEmpty(stack), stack.getItem() == ModRegistry.Items.PRINTED_BOOK.get(), light);
    }

    public static void onRenderInFrame(PoseStack transform, MultiBufferSource render, ItemFrameRenderState frame, PrintoutData data, boolean isBook, int packedLight) {
        // Move a little bit forward to ensure we're not clipping with the frame
        transform.translate(0.0f, 0.0f, -0.001f);
        transform.mulPose(Axis.ZP.rotationDegrees(180f));
        transform.scale(0.95f, 0.95f, -0.95f);
        transform.translate(-0.5f, -0.5f, 0.0f);

        var light = frame.isGlowFrame ? 0xf000d2 : packedLight; // See getLightCoords.
        drawPrintout(transform, render, data, isBook, light);
    }

    private static void drawPrintout(PoseStack transform, MultiBufferSource render, PrintoutData pageData, boolean book, int light) {
        var pages = pageData.pages();

        double width = LINE_LENGTH * FONT_WIDTH + X_TEXT_MARGIN * 2;
        double height = LINES_PER_PAGE * FONT_HEIGHT + Y_TEXT_MARGIN * 2;

        // Non-books will be left aligned
        if (!book) width += offsetAt(pages - 1);

        double visualWidth = width, visualHeight = height;

        // Meanwhile books will be centred
        if (book) {
            visualWidth += 2 * COVER_SIZE + 2 * offsetAt(pages);
            visualHeight += 2 * COVER_SIZE;
        }

        var max = Math.max(visualHeight, visualWidth);

        // Scale the printout to fit correctly.
        var scale = (float) (1.0 / max);
        transform.scale(scale, scale, scale);
        transform.translate((max - width) / 2.0, (max - height) / 2.0, 0.0);

        drawBorder(transform, render, 0, 0, -0.01f, 0, pages, book, light);
        drawText(transform, render, X_TEXT_MARGIN, Y_TEXT_MARGIN, 0, light, pageData.lines());
    }
}
