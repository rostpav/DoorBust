package com.rostpav79.doorbust;

import net.minecraftforge.common.ForgeConfigSpec;

public class DoorBustConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue AUTO_CLOSE;
    public static final ForgeConfigSpec.BooleanValue CAMERA_SHAKE;

    static {
        BUILDER.push("Door Bust Settings");

        AUTO_CLOSE = BUILDER.comment("Should doors automatically close after being busted open?")
                .define("autoClose", false);

        CAMERA_SHAKE = BUILDER.comment("Should the camera shake/punch when a door is busted open?")
                .define("cameraShake", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
