package com.leclowndu93150.barrel_expansion.events;

import com.leclowndu93150.barrel_expansion.registry.BarrelRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.ModifyRegistriesEvent;
import net.neoforged.neoforge.registries.callback.AddCallback;

import java.util.HashMap;
import java.util.Map;

import static com.leclowndu93150.barrel_expansion.BarrelExpansion.MODID;

@EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD)
public class BarrelEvents {
    public static final Map<String, BarrelInfo> REGISTERED_BARRELS = new HashMap<>();

    public record BarrelInfo(
            String woodType,
            String namespace,
            MapColor color,
            ResourceLocation planksId
    ) {}

    private static class BlockRegistrationCallback implements AddCallback<Block> {
        @Override
        public void onAdd(Registry<Block> registry, int id, ResourceKey<Block> key, Block value) {
            ResourceLocation blockId = key.location();
            if (isWoodType(blockId)) {
                String woodType = extractWoodType(blockId);
                MapColor color = getColorForWood(woodType);
                String barrelId = woodType + "_barrel";

                BarrelRegistries.registerDirectBarrel(barrelId, color, registry);

                REGISTERED_BARRELS.put(woodType, new BarrelInfo(
                        woodType,
                        blockId.getNamespace(),
                        color,
                        blockId
                ));
            }
        }
    }

    @SubscribeEvent
    public static void modifyRegistriesEvent(ModifyRegistriesEvent event) {
        event.getRegistry(Registries.BLOCK).addCallback(new BlockRegistrationCallback());
    }

    private static boolean isWoodType(ResourceLocation id) {
        String path = id.getPath();
        return path.contains("planks") || path.endsWith("_planks");
    }

    private static String extractWoodType(ResourceLocation id) {
        String path = id.getPath();
        return path.replace("_planks", "").replace("planks_", "");
    }

    private static MapColor getColorForWood(String woodType) {
        return switch (woodType) {
            case "oak" -> MapColor.WOOD;
            case "spruce" -> MapColor.PODZOL;
            case "birch" -> MapColor.SAND;
            case "jungle" -> MapColor.DIRT;
            case "acacia" -> MapColor.COLOR_ORANGE;
            case "dark_oak" -> MapColor.COLOR_BROWN;
            case "cherry" -> MapColor.TERRACOTTA_RED;
            default -> MapColor.WOOD;
        };
    }
}