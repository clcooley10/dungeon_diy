package net.drDooley.dungeon_diy.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.drDooley.dungeon_diy.DDIY;
import net.drDooley.dungeon_diy.item.DDIY_Items;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;

public class DungeonTeleporterBlockEntityRenderer implements BlockEntityRenderer<DungeonTeleporterEntity> {

    public DungeonTeleporterBlockEntityRenderer(BlockEntityRendererProvider.Context context) { }

    @Override
    public void render(DungeonTeleporterEntity be, float ptick, PoseStack poseStack, MultiBufferSource buf, int light, int overlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        ItemStack stack = new ItemStack(be.getEye().getItem());
        LocalPlayer player = Minecraft.getInstance().player;
        poseStack.pushPose();
        poseStack.translate(0.5155f, 0.485f, 0.5f);

        poseStack.scale(0.5f, 0.5f, 0.5f);

        itemRenderer.renderStatic(player, stack, ItemTransforms.TransformType.FIXED, false, poseStack, buf,
                Minecraft.getInstance().level, light, overlay, 0);
        poseStack.popPose();
    }
}
