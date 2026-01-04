package lu.apwbd.storecoords;

import com.mojang.logging.LogUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.slf4j.Logger;

@Mod("storecoords")
public class StoreCoords {
    public static final String MOD_ID = "storecoords";
    private static final Logger LOGGER = LogUtils.getLogger();

    public StoreCoords() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(StoreCoords::clientSetup);
        LOGGER.info("Mod started successfully");
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        ClientRegistry.registerKeyBinding(Listener.STORE_KEY);
        ClientRegistry.registerKeyBinding(Listener.REMOVE_STORED_KEY);
    }

    public static MutableComponent getPrefix() {
        MutableComponent message = new TextComponent("[").withStyle(ChatFormatting.DARK_AQUA);
        message.append(new TextComponent("SC").withStyle(ChatFormatting.DARK_GREEN));
        message.append(new TextComponent("] ").withStyle(ChatFormatting.DARK_AQUA));
        return message;
    }
}