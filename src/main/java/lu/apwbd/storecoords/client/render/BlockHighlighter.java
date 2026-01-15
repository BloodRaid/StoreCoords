package lu.apwbd.storecoords.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lu.apwbd.storecoords.StoreCoords;
import lu.apwbd.storecoords.client.ChatMessages;
import lu.apwbd.storecoords.client.config.ClientConfig;
import lu.apwbd.storecoords.io.CoordsManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = StoreCoords.MOD_ID, value = Dist.CLIENT)
public final class BlockHighlighter {

    private static boolean enabled = false;

    private static float r,g,b;
    private static ClientConfig.ColorMode lastMode;

    private static final Set<BlockPos> cachedSet = new HashSet<>();
    private static final List<BlockPos> cachedList = new ArrayList<>();

    private BlockHighlighter() {}

    /**
     * Toggles the highlighting state between enabled and disabled. When toggled,
     * it sends a chat message to the specified player indicating whether the
     * highlighting is now ON or OFF.
     *
     * @param player the local player for whom the highlight status is toggled
     */
    public static void toggle(LocalPlayer player, CoordsManager manager) {
        enabled = !enabled;
        ChatMessages.highlightToggled(player, enabled);

        if (!enabled) return;

        if (!manager.ensureLoaded()) return;
        rebuildFromSnapshot(manager.getBlocksSnapshot());
    }

    /**
     * Rebuilds the cached collections for highlighted blocks from the provided snapshot.
     * This method clears the existing cached data and updates the caches with the
     * new block positions from the snapshot to ensure consistent rendering.
     *
     * @param snapshot the set of {@code BlockPos} instances representing the new
     *                 block positions to update the cached data
     */
    private static void rebuildFromSnapshot(Set<BlockPos> snapshot) {
        cachedSet.clear();
        cachedList.clear();
        cachedSet.addAll(snapshot);
        cachedList.addAll(snapshot);
    }

    /**
     * Adds a set of immutable block positions to the cache for highlighting purposes.
     * This method updates both the cached set and the corresponding list, preventing
     * duplicate entries while maintaining the order for rendering.
     *
     * @param added the set of {@code BlockPos} instances to be added to the cache;
     *              must not be {@code null} or empty. Each block position is converted
     *              to an immutable reference before caching.
     */
    public static void addToCache(Set<BlockPos> added) {
        if (!enabled || added == null || added.isEmpty()) return;

        for (BlockPos p : added) {
            BlockPos im = p.immutable();
            if (cachedSet.add(im)) {
                cachedList.add(im);
            }
        }
    }

    /**
     * Removes a set of block positions from the cached collections used for highlighting.
     * This method updates both the cached set and the cached list by removing the specified
     **/
    public static void removeFromCache(Set<BlockPos> removed) {
        if (!enabled || removed == null || removed.isEmpty()) return;

        cachedSet.removeAll(removed);

        cachedList.removeIf(removed::contains);
    }

    /**
     * Handles the rendering stage for highlighting specific blocks within the game world.
     * This method is triggered during the `RenderLevelStageEvent` and performs various checks to determine
     * if rendering should proceed. If enabled, it renders highlighted block boundaries.
     *
     * @param event the {@code RenderLevelStageEvent} instance that provides details about the current rendering
     *              stage in the game world
     */
    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        if (!enabled) return;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (cachedList.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        Camera camera = event.getCamera();

        double camX = camera.getPosition().x;
        double camY = camera.getPosition().y;
        double camZ = camera.getPosition().z;

        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = buffer.getBuffer(RenderType.lines());

        poseStack.pushPose();
        poseStack.translate(-camX, -camY, -camZ);
        float alpha = ClientConfig.ALPHA.get().floatValue();
        updateColorCache();

        BlockPos playerPos = mc.player.blockPosition();
        double maxDist = ClientConfig.RENDER_DISTANCE.get().doubleValue();
        double maxDistSq = maxDist * maxDist;

        for (BlockPos pos : cachedList) {
            if (pos.distSqr(playerPos) > maxDistSq) continue;

            AABB box = new AABB(pos).inflate(0.002D);

            LevelRenderer.renderLineBox(poseStack, consumer, box, r, g, b, alpha);
        }

        poseStack.popPose();
        buffer.endBatch(RenderType.lines());
    }

    /**
     * Determines the appropriate RGB color values based on the current color mode
     * set in the client configuration. The selected mode corresponds to a specific
     * preset of RGB values used for visual highlights.
     *
     * @return a float array containing three elements representing the RGB color values.
     *         The values are in the range of 0.0 to 1.0, with the order of the elements
     *         being red, green, and blue respectively.
     */
    private static void updateColorCache() {
        ClientConfig.ColorMode mode = ClientConfig.COLOR_MODE.get();
        if (mode == lastMode) return;
        lastMode = mode;

        switch (mode) {
            case DEFAULT -> { r=1.0F; g=0.85F; b=0.0F; }
            case DEUTERANOPIA -> { r=0.0F; g=0.55F; b=1.0F; }
            case PROTANOPIA -> { r=0.0F; g=0.75F; b=1.0F; }
            case TRITANOPIA -> { r=1.0F; g=0.4F; b=0.9F; }
            case HIGH_CONTRAST -> { r=1.0F; g=1.0F; b=1.0F; }
        }
    }
}
