package lu.apwbd.storecoords.io;

import lu.apwbd.storecoords.StoreCoords;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraftforge.fml.loading.FMLPaths;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * CoordsManager manages a collection of BlockPos coordinates and provides
 * functionality for storing, removing, and retrieving these coordinates persistently
 * using a YAML configuration file. This class ensures thread-safe operations for
 * accessing and modifying the coordinate set.
 *
 * This class utilizes the following:
 * - A backing file (`coords.yml`) located in the mod's configuration directory for
 *   persistent storage.
 * - Block positions defined as immutable instances of `BlockPos`.
 * - YAML parsing and serialization for loading and saving data from/to the file.
 *
 * It provides methods for handling batch operations, ensuring file persistence, and
 * offering a snapshot of the current coordinate set.
 *
 * This class is immutable and not extendable, marked as `final`, and ensures internal
 * encapsulation of its state.
 */
public final class CoordsManager {

    public enum ActionResult {
        OK,
        ALREADY_EXISTS,
        NOT_FOUND,
        IO_ERROR
    }

    public static final class BatchResult {
        public final ActionResult status;
        public final int changedCount;

        public BatchResult(ActionResult status, int changedCount) {
            this.status = status;
            this.changedCount = changedCount;
        }
    }

    private final File file;
    private final Set<BlockPos> blocks = new HashSet<>();
    /**
     * Indicates whether the coordinate data has been successfully loaded from the file system.
     *
     * This flag is used internally*/
    private boolean loaded = false;

    public CoordsManager() {
        this.file = new File(
                new File(FMLPaths.CONFIGDIR.get().toFile(), StoreCoords.MOD_ID),
                "coords.yml"
        );
    }

    public String getFileName() {
        return file.getName();
    }

    /**
     * Ensures that the configuration file is loaded into memory. If the file is not already
     * loaded, this method attempts to load it. The method returns the current status of
     * whether the data is successfully loaded.
     *
     * @param player The local player instance, which can be used to provide player-specific
     *               context if required during the loading process.
     * @return {@code true} if the configuration file is successfully loaded into memory,
     *         {@code false} otherwise.
     */
    public boolean ensureLoaded(LocalPlayer player) {
        if (loaded) return true;
        loaded = load();
        return loaded;
    }

    /**
     * Provides a snapshot of the current set of block positions.
     * The snapshot represents a copy of the internally stored blocks at the time
     * this method is called, ensuring the original set remains unaffected by external modifications.
     *
     * @return a set containing the block positions currently stored in the manager
     */
    public Set<BlockPos> getBlocksSnapshot() {
        return new HashSet<>(blocks);
    }


    /**
     * Stores all given block positions into the internal set of tracked coordinates associated with a player.
     * Ensures that only positions not already present are added to the set. If the positions are successfully
     * saved to persistent storage, they are retained; otherwise, the operation is rolled back.
     *
     * @param player    The local player instance initiating the store operation.
     * @param positions A set of {@code BlockPos} instances representing the block positions to store.
     * @return A {@code BatchResult} containing the status of the store operation and the number of positions added:
     *         - {@code ActionResult.OK} if the positions were successfully added and saved.
     *         - {@code ActionResult.ALREADY_EXISTS} if all the given positions already exist.
     *         - {@code ActionResult.IO_ERROR} if the save operation failed.
     */
    public BatchResult storeAll(LocalPlayer player, Set<BlockPos> positions) {
        List<BlockPos> toAdd = new ArrayList<>();
        for (BlockPos p : positions) {
            BlockPos im = p.immutable();
            if (!blocks.contains(im)) toAdd.add(im);
        }

        if (toAdd.isEmpty()) return new BatchResult(ActionResult.ALREADY_EXISTS, 0);

        for (BlockPos p : toAdd) blocks.add(p);

        if (!save()) {
            for (BlockPos p : toAdd) blocks.remove(p);
            return new BatchResult(ActionResult.IO_ERROR, 0);
        }

        return new BatchResult(ActionResult.OK, toAdd.size());
    }

