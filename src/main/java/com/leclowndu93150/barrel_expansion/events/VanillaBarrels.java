package com.leclowndu93150.barrel_expansion.events;

import com.leclowndu93150.barrel_expansion.registry.BarrelRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.MapColor;
import java.util.HashMap;
import java.util.Map;
import com.leclowndu93150.barrel_expansion.events.BarrelEvents.BarrelInfo;

public class VanillaBarrels {
    public static final Map<String, BarrelInfo> VANILLA_BARRELS = new HashMap<>();

    public static void init() {
        register("oak", MapColor.WOOD);
        register("birch", MapColor.SAND);
        register("jungle", MapColor.DIRT);
        register("acacia", MapColor.COLOR_ORANGE);
        register("dark_oak", MapColor.COLOR_BROWN);
        register("cherry", MapColor.TERRACOTTA_RED);
        register("bamboo", MapColor.COLOR_YELLOW);
        register("crimson", MapColor.CRIMSON_STEM);
        register("warped", MapColor.WARPED_STEM);
        register("mangrove", MapColor.COLOR_RED);
    }

    private static void register(String woodType, MapColor color) {
        ResourceLocation planksId = ResourceLocation.fromNamespaceAndPath("minecraft", woodType + "_planks");
        VANILLA_BARRELS.put(woodType, new BarrelInfo(woodType, "minecraft", color, planksId));
        BarrelRegistries.registerBarrel(woodType, planksId, color);
    }
}