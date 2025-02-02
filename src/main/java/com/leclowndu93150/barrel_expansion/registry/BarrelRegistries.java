package com.leclowndu93150.barrel_expansion.registry;

import com.leclowndu93150.barrel_expansion.custom.CustomBarrelBlock;
import com.leclowndu93150.barrel_expansion.custom.CustomBarrelBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.*;

import java.util.HashMap;
import java.util.Map;

import static com.leclowndu93150.barrel_expansion.BarrelExpansion.MODID;

public class BarrelRegistries {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);

    public static final Map<String, BarrelRegistryGroup> BARRELS = new HashMap<>();
    public static final Map<String, DirectBarrelRegistryGroup> DIRECT_BARRELS = new HashMap<>();

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BARREL_TAB = CREATIVE_MODE_TABS.register(
            "barrels",
            () -> CreativeModeTab.builder()
                    .title(Component.literal("Barrel Expansion"))
                    .icon(() -> BARRELS.values().iterator().next().blockItem().get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        BARRELS.values().forEach(barrel -> output.accept(barrel.blockItem().get()));
                        DIRECT_BARRELS.values().forEach(barrel -> output.accept(barrel.block().asItem()));
                    }).build()
    );

    public static BarrelRegistryGroup registerBarrel(String name, MapColor color) {
        if (BARRELS.containsKey(name)) {
            return BARRELS.get(name);
        }

        DeferredBlock<CustomBarrelBlock> block = BLOCKS.register(name,
                () -> new CustomBarrelBlock(BlockBehaviour.Properties.of().mapColor(color).strength(2.5F), name));

        DeferredItem<BlockItem> blockItem = ITEMS.register(name,
                () -> new BlockItem(block.get(), new Item.Properties()));

        DeferredHolder<BlockEntityType<?>, BlockEntityType<CustomBarrelBlockEntity>> blockEntity =
                BLOCK_ENTITIES.register(name,
                        () -> BlockEntityType.Builder.of(
                                CustomBarrelBlockEntity::new,
                                block.get()
                        ).build(null));

        BarrelRegistryGroup group = new BarrelRegistryGroup(block, blockItem, blockEntity);
        BARRELS.put(name, group);
        return group;
    }

    public static DirectBarrelRegistryGroup registerDirectBarrel(String name, MapColor color, Registry<Block> blockRegistry) {
        if (DIRECT_BARRELS.containsKey(name)) {
            return DIRECT_BARRELS.get(name);
        }

        CustomBarrelBlock block = new CustomBarrelBlock(BlockBehaviour.Properties.of().mapColor(color).strength(2.5F), name);
        Registry.register(blockRegistry, ResourceLocation.fromNamespaceAndPath(MODID, name), block);

        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, name),
                new BlockItem(block, new Item.Properties()));

        BlockEntityType<CustomBarrelBlockEntity> blockEntity = BlockEntityType.Builder.of(
                CustomBarrelBlockEntity::new, block).build(null);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(MODID, name), blockEntity);

        DirectBarrelRegistryGroup group = new DirectBarrelRegistryGroup(block, blockEntity);
        DIRECT_BARRELS.put(name, group);
        return group;
    }

    public record BarrelRegistryGroup(
            DeferredBlock<CustomBarrelBlock> block,
            DeferredItem<BlockItem> blockItem,
            DeferredHolder<BlockEntityType<?>, BlockEntityType<CustomBarrelBlockEntity>> blockEntity
    ) {}

    public record DirectBarrelRegistryGroup(
            CustomBarrelBlock block,
            BlockEntityType<CustomBarrelBlockEntity> blockEntity
    ) {}
}