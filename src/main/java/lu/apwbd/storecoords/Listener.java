package lu.apwbd.storecoords;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = StoreCoords.MOD_ID, value = Dist.CLIENT)
public class Listener {
    public static final KeyMapping STORE_KEY = new KeyMapping("key.storecoords.store_block", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UP, "key.categories.storecoords");
    public static final KeyMapping REMOVE_STORED_KEY = new KeyMapping("key.storecoords.remove_stored_block", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_DOWN, "key.categories.storecoords");

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        boolean storePressed = STORE_KEY.consumeClick();
        boolean removeStoredPressed = REMOVE_STORED_KEY.consumeClick();
        if (!storePressed && !removeStoredPressed) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        LocalPlayer player = mc.player;

        Manager.updateSet(player);
        BlockHitResult result = (BlockHitResult) player.pick(5, 0.0F, false);
        BlockPos blockPos = result.getBlockPos();
        if (player.level.getBlockState(blockPos).isAir()) return;

        if (storePressed) {
            if (Manager.BLOCKS.contains(blockPos)) {
                sendErrorMessage(player, blockPos, "is already stored");
                return;
            }

            Manager.BLOCKS.add(result.getBlockPos());
            Manager.updateYML(player);
            sendSuccessMessage(player, blockPos, new TextComponent("stored").withStyle(ChatFormatting.GREEN));
        }

        if (removeStoredPressed) {
            if (!Manager.BLOCKS.contains(blockPos)) {
                sendErrorMessage(player, blockPos, "is not stored");
                return;
            }

            Manager.BLOCKS.remove(result.getBlockPos());
            Manager.updateYML(player);
            sendSuccessMessage(player, blockPos, new TextComponent("removed").withStyle(ChatFormatting.RED));
        }
    }

    private static void sendErrorMessage(LocalPlayer player, BlockPos blockPos, String action) {
        MutableComponent message = new TextComponent("Block ").withStyle(ChatFormatting.RED);
        message.append(getFormattedBlockPos(blockPos, ChatFormatting.RED, ChatFormatting.DARK_RED));
        message.append(new TextComponent(" " + action +".").withStyle(ChatFormatting.RED));

        player.sendMessage(StoreCoords.getPrefix().append(message), player.getUUID());
    }

    private static void sendSuccessMessage(LocalPlayer player, BlockPos blockPos, MutableComponent action) {
        MutableComponent message = new TextComponent("Block ").withStyle(ChatFormatting.GRAY);
        message.append(getFormattedBlockPos(blockPos, ChatFormatting.GRAY, ChatFormatting.GOLD));
        message.append(new TextComponent(" was ").withStyle(ChatFormatting.GRAY));
        message.append(action);
        message.append(new TextComponent(".").withStyle(ChatFormatting.GRAY));

        player.sendMessage(StoreCoords.getPrefix().append(message), player.getUUID());
    }

    private static MutableComponent getFormattedBlockPos(BlockPos blockPos, ChatFormatting primaryColor, ChatFormatting secondaryColor) {
        MutableComponent message = new TextComponent(String.valueOf(blockPos.getX())).withStyle(secondaryColor);
        message.append(new TextComponent(", ").withStyle(primaryColor));
        message.append(new TextComponent(String.valueOf(blockPos.getY())).withStyle(secondaryColor));
        message.append(new TextComponent(", ").withStyle(primaryColor));
        message.append(new TextComponent(String.valueOf(blockPos.getZ())).withStyle(secondaryColor));
        return message;
    }
}