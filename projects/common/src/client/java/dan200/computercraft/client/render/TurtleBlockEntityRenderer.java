// Copyright Daniel Ratcliffe, 2011-2022. Do not distribute without permission.
//
// SPDX-License-Identifier: LicenseRef-CCPL

package dan200.computercraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.client.TransformedModel;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.client.platform.ClientPlatformHelper;
import dan200.computercraft.client.turtle.TurtleUpgradeModellers;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.turtle.TurtleOverlay;
import dan200.computercraft.shared.turtle.blocks.TurtleBlockEntity;
import dan200.computercraft.shared.util.Holiday;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

public class TurtleBlockEntityRenderer implements BlockEntityRenderer<TurtleBlockEntity> {
    public static final ResourceLocation NORMAL_TURTLE_MODEL = ResourceLocation.fromNamespaceAndPath(ComputerCraftAPI.MOD_ID, "block/turtle_normal");
    public static final ResourceLocation ADVANCED_TURTLE_MODEL = ResourceLocation.fromNamespaceAndPath(ComputerCraftAPI.MOD_ID, "block/turtle_advanced");
    public static final ResourceLocation COLOUR_TURTLE_MODEL = ResourceLocation.fromNamespaceAndPath(ComputerCraftAPI.MOD_ID, "block/turtle_colour");

    private final BlockEntityRenderDispatcher renderer;
    private final Font font;

    public TurtleBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        renderer = context.getBlockEntityRenderDispatcher();
        font = context.getFont();
    }

    @Override
    public void render(TurtleBlockEntity turtle, float partialTicks, PoseStack transform, MultiBufferSource buffers, int lightmapCoord, int overlayLight) {
        transform.pushPose();

        // Translate the turtle first, so the label moves with it.
        var offset = turtle.getRenderOffset(partialTicks);
        transform.translate(offset.x, offset.y, offset.z);

        // Render the label
        var label = turtle.getLabel();
        var hit = renderer.cameraHitResult;
        if (label != null && hit != null && hit.getType() == HitResult.Type.BLOCK && turtle.getBlockPos().equals(((BlockHitResult) hit).getBlockPos())) {
            var mc = Minecraft.getInstance();
            var font = this.font;

            transform.pushPose();
            transform.translate(0.5, 1.2, 0.5);
            transform.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
            transform.scale(0.025f, -0.025f, 0.025f);

            var matrix = transform.last().pose();
            var opacity = (int) (mc.options.getBackgroundOpacity(0.25f) * 255) << 24;
            var width = -font.width(label) / 2.0f;
            font.drawInBatch(label, width, (float) 0, 0x20ffffff, false, matrix, buffers, Font.DisplayMode.SEE_THROUGH, opacity, lightmapCoord);
            font.drawInBatch(label, width, (float) 0, CommonColors.WHITE, false, matrix, buffers, Font.DisplayMode.NORMAL, 0, lightmapCoord);

            transform.popPose();
        }

        // Then apply rotation and flip if needed.
        transform.translate(0.5f, 0.5f, 0.5f);
        var yaw = turtle.getRenderYaw(partialTicks);
        transform.mulPose(Axis.YP.rotationDegrees(180.0f - yaw));
        transform.translate(-0.5f, -0.5f, -0.5f);

        // Render the turtle
        var colour = turtle.getColour();
        var overlay = turtle.getOverlay();

        if (colour == -1) {
            renderModel(transform, buffers, lightmapCoord, overlayLight, turtle.getFamily() == ComputerFamily.NORMAL ? NORMAL_TURTLE_MODEL : ADVANCED_TURTLE_MODEL, null);
        } else {
            // Otherwise render it using the colour item.
            renderModel(transform, buffers, lightmapCoord, overlayLight, COLOUR_TURTLE_MODEL, new int[]{ ARGB.opaque(colour) });
        }

        // Render the overlay
        if (overlay != null) renderModel(transform, buffers, lightmapCoord, overlayLight, overlay.model(), null);

        // And the Christmas overlay.
        var showChristmas = TurtleOverlay.showElfOverlay(overlay, Holiday.getCurrent() == Holiday.CHRISTMAS);
        if (showChristmas) renderModel(transform, buffers, lightmapCoord, overlayLight, TurtleOverlay.ELF_MODEL, null);

        // Render the upgrades
        renderUpgrade(transform, buffers, lightmapCoord, overlayLight, turtle, TurtleSide.LEFT, partialTicks);
        renderUpgrade(transform, buffers, lightmapCoord, overlayLight, turtle, TurtleSide.RIGHT, partialTicks);

        transform.popPose();
    }

    private void renderUpgrade(PoseStack transform, MultiBufferSource buffers, int lightmapCoord, int overlayLight, TurtleBlockEntity turtle, TurtleSide side, float f) {
        var upgrade = turtle.getUpgrade(side);
        if (upgrade == null) return;
        transform.pushPose();

        var toolAngle = turtle.getToolRenderAngle(side, f);
        transform.translate(0.0f, 0.5f, 0.5f);
        transform.mulPose(Axis.XN.rotationDegrees(toolAngle));
        transform.translate(0.0f, -0.5f, -0.5f);

        switch (TurtleUpgradeModellers.getModel(upgrade, turtle.getAccess(), side)) {
            case TransformedModel.Item model -> {
                transform.mulPose(model.transformation().getMatrix());
                transform.mulPose(Axis.YP.rotation(Mth.PI));
                Minecraft.getInstance().getItemRenderer().renderStatic(
                    model.stack(), ItemDisplayContext.FIXED, lightmapCoord, overlayLight, transform, buffers, turtle.getLevel(), 0
                );
            }

            case TransformedModel.Baked model ->
                renderModel(transform, buffers, lightmapCoord, overlayLight, model.model(), null);
        }


        transform.popPose();
    }

    private void renderModel(PoseStack transform, MultiBufferSource buffers, int lightmapCoord, int overlayLight, ResourceLocation modelLocation, int @Nullable [] tints) {
        var modelManager = Minecraft.getInstance().getModelManager();
        renderModel(transform, buffers, lightmapCoord, overlayLight, ClientPlatformHelper.get().getModel(modelManager, modelLocation), tints);
    }

    /**
     * Render a block model.
     *
     * @param transform     The current matrix stack.
     * @param renderer      The buffer to write to.
     * @param lightmapCoord The current lightmap coordinate.
     * @param overlayLight  The overlay light.
     * @param model         The model to render.
     * @param tints         Tints for the quads, as an array of RGB values.
     * @see net.minecraft.client.renderer.block.ModelBlockRenderer#renderModel
     */
    private void renderModel(PoseStack transform, MultiBufferSource renderer, int lightmapCoord, int overlayLight, BakedModel model, int @Nullable [] tints) {
        ClientPlatformHelper.get().renderBakedModel(transform, renderer, model, lightmapCoord, overlayLight, tints);
    }
}
