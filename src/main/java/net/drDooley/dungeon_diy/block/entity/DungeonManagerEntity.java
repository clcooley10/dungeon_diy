package net.drDooley.dungeon_diy.block.entity;

import net.drDooley.dungeon_diy.DDIY;
import net.drDooley.dungeon_diy.block.DDIY_Blocks;
import net.drDooley.dungeon_diy.item.DDIY_Items;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DungeonManagerEntity extends BlockEntity {
    ItemStack rulebook = ItemStack.EMPTY;
    public int time;
    public float flip;
    public float oFlip;
    public float flipT;
    public float flipA;
    public float open;
    public float oOpen;
    public float rot;
    public float oRot;
    public float tRot;
    private static final RandomSource RANDOM = RandomSource.create();

    public DungeonManagerEntity(BlockPos pPos, BlockState pBlockState) {
        super(DDIY_Blocks.DUNGEON_MANAGER_ENTITY.get(), pPos, pBlockState);
    }

    public ItemStack getBook() { return this.rulebook; }

    public void setBook(ItemStack pStack) {
        this.rulebook = pStack;
        this.setChanged();
    }

    public void removeBook() {
        this.rulebook = ItemStack.EMPTY;
        this.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        if (!this.getBook().isEmpty()) {
            pTag.put("RulebookItem", this.getBook().save(new CompoundTag()));
        }
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        if (pTag.contains("RulebookItem", 10)) {
            this.setBook(ItemStack.of(pTag.getCompound("RulebookItem")));
        }
    }

    public static void bookAnimationTick(Level pLevel, BlockPos pPos, BlockState pState, DungeonManagerEntity pBlockEntity) {
        pBlockEntity.oOpen = pBlockEntity.open;
        pBlockEntity.oRot = pBlockEntity.rot;
        Player player = pLevel.getNearestPlayer((double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, 3.0D, false);
        if (player != null) {
            double d0 = player.getX() - ((double)pPos.getX() + 0.5D);
            double d1 = player.getZ() - ((double)pPos.getZ() + 0.5D);
            pBlockEntity.tRot = (float) Mth.atan2(d1, d0);
            pBlockEntity.open += 0.1F;
            if (pBlockEntity.open < 0.5F || RANDOM.nextInt(40) == 0) {
                float f1 = pBlockEntity.flipT;

                do {
                    pBlockEntity.flipT += (float)(RANDOM.nextInt(4) - RANDOM.nextInt(4));
                } while(f1 == pBlockEntity.flipT);
            }
        } else {
            pBlockEntity.tRot += 0.02F;
            pBlockEntity.open -= 0.1F;
        }

        while(pBlockEntity.rot >= (float)Math.PI) {
            pBlockEntity.rot -= ((float)Math.PI * 2F);
        }

        while(pBlockEntity.rot < -(float)Math.PI) {
            pBlockEntity.rot += ((float)Math.PI * 2F);
        }

        while(pBlockEntity.tRot >= (float)Math.PI) {
            pBlockEntity.tRot -= ((float)Math.PI * 2F);
        }

        while(pBlockEntity.tRot < -(float)Math.PI) {
            pBlockEntity.tRot += ((float)Math.PI * 2F);
        }

        float f2;
        for(f2 = pBlockEntity.tRot - pBlockEntity.rot; f2 >= (float)Math.PI; f2 -= ((float)Math.PI * 2F)) {
        }

        while(f2 < -(float)Math.PI) {
            f2 += ((float)Math.PI * 2F);
        }

        pBlockEntity.rot += f2 * 0.4F;
        pBlockEntity.open = Mth.clamp(pBlockEntity.open, 0.0F, 1.0F);
        ++pBlockEntity.time;
        pBlockEntity.oFlip = pBlockEntity.flip;
        float f = (pBlockEntity.flipT - pBlockEntity.flip) * 0.4F;
        float f3 = 0.2F;
        f = Mth.clamp(f, -0.2F, 0.2F);
        pBlockEntity.flipA += (f - pBlockEntity.flipA) * 0.9F;
        pBlockEntity.flip += pBlockEntity.flipA;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        if (!this.getBook().isEmpty()) {
            tag.put("RulebookItem", this.getBook().save(new CompoundTag()));
        } else {
            tag.put("RulebookItem", new CompoundTag());
        }
        return tag;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        CompoundTag tag = pkt.getTag();
        if (tag.contains("RulebookItem", 10)) {
            this.setBook(ItemStack.of(tag.getCompound("RulebookItem")));
        } else {
            this.setBook(ItemStack.EMPTY);
        }
    }
}
