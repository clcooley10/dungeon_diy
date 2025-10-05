package net.drDooley.dungeon_diy.event;

import net.drDooley.dungeon_diy.DDIY;
import net.drDooley.dungeon_diy.enchantment.DDIY_Enchantments;
import net.drDooley.dungeon_diy.item.DDIY_Items;
import net.drDooley.dungeon_diy.item.DungeonRulebook;
import net.drDooley.dungeon_diy.util.DDIY_Utils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DDIY.MODID)
public class DDIY_Events {

    @SubscribeEvent
    public static void onMobDeath(LivingDeathEvent event) {

        Entity killer = event.getSource().getEntity();
        if (!(killer instanceof ServerPlayer player)) return;

        ItemStack offhand = player.getOffhandItem();
        if (!offhand.is(Items.BOOK)) return;

        Entity killed = event.getEntity();
        if (!(killed instanceof Zombie || killed instanceof Skeleton)) return;

        int enchLevel = player.getMainHandItem().getEnchantmentLevel(DDIY_Enchantments.EXTRACTION.get());
        if (enchLevel <= 0) return;

        float chance;
        switch (enchLevel) {
            case 1 -> chance = 0.02F;
            case 2 -> chance = 0.05F;
            case 3 -> chance = 0.10F;
            default -> chance = 0.20F;
        }

        if (player.level.random.nextFloat() < chance) {
            player.displayClientMessage(Component.translatable("message.dungeon_diy.extracted_soul"), true);
            player.getLevel().sendParticles(ParticleTypes.SOUL, killed.getX(), killed.getY() + 1, killed.getZ(), 12, 0.3, 0.3, 0.3, 0.02);
            player.getLevel().playSound(null, killed.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1F, 1F);
            DDIY_Utils.convertOffhandToRulebook(player, InteractionHand.OFF_HAND);
        }
    }
}
