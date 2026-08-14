package com.rostpav79.doorbust;

import net.minecraft.client.Minecraft;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DoorBustMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientCameraShake {

    private static final RandomSource RANDOM = RandomSource.create();

    private static final float PUNCH_DURATION = 0.30F;
    private static final float PUNCH_DECAY = 0.70F;

    private static float punchPitch;
    private static float punchYaw;
    private static float punchRoll;
    private static float punchTime;

    private static float shakeTime;
    private static float shakeFreq;
    private static float shakeAmp;
    private static double shakeX;
    private static double shakeY;
    private static double shakeZ;

    public static void trigger(double x, double y, double z, float strength) {
        shakeX = x;
        shakeY = y;
        shakeZ = z;

        boolean left = RANDOM.nextBoolean();
        punchPitch = 9.0F * strength;
        punchYaw = (left ? 10.0F : -10.0F) * strength;
        punchRoll = (left ? -10.0F : 10.0F) * strength;
        punchTime = PUNCH_DURATION;

        shakeTime = 0.25F;
        shakeFreq = 12.0F;
        shakeAmp = 0.30F * strength;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (punchTime > 0.0F) {
            punchTime -= 0.05F;
            punchPitch *= PUNCH_DECAY;
            punchYaw *= PUNCH_DECAY;
            punchRoll *= PUNCH_DECAY;
            if (punchTime <= 0.0F) {
                punchTime = 0.0F;
                punchPitch = 0.0F;
                punchYaw = 0.0F;
                punchRoll = 0.0F;
            }
        }

        shakeTime = Math.max(0.0F, shakeTime - 0.05F);
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        float yaw = event.getYaw();
        float pitch = event.getPitch();
        float roll = event.getRoll();

        if (punchTime > 0.0F) {
            yaw += punchYaw;
            pitch += punchPitch;
            roll += punchRoll;
        }

        if (shakeTime > 0.0F) {
            double dist = mc.player.distanceToSqr(shakeX, shakeY, shakeZ);
            float falloff = (float) Math.max(0.0, 1.0 - Math.sqrt(dist) / 12.0);
            float t = (1.0F - shakeTime / 0.25F) * (float) Math.PI * shakeFreq;
            float amp = shakeAmp * falloff * (shakeTime / 0.25F);
            yaw += (float) Math.sin(t) * amp * 2.0F;
            pitch += (float) Math.sin(t * 1.3F + 1.0F) * amp;
            roll += (float) Math.sin(t * 0.7F + 2.0F) * amp;
        }

        event.setYaw(yaw);
        event.setPitch(pitch);
        event.setRoll(roll);
    }
}