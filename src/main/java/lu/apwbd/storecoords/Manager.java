package lu.apwbd.storecoords;

import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

import net.minecraftforge.fml.loading.FMLPaths;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Manager {
    public static File FOLDER = new File(FMLPaths.GAMEDIR.get().toFile(), "moddata/" + StoreCoords.MOD_ID);
    private static final File FILE = new File(FOLDER, "storedCoords.yml");
    public static final Set<BlockPos> BLOCKS = new HashSet<>();

    public static void updateSet(LocalPlayer player) {
        if (failLoadingFile(player)) return;
        Map<String, Object> data;

        try (FileInputStream inputStream = new FileInputStream(FILE)) {
            data = new Yaml().load(inputStream);
        } catch (IOException e) {
            sendFileError(player, "load");
            return;
        }

        if (data == null) {
            sendFileError(player, "load");
            return;
        }

        BLOCKS.clear();
        for (Object object : data.values()) {
            if (!(object instanceof Map<?, ?> coords)) continue;
            Object xObj = coords.get("x");
            Object yObj = coords.get("y");
            Object zObj = coords.get("z");

            if (xObj == null || yObj == null || zObj == null) continue;
            if (xObj instanceof Number x && yObj instanceof Number y && zObj instanceof Number z) {
                BLOCKS.add(new BlockPos(x.intValue(), y.intValue(), z.intValue()));
            }
        }
    }

    public static void updateYML(LocalPlayer player) {
        if (failLoadingFile(player)) return;
        Map<String, Object> data = new HashMap<>();
        int index = 1;

        for (BlockPos block : BLOCKS) {
            Map<String, Object> coords = new HashMap<>(3);
            coords.put("x", block.getX());
            coords.put("y", block.getY());
            coords.put("z", block.getZ());

            data.put(String.valueOf(index), coords);
            index++;
        }

        try (FileWriter writer = new FileWriter(FILE)) {
            new Yaml().dump(data, writer);
        } catch (IOException e) {
            sendFileError(player, "edit");
        }
    }

    private static void sendFileError(LocalPlayer player, String action) {
        MutableComponent message = new TextComponent("Failed to %s '%s'.".formatted(action, FILE.getName())).withStyle(ChatFormatting.RED);
        player.sendMessage(StoreCoords.getPrefix().append(message), player.getUUID());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static boolean failLoadingFile(LocalPlayer player) {
        if (!FOLDER.exists()) FOLDER.mkdirs();

        try {
            if (!FILE.exists()) FILE.createNewFile();
            return false;
        } catch (IOException e) {
            sendFileError(player, "load");
            return true;
        }
    }
}