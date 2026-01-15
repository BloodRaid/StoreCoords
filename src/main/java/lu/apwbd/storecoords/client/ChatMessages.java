package lu.apwbd.storecoords.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

public final class ChatMessages {

    private ChatMessages() {}

    public static void stored(LocalPlayer p, BlockPos anchor, int changedCount, int resolvedCount) {
        String extra = resolvedCount > 1 ? " (+" + (resolvedCount - 1) + " linked)" : "";
        p.sendMessage(prefix().append(lit("Stored: " + anchor + extra).withStyle(ChatFormatting.GREEN)), p.getUUID());
    }

    public static void removed(LocalPlayer p, BlockPos anchor, int changedCount, int resolvedCount) {
        String extra = resolvedCount > 1 ? " (+" + (resolvedCount - 1) + " linked)" : "";
        p.sendMessage(prefix().append(lit("Removed: " + anchor + extra).withStyle(ChatFormatting.GOLD)), p.getUUID());
    }

    public static void alreadyStored(LocalPlayer p, BlockPos anchor, int resolvedCount) {
        String extra = resolvedCount > 1 ? " (multi-block)" : "";
        p.sendMessage(prefix().append(lit("Already stored: " + anchor + extra).withStyle(ChatFormatting.RED)), p.getUUID());
    }

    public static void notStored(LocalPlayer p, BlockPos anchor, int resolvedCount) {
        String extra = resolvedCount > 1 ? " (multi-block)" : "";
        p.sendMessage(prefix().append(lit("Not stored: " + anchor + extra).withStyle(ChatFormatting.RED)), p.getUUID());
    }

    public static void noTarget(LocalPlayer p) {
        p.sendMessage(prefix().append(lit("No valid block targeted.").withStyle(ChatFormatting.RED)), p.getUUID());
    }

    public static void fileError(LocalPlayer p, String action, String fileName) {
        p.sendMessage(prefix().append(lit("Failed to " + action + " " + fileName + ".").withStyle(ChatFormatting.RED)), p.getUUID());
    }

    public static void highlightToggled(LocalPlayer p, boolean enabled) {
        p.sendMessage(prefix().append(lit("Highlight: " + (enabled ? "ON" : "OFF"))
                .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED)), p.getUUID());
    }

    private static MutableComponent prefix() {
        return lit("[SC] ").withStyle(ChatFormatting.DARK_AQUA);
    }

    private static MutableComponent lit(String s) {
        return new TextComponent(s);
    }
}
