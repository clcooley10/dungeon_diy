package net.drdooley.dungeon_diy.Item;

import net.drdooley.dungeon_diy.Block.AncientVaultBlockEntity;
import net.drdooley.dungeon_diy.Component.DDIYDataComponents;
import net.drdooley.dungeon_diy.Dungeon.*;
import net.drdooley.dungeon_diy.Screen.DungeonCodexMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
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
            // A new node
            if (!dungeonInstance.getNodes().containsKey(blockPos)) {
                boolean added = dungeonInstance.addNode(blockPos, level.getBlockState(blockPos));
                if (added) {
                    context.getPlayer().sendSystemMessage(Component.translatable("item.dungeon_diy.dungeon_codex.added_node", blockPos.toShortString()));
                }
            // Update existing node
            } else {
                DungeonNode node = dungeonInstance.getNodes().get(blockPos);
                BlockState blockState = level.getBlockState(blockPos);
                node.addReplacement(new ReplacementEntry(blockState, 1));
                context.getPlayer().sendSystemMessage(Component.translatable("item.dungeon_diy.dungeon_codex.updated_node", blockPos.toShortString()));
            }
            // If player was crouched while scanning, send block to Vault
            if (context.getPlayer().isShiftKeyDown()) {
                BlockState state = level.getBlockState(blockPos);
                if (state.isAir()) {
                    return InteractionResult.SUCCESS;
                }
                ItemStack stack = new ItemStack(state.getBlock());
                ItemStackHandler vaultHandler = dungeonInstance.getVaultInventory().getHandler();
                for (int slot = 0; slot < vaultHandler.getSlots(); slot++) {
                    ItemStack remainder = vaultHandler.insertItem(slot, stack, false);
                    if (remainder.isEmpty()) {
                        level.destroyBlock(blockPos, false);
                        context.getPlayer().sendSystemMessage(Component.translatable("item.dungeon_diy.dungeon_codex.sent_to_vault", stack.getHoverName().getString()));
                        return InteractionResult.SUCCESS;
                    }
                }
                context.getPlayer().sendSystemMessage(Component.translatable("item.dungeon_diy.dungeon_codex.not_sent_to_vault", stack.getHoverName().getString()).withStyle(ChatFormatting.RED));
            }
        }
        return InteractionResult.SUCCESS;
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
        DungeonInstance instance = DungeonManager.getDungeon((ServerLevel) level, dungeonId);
        ItemStackHandler handler = instance.getVaultInventory().getHandler();
        List<ItemStack> vaultContents = new ArrayList<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack inSlot = handler.getStackInSlot(i);
            if (!inSlot.isEmpty()) {
                vaultContents.add(inSlot.copy());
            }
        }
        serverPlayer.openMenu(new SimpleMenuProvider((containerId, inventory, p) ->
            new DungeonCodexMenu(containerId, inventory, dungeonId, vaultContents),
            Component.translatable("menu.dungeon_diy.dungeon_codex")),
          buf -> {
              buf.writeUUID(dungeonId);
              List<DungeonNode> nodes = new ArrayList<>(instance.getNodes().values());
              buf.writeInt(nodes.size());
              for (DungeonNode node : nodes) {
                  node.writeNetworkData(buf);
              }
              List<ReplacementPrefab> prefabs = new ArrayList<>(instance.getReplPrefabs());
              buf.writeInt(prefabs.size());
              for (ReplacementPrefab prefab : prefabs) {
                  prefab.writeNetworkData(buf);
              }
              buf.writeInt(vaultContents.size());
              for (ItemStack vaultStack : vaultContents) {
                  ItemStack.STREAM_CODEC.encode(buf, vaultStack);
              }
              NonNullList<ItemStack> acceptedStacks = instance.getAcceptedPedestalStacks();
              buf.writeInt(acceptedStacks.size());
              for (ItemStack acceptedStack : acceptedStacks) {
                  ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, acceptedStack);
              }
          }
        );
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
