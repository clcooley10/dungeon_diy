package net.drDooley.dungeon_diy.screen;

import net.drDooley.dungeon_diy.DDIY;
import net.drDooley.dungeon_diy.dungeon.DungeonConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class DungeonConfigMenu extends AbstractContainerMenu {
    private final UUID dungeonId;
    
    public DungeonConfigMenu(int windowId, Inventory inv, FriendlyByteBuf buf) {
        this(windowId, inv, buf.readUUID());
    }
    public DungeonConfigMenu(int windowId, Inventory inv, UUID dungeonId) {
        super(DDIY_Menus.DUNGEON_CONFIG_MENU.get(), windowId);
        this.dungeonId = dungeonId;

        DDIY.LOGGER.info("dungeonID: " + this.dungeonId);
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return null;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }

    public UUID getDungeonId() { return dungeonId; }
}
