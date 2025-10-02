package net.drDooley.dungeon_diy.networking;

import net.drDooley.dungeon_diy.DDIY;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class DDIY_Packets {
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;
    private static int id() { return packetId++; }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(DDIY.MODID, "packets"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();
        INSTANCE = net;

        net.messageBuilder(ItemStackSync_S2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ItemStackSync_S2C::new)
                .encoder(ItemStackSync_S2C::toBytes)
                .consumerMainThread(ItemStackSync_S2C::handle)
                .add();

        net.messageBuilder(InitLootChest_S2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(InitLootChest_S2C::new)
                .encoder(InitLootChest_S2C::toBytes)
                .consumerMainThread(InitLootChest_S2C::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }
    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
    public static <MSG> void sendToClients(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }
}
