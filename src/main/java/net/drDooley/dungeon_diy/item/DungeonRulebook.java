package net.drDooley.dungeon_diy.item;

import net.drDooley.dungeon_diy.DDIY;
import net.drDooley.dungeon_diy.block.DDIY_Blocks;
import net.drDooley.dungeon_diy.block.DungeonManagerBlock;
import net.drDooley.dungeon_diy.dungeon.DataManager;
import net.drDooley.dungeon_diy.dungeon.DungeonConfig;
import net.drDooley.dungeon_diy.networking.DDIY_Packets;
import net.drDooley.dungeon_diy.networking.DungeonConfigSync_S2C;
import net.drDooley.dungeon_diy.screen.DungeonConfigMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkHooks;

import java.util.UUID;

public class DungeonRulebook extends Item {
    public DungeonRulebook(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        DDIY.LOGGER.info("Enter useOn");
        Level level = pContext.getLevel();
        BlockPos blockPos = pContext.getClickedPos();
        BlockState blockState = level.getBlockState(blockPos);
        ItemStack stack = pContext.getItemInHand();

        // We clicked on a Manager block...
        if (blockState.is(DDIY_Blocks.DUNGEON_MANAGER.get())) {
            // ...that does not currently have a book inserted
            if (!blockState.getValue(DungeonManagerBlock.HAS_BOOK)) {
                if (!level.isClientSide) {
                    ((DungeonManagerBlock) DDIY_Blocks.DUNGEON_MANAGER.get()).setBook(pContext.getPlayer(), level, blockPos, blockState, stack);
                    level.levelEvent((Player) null, 1010, blockPos, Item.getId(this));
                    stack.shrink(1);
                    Player player = pContext.getPlayer();
                    if (player != null) {
                        //player.awardStat(Stats.DUNGEONS_MANAGED);
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        // We clicked on any other block, add it to the dungeon registry
        else {
            DDIY.LOGGER.info("Enter else (not Manager block)");
            if (!stack.hasTag() || !stack.getTag().hasUUID("DungeonId")) { return InteractionResult.FAIL; }

            UUID id = stack.getTag().getUUID("DungeonId");
            if (pContext.getLevel() instanceof ServerLevel serverLevel) {
                DataManager manager = DataManager.get(serverLevel);
                DungeonConfig config = manager.getDungeon(id);
                config.addBlock(pContext.getClickedPos());
                manager.setDirty();
            }

            // If they were crouching, also open the GUI
            if (pContext.getPlayer().isCrouching() && !pContext.getLevel().isClientSide) {
                ServerPlayer serverPlayer = (ServerPlayer) pContext.getPlayer();
                ServerLevel serverLevel = (ServerLevel) pContext.getLevel();
                DataManager manager = DataManager.get(serverLevel);
                DungeonConfig config = manager.getDungeon(id);

                NetworkHooks.openScreen(serverPlayer,
                        new SimpleMenuProvider((windowId, inv, p) ->
                                new DungeonConfigMenu(windowId, inv, id),
                                    Component.literal("Dungeon Management")
                        ),
                        buf -> buf.writeUUID(id)
                );

                DDIY_Packets.sendToPlayer(new DungeonConfigSync_S2C(id, config), serverPlayer);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        DDIY.LOGGER.info("Enter use");

        ItemStack stack = pPlayer.getItemInHand(pUsedHand);
        if (!pLevel.isClientSide) {
            DDIY.LOGGER.info("Opening GUI");

            ServerLevel serverLevel = (ServerLevel) pLevel;
            DataManager manager = DataManager.get(serverLevel);
            UUID id = stack.getTag().getUUID("DungeonId");
            DungeonConfig config = manager.getDungeon(id);

            ServerPlayer serverPlayer = (ServerPlayer) pPlayer;
            NetworkHooks.openScreen(serverPlayer,
                    new SimpleMenuProvider((windowId, inv, p) ->
                            new DungeonConfigMenu(windowId, inv, id),
                            Component.literal("Dungeon Management")
                    ),
                    buf -> buf.writeUUID(id)
            );

            DDIY_Packets.sendToPlayer(new DungeonConfigSync_S2C(id, config), serverPlayer);

            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }
}

