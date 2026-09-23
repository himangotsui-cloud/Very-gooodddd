package me.example.autobuilder;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Map;

/**
 * Draws a lightweight translucent "hologram" of the blocks still needed — a blue wireframe box per position,
 * Litematica-style but wireframe-only (not full baked block models) so it stays cheap on huge schematics.
 * Shows the standing preview when idle, or live remaining-blocks progress while a build is running.
 */
public class GhostRenderer {
    private static final float R = 0.35f, G = 0.60f, B = 1.0f;
    private static final double MAX_DRAW_DIST_SQ = 96.0 * 96.0;
    private static final int MAX_BOXES_PER_FRAME = 6000; // safety cap so massive schematics can't tank FPS

    public static void register() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(GhostRenderer::render);
    }

    private static void render(WorldRenderContext context) {
        List<Map.Entry<BlockPos, BlockState>> entries = BuildController.renderEntries();
        if (entries.isEmpty()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider consumers = context.consumers();
        if (matrices == null || consumers == null) return;

        Vec3d cam = context.camera().getPos();
        VertexConsumer lines = consumers.getBuffer(RenderLayer.getLines());

        matrices.push();
        matrices.translate(-cam.x, -cam.y, -cam.z);

        Vec3d playerPos = mc.player.getPos();
        int drawn = 0;
        for (var e : entries) {
            if (drawn >= MAX_BOXES_PER_FRAME) break;
            BlockPos p = e.getKey();
            if (playerPos.squaredDistanceTo(Vec3d.ofCenter(p)) > MAX_DRAW_DIST_SQ) continue;
            double inset = 0.06; // shrink slightly so it doesn't z-fight with any real block right next to it
            Box box = new Box(p.getX() + inset, p.getY() + inset, p.getZ() + inset,
                    p.getX() + 1 - inset, p.getY() + 1 - inset, p.getZ() + 1 - inset);
            WorldRenderer.drawBox(matrices, lines, box, R, G, B, 0.85f);
            drawn++;
        }

        matrices.pop();
    }
}
