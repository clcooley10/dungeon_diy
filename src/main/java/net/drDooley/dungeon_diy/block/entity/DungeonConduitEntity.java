package net.drDooley.dungeon_diy.block.entity;

import net.drDooley.dungeon_diy.DDIY;
import net.drDooley.dungeon_diy.block.DDIY_Blocks;
import net.drDooley.dungeon_diy.block.DungeonConduitBlock;
import net.drDooley.dungeon_diy.event.GongRungEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;


@Mod.EventBusSubscriber(modid = DDIY.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DungeonConduitEntity extends BlockEntity {
    private ItemStack reactiveEye = ItemStack.EMPTY;
    private ArrayList<BlockPos> linkedBlocks = new ArrayList<>();

    public DungeonConduitEntity(BlockPos pPos, BlockState pBlockState) {
        super(DDIY_Blocks.DUNGEON_CONDUIT_ENTITY.get(), pPos, pBlockState);
        //MinecraftForge.EVENT_BUS.register(new DungeonConduitBlock(null));
    }

    public void updateLinkedBlocks(CompoundTag nbt) {
        // Also handles sending our position to linked blocks so they can reach us later
        ArrayList<BlockPos> newBlocks = new ArrayList<>();
        ListTag blocksList = nbt.getList("linkedBlocks", Tag.TAG_COMPOUND);
        for (Tag t : blocksList) {
            CompoundTag block = (CompoundTag) t;
            BlockPos linkedPos = new BlockPos(block.getInt("x"), block.getInt("y"), block.getInt("z"));
            newBlocks.add(linkedPos);
        }

        linkedBlocks = newBlocks;
        setChanged();

        if (level != null) {
            // Tell our linkedBlocks where to find us (not all blocks will have a method to handle this)
            for (BlockPos memberPos : linkedBlocks) {
                try {
                    BlockEntity entity = level.getBlockEntity(memberPos);
                    if (entity instanceof DungeonLink linkedEntity) {
                        linkedEntity.registerConduit(this.worldPosition);
                    }
                } catch (NullPointerException ignored) { }
            }
        }
    }

    public ArrayList<BlockPos> getLootSources() {
        ArrayList<BlockPos> ret = new ArrayList<BlockPos>();
        for (BlockPos pos : linkedBlocks) {
            try {
                if (level.getBlockState(pos).is(Tags.Blocks.CHESTS)) {
                    ret.add(pos);
                }
            } catch (NullPointerException ignored) {}
        }
        return ret;
    }

    // Jukebox and RecordItem for handling Eye
    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);

        if (pTag.contains("reactiveEye", Tag.TAG_COMPOUND)) {
            this.setEye(ItemStack.of(pTag.getCompound("reactiveEye")));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);

        if (!this.getEye().isEmpty()) {
            pTag.put("reactiveEye", this.getEye().save(new CompoundTag()));
        }

        if (!linkedBlocks.isEmpty()) {
            ListTag blocksList = new ListTag();
            for (BlockPos pos : linkedBlocks) {
                CompoundTag block = new CompoundTag();
                block.putInt("x", pos.getX());
                block.putInt("y", pos.getY());
                block.putInt("z", pos.getZ());
                blocksList.add(block);
            }
            pTag.put("linkedBlocks", blocksList);
        }
    }

    @SubscribeEvent
    public void gongRung(GongRungEvent event) {
        DDIY.LOGGER.info("Heard gong ring from " + event.getPos().toShortString());
        if (event.getLevel().isClientSide) return;

        BlockPos clickedPos = event.getPos();
        for (BlockPos pos : linkedBlocks) {
            if (pos == clickedPos) {
                BlockPos ourPos = this.getBlockPos();
                event.getEntity().sendSystemMessage(Component.literal("Dungeon Conduit at " + ourPos.toShortString() + " detected Gong Rung at " + clickedPos));
                event.getLevel().getBlockState(ourPos).setValue(DungeonConduitBlock.ACTIVE, Boolean.valueOf(true));
                break;
            }
        }
        setDungeonActive();
    }

    private void setDungeonActive() {
        Level level = this.getLevel();
        for (BlockPos pos : linkedBlocks) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof RequirementDoorEntity door) {
                door.setActive();
            } else if (be instanceof LootChestEntity chest) {
                chest.setActive();
            } else {
                DDIY.LOGGER.info("Tried to activate unexpected block: " + level.getBlockState(pos).toString());
            }
        }
    }

    public void setEye(ItemStack eye) {
        this.reactiveEye = eye;
        this.updateLinkedBlocks(eye.getOrCreateTag());
        this.setChanged();
    }

    public ItemStack getEye() { return this.reactiveEye; }

    public void removedEye() {
        this.setEye(ItemStack.EMPTY);
    }
}
