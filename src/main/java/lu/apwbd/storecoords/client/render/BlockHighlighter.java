package lu.apwbd.storecoords.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lu.apwbd.storecoords.StoreCoords;
import lu.apwbd.storecoords.client.ChatMessages;
import lu.apwbd.storecoords.client.KeyInputHandler;
import lu.apwbd.storecoords.client.config.ClientConfig;
import lu.apwbd.storecoords.io.CoordsManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = StoreCoords.MOD_ID, value = Dist.CLIENT)
public final class BlockHighlighter {

    private static boolean enabled = false;

    private static final double MAX_DISTANCE = 64.0;
    private static final double MAX_DISTANCE_SQ = MAX_DISTANCE * MAX_DISTANCE;

    private static final boolean THROUGH_WALLS = false;

    private static boolean dirty = true;
    private static final List<BlockPos> cachedPositions = new ArrayList<>();

    private BlockHighlighter() {}

    /**
     * Toggles the highlighting state between enabled and disabled. When toggled,
     * it sends a chat message to the specified player indicating whether the
     * highlighting is now ON or OFF.
     *
     * @param player the local player for whom the highlight status is toggled
     */
    public static void toggle(net.minecraft.client.player.LocalPlayer player) {
        enabled = !enabled;
        dirty = true;
        ChatMessages.highlightToggled(player, enabled);
    }

    /** Wenn sich gespeicherte Blöcke ändern -> Cache neu bauen */
    public static void markDirty() {
        dirty = true;
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

        CoordsManager manager = KeyInputHandler.getManager();
        if (!manager.ensureLoaded(mc.player)) return;

        if (dirty) {
            rebuildCache(manager);
            dirty = false;
        }

        if (cachedPositions.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        Camera camera = event.getCamera();

        double camX = camera.getPosition().x;
        double camY = camera.getPosition().y;
        double camZ = camera.getPosition().z;

        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = buffer.getBuffer(RenderType.lines());

        if (THROUGH_WALLS) RenderSystem.disableDepthTest();

        poseStack.pushPose();
        poseStack.translate(-camX, -camY, -camZ);

        BlockPos playerPos = mc.player.blockPosition();

        float[] c = getColor();
        float alpha = ClientConfig.ALPHA.get().floatValue();


        for (BlockPos pos : cachedPositions) {
            if (pos.distSqr(playerPos) > MAX_DISTANCE_SQ) continue;

            AABB box = new AABB(pos).inflate(0.002D);

            LevelRenderer.renderLineBox(poseStack, consumer, box,
                    c[0], c[1], c[2], alpha);
        }

        poseStack.popPose();

        buffer.endBatch(RenderType.lines());

        if (THROUGH_WALLS) RenderSystem.enableDepthTest();
    }

    /**
     * Rebuilds the cached positions used for rendering by clearing the current cache
     * and repopulating it with a snapshot of blocks provided by the specified manager.
     *
     * @param manager the {@code CoordsManager} instance that provides the current
     *                snapshot of block positions to populate the cache
     */
    private static void rebuildCache(CoordsManager manager) {
        cachedPositions.clear();
        Set<BlockPos> snapshot = manager.getBlocksSnapshot();
        cachedPositions.addAll(snapshot);
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
    private static float[] getColor() {
        ClientConfig.ColorMode mode = ClientConfig.COLOR_MODE.get();

        // Rückgabe: {r,g,b}
        return switch (mode) {
            case DEFAULT -> new float[]{1.0F, 0.85F, 0.0F};       // gelb/orange
            case DEUTERANOPIA -> new float[]{0.0F, 0.55F, 1.0F};  // blau (sehr sicher)
            case PROTANOPIA -> new float[]{0.0F, 0.75F, 1.0F};    // cyan-ish
            case TRITANOPIA -> new float[]{1.0F, 0.4F, 0.9F};     // magenta/pink
            case HIGH_CONTRAST -> new float[]{1.0F, 1.0F, 1.0F};  // weiß
        };
    }

}
