package com.undercover.stoneblock_gen;

import com.mojang.logging.LogUtils;
import com.undercover.stoneblock_gen.world.StoneblockChunkGen;
import com.undercover.stoneblock_gen.world.StoneblockWorldPreset;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.ForgeWorldPreset;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

@Mod(StoneblockWorld.MODID)
public class StoneblockWorld {

    public static final String MODID = "stoneblock_gen";
    public static final Logger LOGGER = LogUtils.getLogger();
    public final DeferredRegister<ForgeWorldPreset> WORLD_PRESETS = DeferredRegister.create(ForgeRegistries.Keys.WORLD_TYPES, MODID);

    public StoneblockWorld() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        WORLD_PRESETS.register("stoneblock", () -> new StoneblockWorldPreset(new StoneblockChunkGen()));
        WORLD_PRESETS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("Stoneblock world preset loaded");
    }

    private static boolean isStoneblock(Level world) {
        return world.getBlockState(new BlockPos(0, 319, 0)).equals(Blocks.BEDROCK.defaultBlockState()) && world.getBlockState(new BlockPos(0, 318, 0)).equals(Blocks.STONE.defaultBlockState());
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        LevelAccessor world = event.getWorld();
        if (!world.isClientSide()) {
            ServerLevel serverLevel = world.getServer().overworld();
            if (isStoneblock(serverLevel)) {
                SpawnGeneratedSavedData saveData = SpawnGeneratedSavedData.getOrCreate(serverLevel.getDataStorage());
                if (SpawnGeneratedSavedData.generated.equals(":(")) {
                    LOGGER.info("Spawn not generated yet :(");
                    double radius = 4.5;
                    BlockPos block = new BlockPos(0, 128, 0);
                    for (int x = (int) -Math.ceil(radius); x < Math.ceil(radius); x++) {
                        for (int y = (int) -Math.ceil(radius) + 2; y < Math.ceil(radius); y++) {
                            for (int z = (int) -Math.ceil(radius); z < Math.ceil(radius); z++) {
                                if (x * x + y * y + z * z <= radius * radius) {
                                    world.removeBlock(block.offset(x, y, z), false);
                                }
                            }
                        }
                    }
                    world.setBlock(block.offset(Math.round(radius - 1), -2, 0), Blocks.TORCH.defaultBlockState(), 0);
                    world.setBlock(block.offset(-Math.round(radius - 1), -2, 0), Blocks.TORCH.defaultBlockState(), 0);
                    world.setBlock(block.offset(0, -2, -Math.round(radius - 1)), Blocks.TORCH.defaultBlockState(), 0);
                    world.setBlock(block.offset(0, -2, Math.round(radius - 1)), Blocks.TORCH.defaultBlockState(), 0);
                    world.getServer().getWorldData().overworldData().setSpawn(block.offset(0, -3, 0), 0);
                    SpawnGeneratedSavedData.generated = ":)";
                    saveData.setDirty();
                } else {
                    LOGGER.info("Spawn previously generated :)");
                }
            }
        }
    }

    @SubscribeEvent
    public void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.side == LogicalSide.SERVER) {
            if (isStoneblock(event.player.getLevel())) {
                Player p = event.player;
                if (p.getEyePosition().y > p.getLevel().getMaxBuildHeight() || p.position().y < p.getLevel().getMinBuildHeight()) {
                    p.teleportTo(0.5, 125, 0.5);
                }
            }
        }
    }

    private static class SpawnGeneratedSavedData extends SavedData {

        public void setGenerated(String generated) {
            SpawnGeneratedSavedData.generated = generated;
        }

        static String generated;

        protected SpawnGeneratedSavedData(String s) {
            generated = s;
        }

        public static SpawnGeneratedSavedData getOrCreate(DimensionDataStorage world) {
            return world.computeIfAbsent(SpawnGeneratedSavedData::load, SpawnGeneratedSavedData::create, StoneblockWorld.MODID + "generated");
        }

        public static SpawnGeneratedSavedData load(CompoundTag nbt) {
            return new SpawnGeneratedSavedData(nbt.getString("generated"));
        }

        public static SpawnGeneratedSavedData create() {
            return new SpawnGeneratedSavedData(":(");
        }

        @Override
        public CompoundTag save(CompoundTag compound) {
            compound.putString("generated", generated);
            return compound;
        }
    }
}

