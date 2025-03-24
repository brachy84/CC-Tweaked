/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.client.render;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.media.items.ItemPrintout;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderItemInFrameEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import static dan200.computercraft.client.gui.FixedWidthFontRenderer.FONT_HEIGHT;
import static dan200.computercraft.client.gui.FixedWidthFontRenderer.FONT_WIDTH;
import static dan200.computercraft.client.render.PrintoutRenderer.*;
import static dan200.computercraft.shared.media.items.ItemPrintout.LINES_PER_PAGE;
import static dan200.computercraft.shared.media.items.ItemPrintout.LINE_MAX_LENGTH;

/**
 * Emulates map and item-frame rendering for printouts.
 */
@Mod.EventBusSubscriber(modid = ComputerCraft.MOD_ID, value = Side.CLIENT)
public final class ItemPrintoutRenderer extends ItemMapLikeRenderer {

    private static final ItemPrintoutRenderer INSTANCE = new ItemPrintoutRenderer();

    private ItemPrintoutRenderer() {
    }

    @SubscribeEvent
    public static void onRenderInHand(RenderSpecificHandEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() != ComputerCraft.Items.printout) return;

        event.setCanceled(true);

        INSTANCE.renderItemFirstPerson(event.getHand(), event.getInterpolatedPitch(), event.getEquipProgress(), event.getSwingProgress(),
                                       event.getItemStack());
    }

    @Override
    protected void renderItem(ItemStack stack) {
        // Setup various transformations. Note that these are partially adapated from the corresponding method
        // in ItemRenderer.renderMapFirstPerson
        GlStateManager.disableLighting();

        GlStateManager.rotate(180f, 0f, 1f, 0f);
        GlStateManager.rotate(180f, 0f, 0f, 1f);
        GlStateManager.scale(0.42f, 0.42f, -0.42f);
        GlStateManager.translate(-0.5f, -0.48f, 0.0f);

        drawPrintout(stack);

        GlStateManager.enableLighting();
    }

    @SubscribeEvent
    public static void onRenderInFrame(RenderItemInFrameEvent event) {
        ItemStack stack = event.getItem();
        if (stack.getItem() != ComputerCraft.Items.printout) return;

        event.setCanceled(true);

        GlStateManager.disableLighting();

        // Move a little bit forward to ensure we're not clipping with the frame
        GlStateManager.translate(0.0f, 0.0f, -0.001f);
        GlStateManager.rotate(180f, 0f, 0f, 1f);
        GlStateManager.scale(0.95f, 0.95f, -0.95f);
        GlStateManager.translate(-0.5f, -0.5f, 0.0f);

        drawPrintout(stack);

        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
    }

    private static void drawPrintout(ItemStack stack) {
        int pages = ItemPrintout.getPageCount(stack);
        boolean book = ItemPrintout.getType(stack) == ItemPrintout.Type.Book;

        double width = LINE_MAX_LENGTH * FONT_WIDTH + X_TEXT_MARGIN * 2;
        double height = LINES_PER_PAGE * FONT_HEIGHT + Y_TEXT_MARGIN * 2;

        // Non-books will be left aligned
        if (!book) width += offsetAt(pages);

        double visualWidth = width, visualHeight = height;

        // Meanwhile books will be centred
        if (book) {
            visualWidth += 2 * COVER_SIZE + 2 * offsetAt(pages);
            visualHeight += 2 * COVER_SIZE;
        }

        double max = Math.max(visualHeight, visualWidth);

        // Scale the printout to fit correctly.
        double scale = 1.0 / max;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate((max - width) / 2.0f, (max - height) / 2.0f, 0.0f);

        drawBorder(0, 0, -0.01, 0, pages, book);
        drawText(X_TEXT_MARGIN, Y_TEXT_MARGIN, 0, ItemPrintout.getText(stack), ItemPrintout.getColours(stack));
    }
}
