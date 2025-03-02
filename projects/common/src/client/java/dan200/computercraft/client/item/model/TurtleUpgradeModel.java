// SPDX-FileCopyrightText: 2025 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package dan200.computercraft.client.item.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.client.TransformedModel;
import dan200.computercraft.api.client.turtle.TurtleUpgradeModeller;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.client.turtle.TurtleUpgradeModellers;
import dan200.computercraft.shared.turtle.items.TurtleItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * An {@link ItemModel} that renders a turtle upgrade, using its {@link TurtleUpgradeModeller}.
 *
 * @param side The side the upgrade resides on.
 * @param base The base model. Only used to provide item transforms.
 */
public record TurtleUpgradeModel(TurtleSide side, BakedModel base) implements ItemModel {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ComputerCraftAPI.MOD_ID, "turtle/upgrade");
    public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        TurtleSide.CODEC.fieldOf("side").forGetter(Unbaked::side),
        ResourceLocation.CODEC.fieldOf("transforms").forGetter(Unbaked::base)
    ).apply(instance, Unbaked::new));

    @Override
    public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext context, @Nullable ClientLevel level, @Nullable LivingEntity holder, int light) {
        var upgrade = TurtleItem.getUpgradeWithData(stack, side);
        if (upgrade == null) return;

        switch (TurtleUpgradeModellers.getModel(upgrade.upgrade(), upgrade.data(), side)) {
            case TransformedModel.Item model -> {
                var childState = new ItemStackRenderState();
                resolver.updateForTopItem(childState, model.stack(), ItemDisplayContext.NONE, false, level, null, 0);
                if (!childState.isEmpty()) {
                    state.newLayer().setupSpecialModel(new TransformedRenderer(childState, model.transformation()), null, base);
                }
            }
            case TransformedModel.Baked baked ->
                BakedModelWithTransform.addLayer(state, baked.model(), base.getTransforms());
        }
    }

    public record Unbaked(TurtleSide side, ResourceLocation base) implements ItemModel.Unbaked {
        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }

        @Override
        public ItemModel bake(BakingContext bakingContext) {
            return new TurtleUpgradeModel(side, bakingContext.bake(base));
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.resolve(base);
        }
    }

    private record TransformedRenderer(
        ItemStackRenderState state, Transformation transform
    ) implements SpecialModelRenderer<Void> {
        @Override
        public void render(@Nullable Void object, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int overlay, int light, boolean bl) {
            poseStack.pushPose();
            poseStack.mulPose(transform.getMatrix());
            state.render(poseStack, multiBufferSource, overlay, light);
            poseStack.popPose();
        }

        @Override
        public @Nullable Void extractArgument(ItemStack itemStack) {
            return null;
        }
    }
}
