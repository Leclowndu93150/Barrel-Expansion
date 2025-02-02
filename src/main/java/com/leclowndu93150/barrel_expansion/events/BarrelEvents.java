package com.leclowndu93150.barrel_expansion.events;

import com.leclowndu93150.barrel_expansion.custom.CustomBarrelBlock;
import com.leclowndu93150.barrel_expansion.custom.CustomBarrelBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

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

    @SubscribeEvent
    public static void onRegister(RegisterEvent event) {
        event.register(Registries.BLOCK, helper -> {
            for (var block : BuiltInRegistries.BLOCK.holders().toList()) {
                ResourceLocation blockId = block.key().location();
                if (isWoodType(blockId)) {
                    String woodType = extractWoodType(blockId);
                    MapColor color = getColorForWood(woodType);
                    String barrelName = woodType + "_barrel";

                    CustomBarrelBlock barrelBlock = new CustomBarrelBlock(
                            BlockBehaviour.Properties.of().mapColor(color).strength(2.5F),
                            barrelName
                    );
                    helper.register(ResourceLocation.fromNamespaceAndPath(MODID, barrelName), barrelBlock);

                    REGISTERED_BARRELS.put(woodType, new BarrelInfo(
                            woodType,
                            blockId.getNamespace(),
                            color,
                            blockId
                    ));
                }
            }
        });

        event.register(Registries.ITEM, helper -> {
            for (var entry : REGISTERED_BARRELS.entrySet()) {
                String barrelName = entry.getKey() + "_barrel";
                Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(MODID, barrelName));
                helper.register(ResourceLocation.fromNamespaceAndPath(MODID, barrelName),
                        new BlockItem(block, new Item.Properties()));
            }
        });

        event.register(Registries.BLOCK_ENTITY_TYPE, helper -> {
            for (var entry : REGISTERED_BARRELS.entrySet()) {
                String barrelName = entry.getKey() + "_barrel";
                Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(MODID, barrelName));
                helper.register(ResourceLocation.fromNamespaceAndPath(MODID, barrelName),
                        BlockEntityType.Builder.of(CustomBarrelBlockEntity::new, block).build(null));
            }
        });
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