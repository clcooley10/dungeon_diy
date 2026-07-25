package net.drdooley.dungeon_diy.Item;

import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.ItemStack;


public class AncientBookItem extends BookItem {

    public AncientBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
