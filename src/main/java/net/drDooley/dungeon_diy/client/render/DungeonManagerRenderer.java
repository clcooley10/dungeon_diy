package net.drDooley.dungeon_diy.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.drDooley.dungeon_diy.DDIY;
import net.drDooley.dungeon_diy.block.entity.DungeonManagerEntity;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DungeonManagerRenderer implements BlockEntityRenderer<DungeonManagerEntity> {
    private static final ResourceLocation BOOK_LOCATION = new ResourceLocation(DDIY.MODID, "textures/entity/dungeon_manager_rulebook.png");
    private final BookModel bookModel;

    public DungeonManagerRenderer(BlockEntityRendererProvider.Context pContext) {
        this.bookModel = new BookModel(pContext.bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public void render(DungeonManagerEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        if (pBlockEntity.getBook().isEmpty()) { return; }

        pPoseStack.pushPose();
        pPoseStack.translate(0.5D, 0.75D, 0.5D);
        float f = (float)pBlockEntity.time + pPartialTick;
        pPoseStack.translate(0.0D, (double)(0.1F + Mth.sin(f * 0.1F) * 0.01F), 0.0D);

        float f1;
        for(f1 = pBlockEntity.rot - pBlockEntity.oRot; f1 >= (float)Math.PI; f1 -= ((float)Math.PI * 2F)) {
        }

        while(f1 < -(float)Math.PI) {
            f1 += ((float)Math.PI * 2F);
        }

        float f2 = pBlockEntity.oRot + f1 * pPartialTick;
        pPoseStack.mulPose(Vector3f.YP.rotation(-f2));
        pPoseStack.mulPose(Vector3f.ZP.rotationDegrees(80.0F));
        float f3 = Mth.lerp(pPartialTick, pBlockEntity.oFlip, pBlockEntity.flip);
        float f4 = Mth.frac(f3 + 0.25F) * 1.6F - 0.3F;
        float f5 = Mth.frac(f3 + 0.75F) * 1.6F - 0.3F;
        float f6 = Mth.lerp(pPartialTick, pBlockEntity.oOpen, pBlockEntity.open);
        this.bookModel.setupAnim(f, Mth.clamp(f4, 0.0F, 1.0F), Mth.clamp(f5, 0.0F, 1.0F), f6);
        VertexConsumer vertexconsumer = pBufferSource.getBuffer(RenderType.entityCutoutNoCull(BOOK_LOCATION));
        // Make a glowing color effect using sine
        float glow = (float) (0.9 + 0.1 * Math.sin((float)pBlockEntity.time * 0.2));

        this.bookModel.render(pPoseStack, vertexconsumer, pPackedLight, pPackedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        pPoseStack.popPose();
    }
}
