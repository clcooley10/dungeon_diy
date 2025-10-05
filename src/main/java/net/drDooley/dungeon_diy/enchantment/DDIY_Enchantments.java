package net.drDooley.dungeon_diy.enchantment;

import net.drDooley.dungeon_diy.DDIY;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DDIY_Enchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, DDIY.MODID);

    public static RegistryObject<Enchantment> EXTRACTION = ENCHANTMENTS.register("extraction",
            () -> new ExtractionEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.WEAPON, EquipmentSlot.MAINHAND));

    public static void register(IEventBus bus) {
        ENCHANTMENTS.register(bus);
    }
}
