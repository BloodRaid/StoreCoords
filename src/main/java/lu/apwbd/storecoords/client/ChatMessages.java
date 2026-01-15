package lu.apwbd.storecoords.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

public final class ChatMessages {

    private ChatMessages() {}

    public static void stored(LocalPlayer p, BlockPos anchor, int resolvedCount) {
        String extra = linkedExtra(resolvedCount);

        MutableComponent msg = new TextComponent("Stored Block: ").withStyle(ChatFormatting.GRAY);
        msg.append(coords(anchor, ChatFormatting.GOLD, ChatFormatting.GRAY));
        msg.append(new TextComponent(extra).withStyle(ChatFormatting.DARK_GREEN));

        send(p, msg);
    }

    public static void removed(LocalPlayer p, BlockPos anchor, int resolvedCount) {
        String extra = linkedExtra(resolvedCount);

        MutableComponent msg = new TextComponent("Removed Block: ").withStyle(ChatFormatting.GRAY);
        msg.append(coords(anchor, ChatFormatting.GOLD, ChatFormatting.GRAY));
        msg.append(new TextComponent(extra).withStyle(ChatFormatting.DARK_GREEN));

        send(p, msg);
    }

    public static void alreadyStored(LocalPlayer p, BlockPos anchor, int resolvedCount) {
        String extra = linkedExtra(resolvedCount);

        MutableComponent msg = new TextComponent("Block already stored: ").withStyle(ChatFormatting.RED);
        msg.append(coords(anchor, ChatFormatting.GOLD, ChatFormatting.GRAY));
        msg.append(new TextComponent(extra).withStyle(ChatFormatting.DARK_RED));

        send(p, msg);
    }

    public static void notStored(LocalPlayer p, BlockPos anchor, int resolvedCount) {
        String extra = linkedExtra(resolvedCount);

        MutableComponent msg = new TextComponent("Block not stored: ").withStyle(ChatFormatting.RED);
        msg.append(coords(anchor, ChatFormatting.GOLD, ChatFormatting.RED));
        msg.append(new TextComponent(extra).withStyle(ChatFormatting.DARK_RED));

        send(p, msg);
    }

    public static void noTarget(LocalPlayer p) {
        send(p, new TextComponent("No valid block targeted.").withStyle(ChatFormatting.RED));
    }

    public static void fileError(LocalPlayer p, String action, String fileName) {
        MutableComponent msg = new TextComponent("Failed to " + action + " ").withStyle(ChatFormatting.RED);
        msg.append(new TextComponent(fileName).withStyle(ChatFormatting.DARK_RED));
        msg.append(new TextComponent(".").withStyle(ChatFormatting.RED));
        send(p, msg);
    }

    public static void highlightToggled(LocalPlayer p, boolean enabled) {
        MutableComponent msg = new TextComponent("Highlight ").withStyle(ChatFormatting.GRAY);
        msg.append(new TextComponent(enabled ? "ON" : "OFF")
                .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED));
        msg.append(new TextComponent(".").withStyle(ChatFormatting.GRAY));
        send(p, msg);
    }


    private static void send(LocalPlayer p, MutableComponent message) {
        p.sendMessage(prefix().append(message), p.getUUID());
    }

    private static MutableComponent prefix() {
        MutableComponent msg = new TextComponent("[").withStyle(ChatFormatting.DARK_AQUA);
        msg.append(new TextComponent("SC").withStyle(ChatFormatting.DARK_GREEN));
        msg.append(new TextComponent("] ").withStyle(ChatFormatting.DARK_AQUA));
        return msg;
    }

    private static String linkedExtra(int resolvedCount) {
        if (resolvedCount <= 1) return "";
        return " (+" + (resolvedCount - 1) + " linked)";
    }


    private static MutableComponent coords(BlockPos pos, ChatFormatting numberColor, ChatFormatting sepColor) {
        MutableComponent c = new TextComponent(String.valueOf(pos.getX())).withStyle(numberColor);
        c.append(new TextComponent(", ").withStyle(sepColor));
        c.append(new TextComponent(String.valueOf(pos.getY())).withStyle(numberColor));
        c.append(new TextComponent(", ").withStyle(sepColor));
        c.append(new TextComponent(String.valueOf(pos.getZ())).withStyle(numberColor));
        return c;
    }
}
