package com.leclowndu93150.barrel_expansion;

import com.leclowndu93150.barrel_expansion.datagen.BarrelDataGenerator;
import com.leclowndu93150.barrel_expansion.datagen.BarrelResourceGenerator;
import com.leclowndu93150.barrel_expansion.events.VanillaBarrels;
import com.leclowndu93150.barrel_expansion.registry.BarrelRegistries;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(BarrelExpansion.MODID)
public class BarrelExpansion {
    public static final String MODID = "barrel_expansion";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final org.apache.logging.log4j.Logger LOGGER2 = org.apache.logging.log4j.LogManager.getLogger();

    public BarrelExpansion(IEventBus modEventBus, ModContainer modContainer) {
        BarrelRegistries.BLOCKS.register(modEventBus);
        BarrelRegistries.ITEMS.register(modEventBus);
        BarrelRegistries.CREATIVE_MODE_TABS.register(modEventBus);
        BarrelRegistries.BLOCK_ENTITIES.register(modEventBus);
        VanillaBarrels.init();
        BarrelResourceGenerator.init();
        BarrelDataGenerator.init();
        //modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }


}
