package com.leclowndu93150.barrel_expansion.util;

import com.leclowndu93150.barrel_expansion.custom.CustomBarrelBlock;
import com.leclowndu93150.barrel_expansion.custom.CustomBarrelBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

import static com.leclowndu93150.barrel_expansion.BarrelExpansion.MODID;

public class BarrelUtil {
    private static final Set<String> KNOWN_BARRELS = new HashSet<>();

    public static void registerBarrelIfNotYetDone(String woodType, ResourceLocation planksId, MapColor color,
                                                  BiConsumer<ResourceLocation, Block> blockReg,
                                                  BiConsumer<ResourceLocation, Item> itemReg,
                                                  BiConsumer<ResourceLocation, BlockEntityType<?>> blockEntityReg) {

        if (!KNOWN_BARRELS.add(woodType)) {
            return;
        }

        ResourceLocation barrelId = ResourceLocation.fromNamespaceAndPath(MODID, woodType + "_barrel");

        CustomBarrelBlock block = new CustomBarrelBlock(BlockBehaviour.Properties.of().mapColor(color).strength(2.5F), woodType);
        blockReg.accept(barrelId, block);

        BlockItem blockItem = new BlockItem(block, new Item.Properties());
        itemReg.accept(barrelId, blockItem);

        BlockEntityType<CustomBarrelBlockEntity> blockEntity = BlockEntityType.Builder.of(
                (pos, state) -> new CustomBarrelBlockEntity(pos, state),
                block
        ).build(null);
        blockEntityReg.accept(barrelId, blockEntity);
    }
}