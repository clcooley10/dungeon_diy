package net.drdooley.dungeon_diy.Item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;


public class DungeonCodexItem extends BookItem {
    public DungeonCodexItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
