package net.drDooley.dungeon_diy.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.drDooley.dungeon_diy.block.LootChestBlock;
import net.drDooley.dungeon_diy.block.entity.LootChestEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LootChestRenderer<T extends LootChestEntity & LidBlockEntity> extends ChestRenderer<T> {
    private final ModelPart chestLid;
    private final ModelPart chestBase;
    private final ModelPart chestLock;

    public LootChestRenderer(BlockEntityRendererProvider.Context pContext) {
        super(pContext);
        ModelPart modelPart = pContext.bakeLayer(ModelLayers.CHEST);
        this.chestBase = modelPart.getChild("bottom");
        this.chestLid = modelPart.getChild("lid");
        this.chestLock = modelPart.getChild("lock");
    }

    public void render(T entity, float tickDelta, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        LootChestEntity lootEntity = (LootChestEntity) entity;
        Level level = entity.getLevel();
        boolean bl = level != null;
        BlockState blockState = bl ? lootEntity.getBlockState() : (BlockState) Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
        if (blockState.getBlock() instanceof LootChestBlock block) {
            poseStack.pushPose();
            float f = ((Direction)blockState.getValue(ChestBlock.FACING)).toYRot();
            poseStack.translate(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(-f));
            poseStack.translate(-0.5F, -0.5F, -0.5F);

            DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> neighborCombineResult;
            neighborCombineResult = DoubleBlockCombiner.Combiner::acceptNone;

//            if (bl) {
//                neighborCombineResult = LootChestBlock.combine(blockState, level, entity.getBlockPos(), true);
//            } else {
//                neighborCombineResult = DoubleBlockCombiner.Combiner::acceptNone;
//            }

            float openness = neighborCombineResult.<Float2FloatFunction>apply(LootChestBlock.opennessCombiner(lootEntity)).get(tickDelta);
            openness = 1.0F - openness;
            openness = 1.0F - openness * openness * openness;

            int brightness = neighborCombineResult.<Int2IntFunction>apply(new BrightnessCombiner<>()).applyAsInt(light);

            //boolean trapped = entity instanceof TrappedLootChestEntity;
            boolean trapped = false;

            Material material = new Material(Sheets.CHEST_SHEET, block.getType().getTextureId());

            VertexConsumer vertexConsumer = material.buffer(bufferSource, RenderType::entityCutout);

            this.render(poseStack, vertexConsumer, this.chestLid, this.chestLock, this.chestBase, openness, brightness, overlay);

            poseStack.popPose();
        }
    }

    private void render(PoseStack poseStack, VertexConsumer vertexConsumer, ModelPart lid, ModelPart lock, ModelPart bottom, float openness, int brightness, int combinedOverlayIn) {
        lid.xRot = -(openness * ((float) Math.PI / 2F));
        lock.xRot = lid.xRot;

        lid.render(poseStack, vertexConsumer, brightness, combinedOverlayIn);
        lock.render(poseStack, vertexConsumer, brightness, combinedOverlayIn);
        bottom.render(poseStack, vertexConsumer, brightness, combinedOverlayIn);
    }
}
