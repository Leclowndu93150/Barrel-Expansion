package com.leclowndu93150.barrel_expansion;

import com.leclowndu93150.barrel_expansion.datagen.BarrelDataGenerator;
import com.leclowndu93150.barrel_expansion.datagen.BarrelResourceGenerator;
import com.leclowndu93150.barrel_expansion.events.VanillaBarrels;
import com.leclowndu93150.barrel_expansion.registry.BarrelRegistries;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(BarrelExpansion.MODID)
public class BarrelExpansion {
    public static final String MODID = "barrel_expansion";
    public static final Logger LOGGER2 = LogManager.getLogger();

    public BarrelExpansion(IEventBus modEventBus, ModContainer modContainer) {
        RegHelper.startRegisteringFor(modEventBus);
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
