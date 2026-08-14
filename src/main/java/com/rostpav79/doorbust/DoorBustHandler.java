package com.rostpav79.doorbust;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

public class DoorBustHandler {

    private final WeakHashMap<Player, Integer> sprintTicks = new WeakHashMap<>();
    private final Map<ResourceKey<Level>, Map<BlockPos, Long>> doorsToClose = new HashMap<>();
    private final Map<ResourceKey<Level>, Map<BlockPos, Long>> bustCooldown = new HashMap<>();

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }

        Player player = event.player;
        if (player.isSprinting()) {
            int ticks = sprintTicks.getOrDefault(player, 0) + 1;
            sprintTicks.put(player, ticks);

            if (ticks > 5) {
                Level level = player.level();

                net.minecraft.world.phys.Vec3 move = player.getDeltaMovement();
                net.minecraft.world.phys.Vec3 look = player.getLookAngle();
                net.minecraft.world.phys.Vec3 dir;
                if (Math.abs(move.x) + Math.abs(move.z) < 0.01D) {
                    dir = new net.minecraft.world.phys.Vec3(look.x, 0.0, look.z);
                } else {
                    double len = Math.sqrt(move.x * move.x + move.z * move.z);
                    dir = new net.minecraft.world.phys.Vec3(move.x / len, 0.0, move.z / len);
                }

                AABB box = player.getBoundingBox().expandTowards(dir.x, 0.0, dir.z).inflate(0.1D, 0.0D, 0.1D);

                Map<BlockPos, Long> cooldowns = bustCooldown.computeIfAbsent(level.dimension(), k -> new HashMap<>());
                boolean[] busted = {false};

                BlockPos.betweenClosedStream(box).forEach(pos -> {
                    BlockState state = level.getBlockState(pos);
                    if (state.getBlock() instanceof DoorBlock && state.is(BlockTags.WOODEN_DOORS)) {
                        if (!state.getValue(DoorBlock.OPEN)) {
                            BlockPos immPos = pos.immutable();

                            Long cooldownUntil = cooldowns.get(immPos);
                            if (cooldownUntil != null && level.getGameTime() < cooldownUntil) {
                                return;
                            }

                            ((DoorBlock) state.getBlock()).setOpen(player, level, state, pos, true);
                            cooldowns.put(immPos, level.getGameTime() + 40);

                            if (!busted[0]) {
                                level.playSound(null, pos, DoorBustMod.DOOR_BUST.get(), SoundSource.BLOCKS, 1.0F,
                                        level.random.nextFloat() * 0.1F + 0.9F);
                                busted[0] = true;

                                if (DoorBustConfig.CAMERA_SHAKE.get()) {
                                    double sx = pos.getX() + 0.5D;
                                    double sy = pos.getY() + 0.5D;
                                    double sz = pos.getZ() + 0.5D;
                                    DoorBustNetworking.CHANNEL.send(
                                            net.minecraftforge.network.PacketDistributor.PLAYER.with(() ->
                                                    (net.minecraft.server.level.ServerPlayer) player),
                                            new BustShakePacket(1.0F, sx, sy, sz));
                                }
                            }

                            if (DoorBustConfig.AUTO_CLOSE.get()) {
                                doorsToClose.computeIfAbsent(level.dimension(), k -> new HashMap<>())
                                        .put(immPos, level.getGameTime() + 30);
                            }
                        }
                    }
                });
            }
        } else {
            sprintTicks.remove(player);
        }
    }

    @SubscribeEvent
    public void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide)
            return;

        Map<BlockPos, Long> levelDoors = doorsToClose.get(event.level.dimension());
        if (levelDoors != null && !levelDoors.isEmpty()) {
            long time = event.level.getGameTime();
            Iterator<Map.Entry<BlockPos, Long>> it = levelDoors.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<BlockPos, Long> entry = it.next();
                if (time >= entry.getValue()) {
                    BlockPos pos = entry.getKey();
                    BlockState state = event.level.getBlockState(pos);
                    if (state.getBlock() instanceof DoorBlock && state.is(BlockTags.WOODEN_DOORS)
                            && state.getValue(DoorBlock.OPEN)) {
                        ((DoorBlock) state.getBlock()).setOpen(null, event.level, state, pos, false);
                        event.level.playSound(null, pos, SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F,
                                event.level.random.nextFloat() * 0.1F + 0.9F);
                    }
                    it.remove();
                }
            }
        }

        Map<BlockPos, Long> cooldowns = bustCooldown.get(event.level.dimension());
        if (cooldowns != null && !cooldowns.isEmpty()) {
            long time = event.level.getGameTime();
            cooldowns.entrySet().removeIf(e -> time >= e.getValue());
        }
    }
}
