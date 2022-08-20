package com.undercover.stoneblock_gen.world;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraftforge.common.world.ForgeWorldPreset;

import java.util.List;
import java.util.Optional;

public class StoneblockChunkGen implements ForgeWorldPreset.IBasicChunkGeneratorFactory {
    @Override
    public ChunkGenerator createChunkGenerator(RegistryAccess registryAccess, long seed) {
        Registry<Biome> biomeRegistry = registryAccess.registryOrThrow(Registry.BIOME_REGISTRY);
        Registry<StructureSet> structureSetRegistry = registryAccess.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);

        return new FlatLevelSource(structureSetRegistry,
                new FlatLevelGeneratorSettings(Optional.empty(), biomeRegistry).withLayers(
                        List.of(new FlatLayerInfo(1, Blocks.BEDROCK),
                                new FlatLayerInfo(382, Blocks.STONE),
                                new FlatLayerInfo(1, Blocks.BEDROCK)
                        ),
                        Optional.empty()));
    }
}
