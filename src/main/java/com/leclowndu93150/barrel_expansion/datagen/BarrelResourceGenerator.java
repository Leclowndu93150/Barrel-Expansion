package com.leclowndu93150.barrel_expansion.datagen;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.leclowndu93150.barrel_expansion.BarrelExpansion;
import com.leclowndu93150.barrel_expansion.events.BarrelEvents;
import com.leclowndu93150.barrel_expansion.events.VanillaBarrels;
import net.mehvahdjukaar.moonlight.api.events.AfterLanguageLoadEvent;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynClientResourcesGenerator;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicTexturePack;
import net.mehvahdjukaar.moonlight.api.resources.textures.Palette;
import net.mehvahdjukaar.moonlight.api.resources.textures.Respriter;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureImage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BarrelResourceGenerator extends DynClientResourcesGenerator {
    private static final Logger LOGGER = BarrelExpansion.LOGGER2;

    public static void init() {
        BarrelResourceGenerator generator = new BarrelResourceGenerator();
        generator.register();
    }

    protected BarrelResourceGenerator() {
        super(new DynamicTexturePack(ResourceLocation.fromNamespaceAndPath(BarrelExpansion.MODID, "generated_pack"), Pack.Position.TOP, true, false));
    }

    @Override
    public void regenerateDynamicAssets(ResourceManager manager) {
        Stream.concat(
                BarrelEvents.REGISTERED_BARRELS.values().stream(),
                VanillaBarrels.VANILLA_BARRELS.values().stream()
        ).forEach(barrelInfo -> {
            try {
                generateBarrelResources(manager, barrelInfo);
            } catch (Exception e) {
                LOGGER.error("Failed generating resources for " + barrelInfo.woodType(), e);
            }
        });
    }

    private void generateBarrelResources(ResourceManager manager, BarrelEvents.BarrelInfo barrelInfo) throws Exception {
        // Get plank texture
        TextureImage plankTexture = getPlankTexture(manager, barrelInfo.planksId());
        if (plankTexture == null) return;

        try {
            Palette plankPalette = Palette.fromImage(plankTexture);
            String barrelId = barrelInfo.woodType() + "_barrel";

            // Generate textures
            generateBarrelTexture(manager, barrelId, "side", plankPalette);
            generateBarrelTexture(manager, barrelId, "top", plankPalette);
            generateBarrelTexture(manager, barrelId, "top_open", plankPalette);
            generateBarrelTexture(manager, barrelId, "bottom", plankPalette);

            // Generate models
            generateBarrelModels(barrelId);

            // Generate blockstate
            generateBlockstate(barrelId);
        } finally {
            plankTexture.close();
        }
    }

    private TextureImage getPlankTexture(ResourceManager manager, ResourceLocation planksId) throws Exception {
        ResourceLocation modelLoc = ResourceLocation.fromNamespaceAndPath(planksId.getNamespace(), "models/block/" + planksId.getPath() + ".json");
        try (InputStream modelStream = manager.getResource(modelLoc).orElseThrow().open()) {
            JsonObject model = JsonParser.parseReader(new InputStreamReader(modelStream)).getAsJsonObject();
            String texturePath = model.getAsJsonObject("textures").get("all").getAsString();
            return TextureImage.open(manager, ResourceLocation.parse(texturePath));
        }
    }

    private void generateBarrelTexture(ResourceManager manager, String barrelId, String part, Palette plankPalette) throws Exception {
        ResourceLocation baseLoc = ResourceLocation.fromNamespaceAndPath(BarrelExpansion.MODID, "block/gray_barrel_" + part);
        try (TextureImage baseImage = TextureImage.open(manager, baseLoc)) {
            int[] savedPixels = null;
            if(part.contains("top")) {
                savedPixels = new int[4];
                int idx = 0;
                for(int x = 3; x <= 4; x++) {
                    for(int y = 8; y <= 9; y++) {
                        savedPixels[idx++] = baseImage.getFramePixel(0, x, y);
                    }
                }
            } else if(part.equals("side")) {
                savedPixels = new int[64]; // 16*2 + 16*2 pixels
                int idx = 0;
                // Save top strip
                for(int x = 0; x < 16; x++) {
                    for(int y = 3; y <= 4; y++) {
                        savedPixels[idx++] = baseImage.getFramePixel(0, x, y);
                    }
                }
                // Save bottom strip
                for(int x = 0; x < 16; x++) {
                    for(int y = 11; y <= 12; y++) {
                        savedPixels[idx++] = baseImage.getFramePixel(0, x, y);
                    }
                }
            }

            Respriter respriter = Respriter.of(baseImage);
            TextureImage recolored = respriter.recolor(plankPalette);

            if(savedPixels != null) {
                int idx = 0;
                if(part.contains("top")) {
                    for(int x = 3; x <= 4; x++) {
                        for(int y = 8; y <= 9; y++) {
                            recolored.setFramePixel(0, x, y, savedPixels[idx++]);
                        }
                    }
                } else if(part.equals("side")) {
                    // Restore top strip
                    for(int x = 0; x < 16; x++) {
                        for(int y = 3; y <= 4; y++) {
                            recolored.setFramePixel(0, x, y, savedPixels[idx++]);
                        }
                    }
                    // Restore bottom strip
                    for(int x = 0; x < 16; x++) {
                        for(int y = 11; y <= 12; y++) {
                            recolored.setFramePixel(0, x, y, savedPixels[idx++]);
                        }
                    }
                }
            }

            this.dynamicPack.addAndCloseTexture(
                    ResourceLocation.fromNamespaceAndPath(BarrelExpansion.MODID, "block/" + barrelId + "_" + part),
                    recolored
            );
        }
    }

    private void generateBarrelModels(String barrelId) {
        // Normal barrel model
        JsonObject model = new JsonObject();
        model.addProperty("parent", "minecraft:block/cube_bottom_top");
        JsonObject textures = new JsonObject();
        textures.addProperty("bottom", BarrelExpansion.MODID + ":block/" + barrelId + "_bottom");
        textures.addProperty("side", BarrelExpansion.MODID + ":block/" + barrelId + "_side");
        textures.addProperty("top", BarrelExpansion.MODID + ":block/" + barrelId + "_top");
        model.add("textures", textures);
        this.dynamicPack.addBlockModel(ResourceLocation.fromNamespaceAndPath(BarrelExpansion.MODID, barrelId), model);

        // Open barrel model
        JsonObject openModel = new JsonObject();
        openModel.addProperty("parent", "minecraft:block/cube_bottom_top");
        JsonObject openTextures = new JsonObject();
        openTextures.addProperty("bottom", BarrelExpansion.MODID + ":block/" + barrelId + "_bottom");
        openTextures.addProperty("side", BarrelExpansion.MODID + ":block/" + barrelId + "_side");
        openTextures.addProperty("top", BarrelExpansion.MODID + ":block/" + barrelId + "_top_open");
        openModel.add("textures", openTextures);
        this.dynamicPack.addBlockModel(ResourceLocation.fromNamespaceAndPath(BarrelExpansion.MODID, barrelId + "_open"), openModel);

        //Item model
        JsonObject itemModel = new JsonObject();
        itemModel.addProperty("parent", BarrelExpansion.MODID + ":block/" + barrelId);
        this.dynamicPack.addItemModel(ResourceLocation.fromNamespaceAndPath(BarrelExpansion.MODID, barrelId), itemModel);
    }

    private void generateBlockstate(String barrelId) {
        JsonObject blockstate = new JsonObject();
        JsonObject variants = new JsonObject();

        addBarrelVariant(variants, barrelId, "down", false, 180, 0);
        addBarrelVariant(variants, barrelId, "down", true, 180, 0);
        addBarrelVariant(variants, barrelId, "east", false, 90, 90);
        addBarrelVariant(variants, barrelId, "east", true, 90, 90);
        addBarrelVariant(variants, barrelId, "north", false, 90, 0);
        addBarrelVariant(variants, barrelId, "north", true, 90, 0);
        addBarrelVariant(variants, barrelId, "south", false, 90, 180);
        addBarrelVariant(variants, barrelId, "south", true, 90, 180);
        addBarrelVariant(variants, barrelId, "up", false, 0, 0);
        addBarrelVariant(variants, barrelId, "up", true, 0, 0);
        addBarrelVariant(variants, barrelId, "west", false, 90, 270);
        addBarrelVariant(variants, barrelId, "west", true, 90, 270);

        blockstate.add("variants", variants);
        this.dynamicPack.addBlockState(ResourceLocation.fromNamespaceAndPath(BarrelExpansion.MODID, barrelId), blockstate);
    }

    private void addBarrelVariant(JsonObject variants, String barrelId, String facing, boolean open, int x, int y) {
        JsonObject variant = new JsonObject();
        variant.addProperty("model", BarrelExpansion.MODID + ":block/" + barrelId + (open ? "_open" : ""));
        if (x != 0) variant.addProperty("x", x);
        if (y != 0) variant.addProperty("y", y);
        variants.add("facing=" + facing + ",open=" + open, variant);
    }

    @Override
    public void addDynamicTranslations(AfterLanguageLoadEvent event) {
        Stream.concat(
                BarrelEvents.REGISTERED_BARRELS.values().stream(),
                VanillaBarrels.VANILLA_BARRELS.values().stream()
        ).forEach(barrel -> {
            String barrelId = barrel.woodType() + "_barrel";
            String name = Stream.of(barrel.woodType().split("_"))
                    .map(StringUtils::capitalize)
                    .collect(Collectors.joining(" ")) + " Barrel";
            event.addEntry("block." + BarrelExpansion.MODID + "." + barrelId, name);
        });
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @Override
    public boolean dependsOnLoadedPacks() {
        return true;
    }
}