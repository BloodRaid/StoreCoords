package lu.apwbd.storecoords.client;

import com.mojang.blaze3d.platform.InputConstants;
import lu.apwbd.storecoords.StoreCoords;
import lu.apwbd.storecoords.client.render.BlockHighlighter;
import lu.apwbd.storecoords.io.CoordsManager;
import lu.apwbd.storecoords.world.MultiBlockResolver;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.Set;

@Mod.EventBusSubscriber(modid = StoreCoords.MOD_ID, value = Dist.CLIENT)
public final class KeyInputHandler {

    public static final KeyMapping STORE_KEY = new KeyMapping(
            "key.storecoords.store_block",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UP,
            "key.categories.storecoords"
    );

    public static final KeyMapping REMOVE_KEY = new KeyMapping(
            "key.storecoords.remove_stored_block",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_DOWN,
            "key.categories.storecoords"
    );

    public static final KeyMapping HIGHLIGHT_TOGGLE_KEY = new KeyMapping(
            "key.storecoords.toggle_highlight",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "key.categories.storecoords"
    );

    private static final CoordsManager MANAGER = new CoordsManager();

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) return;

        LocalPlayer player = mc.player;
        if (player == null) return;

        InputConstants.Key pressed = InputConstants.getKey(event.getKey(), event.getScanCode());

        if (HIGHLIGHT_TOGGLE_KEY.isActiveAndMatches(pressed)) {
            BlockHighlighter.toggle(player, MANAGER);
            return;
        }

        boolean storePressed = STORE_KEY.isActiveAndMatches(pressed);
        boolean removePressed = REMOVE_KEY.isActiveAndMatches(pressed);
        if (!storePressed && !removePressed) return;

        if (!MANAGER.ensureLoaded()) {
            ChatMessages.fileError(player, "load", MANAGER.getFileName());
            return;
        }

        BlockPos target = getTargetBlockPos(player);
        if (target == null) {
            ChatMessages.noTarget(player);
            return;
        }

        Set<BlockPos> positions = MultiBlockResolver.resolve(player.level, target);

        if (storePressed) {
            CoordsManager.BatchResult r = MANAGER.storeAll(positions);
            handleStore(player, target, positions.size(), r);
        } else {
            CoordsManager.BatchResult r = MANAGER.removeAll(positions);
            handleRemove(player, target, positions.size(), r);
        }
    }

    /**
     * Determines the position of the block currently targeted by the player.
     * The target must not be an air block and must result from a block hit.
     *
     * @param player The local player instance used to determine the targeted block.
     * @return The {@code BlockPos} of the targeted block, or {@code null} if no valid block is targeted.
     */
    private static BlockPos getTargetBlockPos(LocalPlayer player) {
        HitResult hit = player.pick(5.0D, 0.0F, false);
        if (!(hit instanceof BlockHitResult bhr)) return null;

        BlockPos pos = bhr.getBlockPos();
        if (player.level.getBlockState(pos).isAir()) return null;

        return pos;
    }


    private static void handleStore(LocalPlayer player, BlockPos anchor, int resolvedCount, CoordsManager.BatchResult r) {
        if (r.status == CoordsManager.ActionResult.IO_ERROR) {
            ChatMessages.fileError(player, "edit", MANAGER.getFileName());
            return;
        }
        if (r.status == CoordsManager.ActionResult.ALREADY_EXISTS) {
            ChatMessages.alreadyStored(player, anchor, resolvedCount);
            return;
        }

        ChatMessages.stored(player, anchor, resolvedCount);

        if (!r.changedPositions.isEmpty()) {
            BlockHighlighter.addToCache(r.changedPositions);
        }
    }

    private static void handleRemove(LocalPlayer player, BlockPos anchor, int resolvedCount, CoordsManager.BatchResult r) {
        if (r.status == CoordsManager.ActionResult.IO_ERROR) {
            ChatMessages.fileError(player, "edit", MANAGER.getFileName());
            return;
        }
        if (r.status == CoordsManager.ActionResult.NOT_FOUND) {
            ChatMessages.notStored(player, anchor, resolvedCount);
            return;
        }

        ChatMessages.removed(player, anchor, resolvedCount);

        if (!r.changedPositions.isEmpty()) {
            BlockHighlighter.removeFromCache(r.changedPositions);
        }
    }

}
