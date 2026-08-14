package com.rostpav79.doorbust;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(DoorBustMod.MODID)
public class DoorBustMod {
    public static final String MODID = "doorbust";

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);

    public static final RegistryObject<SoundEvent> DOOR_BUST = SOUND_EVENTS.register("door_bust", 
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "door_bust")));

    public DoorBustMod() {
        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, DoorBustConfig.SPEC);
        
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        SOUND_EVENTS.register(modEventBus);
        DoorBustNetworking.register();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new DoorBustHandler());
    }
}
