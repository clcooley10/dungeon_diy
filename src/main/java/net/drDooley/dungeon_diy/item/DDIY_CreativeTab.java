package net.drDooley.dungeon_diy.item;

import net.drDooley.dungeon_diy.DDIY;
import net.drDooley.dungeon_diy.block.DDIY_Blocks;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class DDIY_CreativeTab {
    public static final CreativeModeTab DDIY_CREATIVE = new CreativeModeTab(DDIY.MODID) {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(DDIY_Blocks.DRAFTING_TABLE.get());
        }
    };
}