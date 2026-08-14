package com.rostpav79.doorbust;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class DoorBustNetworking {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(DoorBustMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        CHANNEL.registerMessage(0, BustShakePacket.class,
                (msg, buf) -> BustShakePacket.encode(msg, buf),
                buf -> BustShakePacket.decode(buf),
                (msg, ctx) -> BustShakePacket.handle(msg, ctx));
    }
}