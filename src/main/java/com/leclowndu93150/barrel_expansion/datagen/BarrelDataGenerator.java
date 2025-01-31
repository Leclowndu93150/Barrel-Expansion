package com.leclowndu93150.barrel_expansion.datagen;

import com.leclowndu93150.barrel_expansion.BarrelExpansion;
import com.leclowndu93150.barrel_expansion.events.BarrelEvents;
import com.leclowndu93150.barrel_expansion.events.VanillaBarrels;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
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
import net.neoforged.neoforge.common.conditions.ICondition;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class BarrelDataGenerator extends DynServerResourcesGenerator {

    public static final BarrelDataGenerator INSTANCE = new BarrelDataGenerator();

    public BarrelDataGenerator() {
        super(new net.mehvahdjukaar.moonlight.api.resources.pack.DynamicDataPack(
                ResourceLocation.fromNamespaceAndPath(BarrelExpansion.MODID, "generated_pack"),
                Pack.Position.TOP,
                false,
                false
        ));
        this.dynamicPack.setGenerateDebugResources(PlatHelper.isDev());
    }

    @Override
    public void regenerateDynamicAssets(ResourceManager manager) {
        Stream.concat(
                BarrelEvents.REGISTERED_BARRELS.values().stream(),
                VanillaBarrels.VANILLA_BARRELS.values().stream()
        ).forEach(barrel -> {
            String barrelId = barrel.woodType() + "_barrel";
            Item barrelItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(BarrelExpansion.MODID, barrelId));

            // Generate tags
            SimpleTagBuilder woodenBarrels = SimpleTagBuilder.of(ResourceLocation.fromNamespaceAndPath("c", "wooden_barrels"));
            woodenBarrels.addEntry(barrelItem);
            dynamicPack.addTag(woodenBarrels, Registries.ITEM);

            // Generate crafting recipe
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