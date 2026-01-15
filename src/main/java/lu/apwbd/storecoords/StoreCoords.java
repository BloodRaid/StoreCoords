package lu.apwbd.storecoords;

import com.mojang.logging.LogUtils;

import lu.apwbd.storecoords.client.KeyInputHandler;
import lu.apwbd.storecoords.client.config.ClientConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.slf4j.Logger;

@Mod("storecoords")
public class StoreCoords {
    public static final String MOD_ID = "storecoords";
    public static final Logger LOGGER = LogUtils.getLogger();

    public StoreCoords() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(StoreCoords::clientSetup);
        LOGGER.info("Mod started successfully");
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ClientRegistry.registerKeyBinding(KeyInputHandler.STORE_KEY);
            ClientRegistry.registerKeyBinding(KeyInputHandler.REMOVE_KEY);
            ClientRegistry.registerKeyBinding(KeyInputHandler.HIGHLIGHT_TOGGLE_KEY);
        });
    }

}