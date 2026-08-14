package com.rostpav79.doorbust;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BustShakePacket {

    private final float strength;
    private final double x;
    private final double y;
    private final double z;

    public BustShakePacket(float strength, double x, double y, double z) {
        this.strength = strength;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BustShakePacket(FriendlyByteBuf buf) {
        this.strength = buf.readFloat();
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
    }

    public static void encode(BustShakePacket msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.strength);
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
    }

    public static BustShakePacket decode(FriendlyByteBuf buf) {
        return new BustShakePacket(buf);
    }

    public static void handle(BustShakePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientCameraShake.trigger(msg.x, msg.y, msg.z, msg.strength)));
        ctx.get().setPacketHandled(true);
    }
}