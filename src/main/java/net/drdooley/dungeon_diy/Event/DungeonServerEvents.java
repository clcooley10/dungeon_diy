package net.drdooley.dungeon_diy.Event;


import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.drdooley.dungeon_diy.Dungeon.DungeonInstance;
import net.drdooley.dungeon_diy.Dungeon.DungeonManager;
import net.drdooley.dungeon_diy.Dungeon.DungeonNode;
import net.drdooley.dungeon_diy.DungeonDIY;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.UUID;


@EventBusSubscriber(modid = DungeonDIY.MOD_ID)
public class DungeonServerEvents {
    //TODO: Overworld may not be right, also may not even use tick
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post e) {
        DungeonManager.tickDungeons(e.getServer().overworld());
    }

    @SubscribeEvent
    public static void onRegisterComands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
          Commands.literal("dungeon-diy")
            .then(Commands.literal("create").executes(ctx -> {
                DungeonManager.createDungeon(ctx.getSource().getLevel());
                return 1;
            }))
            .then(Commands.literal("list").executes(ctx -> {
                DungeonManager.printDungeons(ctx.getSource());
                return 1;
            }))
            .then(Commands.literal("node")
              .then(Commands.literal("add")
                .then(Commands.argument("dungeon_id", StringArgumentType.string())
                  .then(Commands.argument("X", IntegerArgumentType.integer())
                    .then(Commands.argument("Y", IntegerArgumentType.integer())
                      .then(Commands.argument("Z", IntegerArgumentType.integer())
                        .executes(ctx -> {
                            String idStr = StringArgumentType.getString(ctx, "dungeon_id");
                            UUID id = UUID.fromString(idStr);
                            DungeonInstance dungeon = DungeonManager.getDungeon(ctx.getSource().getLevel(), id);
                            if (dungeon == null) {
                                ctx.getSource().sendFailure(Component.literal("Dungeon not found: " + idStr));
                                return 0;
                            }
                            BlockPos pos = new BlockPos(IntegerArgumentType.getInteger(ctx, "X"), IntegerArgumentType.getInteger(ctx, "Y"), IntegerArgumentType.getInteger(ctx, "Z"));
                            BlockState state = ctx.getSource().getLevel().getBlockState(pos);
                            dungeon.addNode(pos, state);
                            ctx.getSource().sendSuccess(() -> Component.literal("Node added to dungeon " + idStr), false);
                            return 1;

                        })
                      )
                    )
                  )
                )
              )
            )
        );


    }


}
