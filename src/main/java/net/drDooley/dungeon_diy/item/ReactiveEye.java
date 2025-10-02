package net.drDooley.dungeon_diy.item;

import net.drDooley.dungeon_diy.DDIY;
import net.drDooley.dungeon_diy.block.DDIY_Blocks;
import net.drDooley.dungeon_diy.block.DungeonConduitBlock;
import net.drDooley.dungeon_diy.block.entity.DungeonConduitEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;

public class ReactiveEye extends Item {
    public ReactiveEye(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        // If we didn't click a block or the block isn't part of the mod, exit early.
        if (context.getLevel().isClientSide) { return InteractionResult.PASS; }

        //TODO: Temporary. Accept all blocks.
        // We need vanilla chests to be included so that loot chests can automatically source them.
        // Later testing will be required to check how best to scale down from All blocks to just chests.
        // Does this include anything with an ItemHandler? Anything with the Forge Chests Tag?

        BlockPos clickedPos = context.getClickedPos();
        BlockState bs = context.getLevel().getBlockState(clickedPos);
        Block block = bs.getBlock();
        /*
        ResourceLocation blockName = ForgeRegistries.BLOCKS.getKey(block);
        if (!blockName.getNamespace().equals(DDIY.MODID)) { return super.onItemUseFirst(stack, context); }
         */
        // Exception to the above rule-- The Dungeon Conduit has no reason to be in the list
        // In fact, we want to avoid it. Right click should insert the Eye
        if (block instanceof DungeonConduitBlock) { return super.onItemUseFirst(stack, context); }


        CompoundTag tag = stack.getOrCreateTag();
        ListTag listTag = tag.getList("linkedBlocks", ListTag.TAG_COMPOUND);

        if (context.getPlayer().isCrouching()) {
            tag.remove("linkedBlocks");
            context.getPlayer().sendSystemMessage(Component.translatable("item.dungeon_diy.reactive_eye.clearAll"));
            return InteractionResult.SUCCESS;
        }

        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag t = (CompoundTag) listTag.get(i);
            if (t.getInt("x") == clickedPos.getX() && t.getInt("y") == clickedPos.getY() && t.getInt("z") == clickedPos.getZ()) {
                listTag.remove(i);

                context.getPlayer().sendSystemMessage(Component.translatable("item.dungeon_diy.reactive_eye.clear", clickedPos.toShortString()));
                return InteractionResult.SUCCESS;
            }
        }

        CompoundTag posTag = new CompoundTag();
        posTag.putInt("x", clickedPos.getX());
        posTag.putInt("y", clickedPos.getY());
        posTag.putInt("z", clickedPos.getZ());
        listTag.add(posTag);
        tag.put("linkedBlocks", listTag);
        context.getPlayer().sendSystemMessage(Component.translatable("item.dungeon_diy.reactive_eye.create", clickedPos.toShortString()));

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        BlockPos blockpos = pContext.getClickedPos();
        BlockState blockstate = level.getBlockState(blockpos);
        if (blockstate.is(DDIY_Blocks.DUNGEON_CONDUIT.get()) && !blockstate.getValue(DungeonConduitBlock.HAS_EYE)) {
            ItemStack itemstack = pContext.getItemInHand();
            if (!level.isClientSide) {
                ((DungeonConduitBlock)DDIY_Blocks.DUNGEON_CONDUIT.get()).setEye(pContext.getPlayer(), level, blockpos, blockstate, itemstack);
                level.levelEvent((Player)null, 1010, blockpos, Item.getId(this));
                itemstack.shrink(1);
                Player player = pContext.getPlayer();
                if (player != null) {
                    player.awardStat(Stats.PLAY_RECORD);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else {
            return InteractionResult.PASS;
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        CompoundTag tag = pStack.getTag();
        if (tag != null && tag.contains("linkedBlocks", tag.TAG_LIST)) {
            int count = tag.getList("linkedBlocks", ListTag.TAG_COMPOUND).size();
            pTooltipComponents.add(Component.translatable("item.dungeon_diy.reactive_eye.tooltip.count",count));
        } else {
            pTooltipComponents.add(Component.translatable("item.dungeon_diy.reactive_eye.tooltip.idle"));
        }
    }
}
