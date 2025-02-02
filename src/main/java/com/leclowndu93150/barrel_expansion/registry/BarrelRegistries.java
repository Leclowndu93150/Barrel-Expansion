package com.leclowndu93150.barrel_expansion.registry;

import com.leclowndu93150.barrel_expansion.custom.CustomBarrelBlock;
import com.leclowndu93150.barrel_expansion.custom.CustomBarrelBlockEntity;
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
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;

import static com.leclowndu93150.barrel_expansion.BarrelExpansion.MODID;

public class BarrelRegistries {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);

    public static final Map<String, DeferredBlockEntity> BARRELS = new HashMap<>();

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BARREL_TAB = CREATIVE_MODE_TABS.register(
            "barrels",
            () -> CreativeModeTab.builder()
                    .title(Component.literal("Barrel Expansion"))
                    .icon(() -> ITEMS.getEntries().stream().findFirst()
                            .map(holder -> holder.get().getDefaultInstance())
                            .orElse(net.minecraft.world.item.Items.BARREL.getDefaultInstance()))
                    .displayItems((params, output) -> {
                        ITEMS.getEntries().forEach(reg -> output.accept(reg.get()));
                    }).build()
    );

    public static void registerBarrel(String woodType, ResourceLocation planksId, MapColor color) {
        if (BARRELS.containsKey(woodType + "_barrel")) {
            return;
        }

        String barrelName = woodType + "_barrel";

        DeferredHolder<Block, CustomBarrelBlock> block = BLOCKS.register(barrelName,
                () -> new CustomBarrelBlock(BlockBehaviour.Properties.of().mapColor(color).strength(2.5F), barrelName));

        DeferredHolder<Item, BlockItem> item = ITEMS.register(barrelName,
                () -> new BlockItem(block.get(), new Item.Properties()));

        DeferredHolder<BlockEntityType<?>, BlockEntityType<CustomBarrelBlockEntity>> blockEntity =
                BLOCK_ENTITIES.register(barrelName,
                        () -> BlockEntityType.Builder.of(CustomBarrelBlockEntity::new, block.get()).build(null));

        BARRELS.put(barrelName, new DeferredBlockEntity(block, item, blockEntity));
    }

    public record DeferredBlockEntity(
            DeferredHolder<Block, CustomBarrelBlock> block,
            DeferredHolder<Item, BlockItem> item,
            DeferredHolder<BlockEntityType<?>, BlockEntityType<CustomBarrelBlockEntity>> blockEntity
    ) {}
}