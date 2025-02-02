package com.leclowndu93150.barrel_expansion.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.leclowndu93150.barrel_expansion.BarrelExpansion;
import com.leclowndu93150.barrel_expansion.events.BarrelEvents;
import com.leclowndu93150.barrel_expansion.events.VanillaBarrels;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.SimpleTagBuilder;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynServerResourcesGenerator;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class BarrelDataGenerator extends DynServerResourcesGenerator {
    public static final BarrelDataGenerator INSTANCE = new BarrelDataGenerator();
    private final List<String> blockValues = new ArrayList<>();
    private final List<String> itemValues = new ArrayList<>();

    public BarrelDataGenerator() {
        super(new net.mehvahdjukaar.moonlight.api.resources.pack.DynamicDataPack(
                ResourceLocation.fromNamespaceAndPath(BarrelExpansion.MODID, "generated_pack"),
                Pack.Position.TOP,
                false,
                false
        ));
        this.dynamicPack.setGenerateDebugResources(PlatHelper.isDev());
    }

    private JsonObject createLootTable(ResourceLocation itemId) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:block");

        JsonArray pools = new JsonArray();
        JsonObject pool = new JsonObject();
        pool.addProperty("rolls", 1);
        pool.addProperty("bonus_rolls", 0);

        JsonArray conditions = new JsonArray();
        JsonObject survives = new JsonObject();
        survives.addProperty("condition", "minecraft:survives_explosion");
        conditions.add(survives);
        pool.add("conditions", conditions);

        JsonArray entries = new JsonArray();
        JsonObject entry = new JsonObject();
        entry.addProperty("type", "minecraft:item");
        entry.addProperty("name", itemId.toString());

        JsonArray functions = new JsonArray();
        JsonObject copyName = new JsonObject();
        copyName.addProperty("function", "minecraft:copy_components");
        copyName.addProperty("source", "block_entity");
        JsonArray include = new JsonArray();
        include.add("minecraft:custom_name");
        copyName.add("include", include);
        functions.add(copyName);

        entry.add("functions", functions);
        entries.add(entry);
        pool.add("entries", entries);
        pools.add(pool);
        root.add("pools", pools);
        root.addProperty("random_sequence", "minecraft:blocks/" + itemId.getPath());

        return root;
    }

    @Override
    public void regenerateDynamicAssets(ResourceManager manager) {
        blockValues.clear();
        itemValues.clear();

        Stream.concat(
                BarrelEvents.REGISTERED_BARRELS.values().stream(),
                VanillaBarrels.VANILLA_BARRELS.values().stream()
        ).forEach(barrel -> {
            String barrelId = barrel.woodType() + "_barrel";
            Item barrelItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(BarrelExpansion.MODID, barrelId));
            Block barrelBlock = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(BarrelExpansion.MODID, barrelId));

            SimpleTagBuilder mineableAxe = SimpleTagBuilder.of(ResourceLocation.fromNamespaceAndPath("minecraft", "mineable/axe"));
            mineableAxe.addEntry(barrelBlock);
            dynamicPack.addTag(mineableAxe, Registries.BLOCK);

            SimpleTagBuilder woodenBarrels = SimpleTagBuilder.of(ResourceLocation.fromNamespaceAndPath("c", "barrels"));
            woodenBarrels.addEntry(barrelItem);
            dynamicPack.addTag(woodenBarrels, Registries.ITEM);

            blockValues.add(BuiltInRegistries.BLOCK.getKey(barrelBlock).toString());
            itemValues.add(BuiltInRegistries.ITEM.getKey(barrelItem).toString());

            JsonObject lootTable = createLootTable(BuiltInRegistries.ITEM.getKey(barrelItem));
            dynamicPack.addJson(ResourceLocation.fromNamespaceAndPath(BarrelExpansion.MODID, "blocks/" + barrelId), lootTable, ResType.LOOT_TABLES);

            Item planks = BuiltInRegistries.ITEM.get(barrel.planksId());
            Item slabs = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(barrel.planksId().getNamespace(),
                    barrel.woodType() + "_slab"));

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, barrelItem)
                    .pattern("PSP")
                    .pattern("P P")
                    .pattern("PSP")
                    .define('P', planks)
                    .define('S', slabs)
                    .unlockedBy("has_planks", InventoryChangeTrigger.TriggerInstance.hasItems(planks))
                    .save(new RecipeOutput() {
                        @Override
                        public Advancement.@NotNull Builder advancement() {
                            return Advancement.Builder.advancement();
                        }

                        @Override
                        public void accept(@NotNull ResourceLocation resourceLocation, @NotNull Recipe<?> recipe,
                                           @Nullable AdvancementHolder advancementHolder, ICondition... iConditions) {
                            dynamicPack.addRecipe(recipe, resourceLocation);
                        }
                    });
        });
    }

    @Override
    public Logger getLogger() {
        return BarrelExpansion.LOGGER2;
    }

    @Override
    public boolean dependsOnLoadedPacks() {
        return true;
    }

    public static void init() {
        INSTANCE.register();
    }
}