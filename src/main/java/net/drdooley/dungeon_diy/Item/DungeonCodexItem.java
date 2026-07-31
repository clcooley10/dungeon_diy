package net.drdooley.dungeon_diy.Item;

import net.drdooley.dungeon_diy.Block.AncientVaultBlockEntity;
import net.drdooley.dungeon_diy.Component.DDIYDataComponents;
import net.drdooley.dungeon_diy.Dungeon.DungeonInstance;
import net.drdooley.dungeon_diy.Dungeon.DungeonManager;
import net.drdooley.dungeon_diy.Dungeon.DungeonNode;
import net.drdooley.dungeon_diy.Dungeon.DungeonSavedData;
import net.drdooley.dungeon_diy.screen.DungeonCodexMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WritableBookItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.UUID;


public class DungeonCodexItem extends BookItem {
    public DungeonCodexItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        // Vaults have special bind interaction with codex, should not end up in dungeon config
        if (level.getBlockEntity(context.getClickedPos()) instanceof AncientVaultBlockEntity) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            BlockPos blockPos = context.getClickedPos();
            UUID dungeonID = context.getItemInHand().get(DDIYDataComponents.DUNGEON_ID);
            DungeonInstance dungeonInstance = DungeonManager.getDungeon((ServerLevel) level, dungeonID);
            boolean added = dungeonInstance.toggleNode(blockPos);
            if (added) {
                context.getPlayer().sendSystemMessage(Component.translatable("item.dungeon_diy.dungeon_codex.added_node", blockPos.toShortString()));
            } else {
                context.getPlayer().sendSystemMessage(Component.translatable("item.dungeon_diy.dungeon_codex.removed_node", blockPos.toShortString()));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }
        ServerPlayer serverPlayer = (ServerPlayer) player;
        UUID dungeonId = stack.get(DDIYDataComponents.DUNGEON_ID);
        if (dungeonId == null) {
            player.sendSystemMessage(Component.translatable("item.dungeon_diy.dungeon_codex.click_no_dungeon"));
            return InteractionResultHolder.fail(stack);
        }
        serverPlayer.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("menu.dungeon_diy.dungeon_codex");
            }
            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory inv, Player player) {
                return new DungeonCodexMenu(containerId, inv, dungeonId);
            }
        }, buf -> {
            buf.writeUUID(dungeonId);
        });
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