    /**
     * Removes all specified block positions from the manager's tracking storage.
     * If any positions in the provided set match the currently tracked blocks, they are removed.
     * The operation attempts to persist changes to storage and will revert on failure to save.
     *
     * @param player    The local player initiating the removal operation.
     * @param positions A set of {@code BlockPos} objects representing the block positions to be removed.
     * @return A {@code BatchResult} object containing the result status of the operation
     *         and the count of successfully removed positions. Returns
     *         {@code ActionResult.NOT_FOUND} if no matching positions were found,
     *         {@code ActionResult.IO_ERROR} if a failure occurred during saving,
     *         or {@code ActionResult.OK} on successful removal.
     */
    public BatchResult removeAll(LocalPlayer player, Set<BlockPos> positions) {
        List<BlockPos> toRemove = new ArrayList<>();
        for (BlockPos p : positions) {
            if (blocks.contains(p)) toRemove.add(p.immutable());
        }

        if (toRemove.isEmpty()) return new BatchResult(ActionResult.NOT_FOUND, 0);

        for (BlockPos p : toRemove) blocks.remove(p);

        if (!save()) {
            blocks.addAll(toRemove);
            return new BatchResult(ActionResult.IO_ERROR, 0);
        }

        return new BatchResult(ActionResult.OK, toRemove.size());
    }


    /**
     * Loads data from a YAML file and populates the internal blocks set with coordinates
     * if the file exists and is successfully parsed. Ensures the file exists before attempting
     * to load the data. Clears existing blocks before loading new data. If the file contains
     * invalid or unexpected data, clears the blocks and considers the operation successful.
     *
     * @return {@code true} if the data was successfully loaded and parsed, or if the file
     *         contains no relevant data (such as being empty); {@code false} if an error
     *         occurs during the loading process.
     */
    private boolean load() {
        if (!ensureFileExists()) return false;

        Map<Object, Object> data;
        try (FileInputStream inputStream = new FileInputStream(file)) {
            Object root = new Yaml().load(inputStream);

            if (root == null) {
                blocks.clear();
                return true;
            }

            if (!(root instanceof Map<?, ?> mapRoot)) {
                blocks.clear();
                return true;
            }

            data = (Map<Object, Object>) mapRoot;

        } catch (IOException e) {
            StoreCoords.LOGGER.error("Failed to load coords.yml", e);
            return false;
        }

        blocks.clear();

        for (Object value : data.values()) {
            if (!(value instanceof Map<?, ?> coords)) continue;

            Object xObj = coords.get("x");
            Object yObj = coords.get("y");
            Object zObj = coords.get("z");

            if (!(xObj instanceof Number x && yObj instanceof Number y && zObj instanceof Number z)) continue;

            blocks.add(new BlockPos(x.intValue(), y.intValue(), z.intValue()));
        }

        return true;
    }

    /**
     * Saves the current state of block positions to the associated YAML file.
     * The method ensures that the file exists and writes the block data in
     * YAML format, with each block position represented as a map containing
     * its x, y, and z coordinates.
     *
     * @return true if the save operation was successful, false otherwise
     */
    private boolean save() {
        if (!ensureFileExists()) return false;

        Map<Integer, Object> out = new LinkedHashMap<>();
        int index = 1;

        for (BlockPos b : blocks) {
            Map<String, Object> coords = new LinkedHashMap<>(3);
            coords.put("x", b.getX());
            coords.put("y", b.getY());
            coords.put("z", b.getZ());
            out.put(index++, coords);
        }

        try (FileWriter writer = new FileWriter(file)) {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            new Yaml(options).dump(out, writer);
            return true;
        } catch (IOException e) {
            StoreCoords.LOGGER.error("Failed to save coords.yml", e);
            return false;
        }
    }

    /**
     * Ensures that the specified file exists. If the file does not exist, it attempts to create the
     * parent directories (if necessary) and the file.
     *
     * @return true if the file exists or was successfully created; false if an IOException occurs
     *         during the process.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private boolean ensureFileExists() {
        if (file.exists()) return true;

        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            return file.createNewFile();
        } catch (IOException e) {
            StoreCoords.LOGGER.error("Failed to create coords.yml", e);
            return false;
        }
    }
}
