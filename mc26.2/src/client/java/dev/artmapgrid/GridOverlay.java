package dev.artmapgrid;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public final class GridOverlay {
    private static final Identifier WHITE = Identifier.fromNamespaceAndPath("artmap_grid", "textures/white.png");
    private static final float SIZE = 128;
    private static final float STEP = SIZE / 32;
    private static final float Z = -0.04f;

    public static void submit(PoseStack stack, SubmitNodeCollector collector, GridStyle style, int light) {
        if (style.gridEnabled()) submitGrid(stack, collector, style, light);
        if (style.sectionsEnabled()) submitSections(stack, collector, style, light);
        if (style.centerEnabled()) submitCenter(stack, collector, light);
    }

    private static void submitSections(PoseStack stack, SubmitNodeCollector collector, GridStyle style, int light) {
        collector.order(2).submitCustomGeometry(stack, RenderTypes.text(WHITE), (pose, buffer) ->
                drawSections(pose, buffer, style.sectionSize(), light));
    }

    private static void drawSections(PoseStack.Pose pose, VertexConsumer buffer, int sectionSize, int light) {
        if (!GridConfig.validSectionSize(sectionSize)) return;
        float spacing = sectionSize * STEP;
        float half = 0.18f;
        int cells = 32 / sectionSize;
        for (int i = 0; i <= cells; i++) {
            float coordinate = i * spacing;
            sectionQuad(pose, buffer, Math.max(0, coordinate-half), 0,
                    Math.min(SIZE, coordinate+half), SIZE, light);
            for (int cell = 0; cell < cells; cell++) {
                sectionQuad(pose, buffer, cell*spacing+half, Math.max(0, coordinate-half),
                        (cell+1)*spacing-half, Math.min(SIZE, coordinate+half), light);
            }
        }
    }

    private static void sectionQuad(PoseStack.Pose pose, VertexConsumer buffer,
                                    float x1, float y1, float x2, float y2, int light) {
        int color = 0xB3F08ABB;
        float z = -0.05f;
        buffer.addVertex(pose, x1, y2, z).setColor(color).setUv(0, 1).setLight(light);
        buffer.addVertex(pose, x2, y2, z).setColor(color).setUv(1, 1).setLight(light);
        buffer.addVertex(pose, x2, y1, z).setColor(color).setUv(1, 0).setLight(light);
        buffer.addVertex(pose, x1, y1, z).setColor(color).setUv(0, 0).setLight(light);
    }

    private static void submitGrid(PoseStack stack, SubmitNodeCollector collector, GridStyle style, int light) {
        int color = (Math.round(style.opacity() * 255) << 24) | 0xB8B8B8;
        float half = style.width() / 2;
        collector.order(1).submitCustomGeometry(stack, RenderTypes.text(WHITE), (pose, buffer) -> {
            for (int i = 0; i <= 32; i++) {
                float coordinate = i * STEP;
                quad(pose, buffer, Math.max(0, coordinate - half), 0,
                        Math.min(SIZE, coordinate + half), SIZE, color, light);
                // Avoid drawing intersections twice, preserving consistent transparency.
                for (int cell = 0; cell < 32; cell++) {
                    quad(pose, buffer, cell * STEP + half, Math.max(0, coordinate - half),
                            (cell + 1) * STEP - half, Math.min(SIZE, coordinate + half), color, light);
                }
            }
        });
    }

    private static void submitCenter(PoseStack stack, SubmitNodeCollector collector, int light) {
        collector.order(2).submitCustomGeometry(stack, RenderTypes.text(WHITE), (pose, buffer) -> {
            // Cells 15 and 16 (zero-based) on both axes are the central four cells of a 32×32 canvas.
            // Each marker has margins inside its cell, plus a dark outline for light paintings.
            for (int row = 15; row <= 16; row++) {
                for (int column = 15; column <= 16; column++) {
                    float x = (column + 0.5f) * STEP;
                    float y = (row + 0.5f) * STEP;
                    cross(pose, buffer, x, y, 0.48f, -0.06f, 0xD9222529, light);
                    cross(pose, buffer, x, y, 0.22f, -0.065f, 0xF2F4F4F0, light);
                }
            }
        });
    }

    private static void cross(PoseStack.Pose pose, VertexConsumer buffer, float x, float y,
                              float width, float z, int color, int light) {
        float arm = 0.85f;
        segment(pose, buffer, x - arm, y - arm, x + arm, y + arm, width, z, color, light);
        segment(pose, buffer, x - arm, y + arm, x + arm, y - arm, width, z, color, light);
    }

    private static void segment(PoseStack.Pose pose, VertexConsumer buffer,
                                float x1, float y1, float x2, float y2,
                                float width, float z, int color, int light) {
        float length = (float) Math.hypot(x2 - x1, y2 - y1);
        float nx = -(y2 - y1) / length * width / 2;
        float ny = (x2 - x1) / length * width / 2;
        buffer.addVertex(pose, x1 + nx, y1 + ny, z).setColor(color).setUv(0, 1).setLight(light);
        buffer.addVertex(pose, x2 + nx, y2 + ny, z).setColor(color).setUv(1, 1).setLight(light);
        buffer.addVertex(pose, x2 - nx, y2 - ny, z).setColor(color).setUv(1, 0).setLight(light);
        buffer.addVertex(pose, x1 - nx, y1 - ny, z).setColor(color).setUv(0, 0).setLight(light);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer buffer,
                             float x1, float y1, float x2, float y2, int color, int light) {
        buffer.addVertex(pose, x1, y2, Z).setColor(color).setUv(0, 1).setLight(light);
        buffer.addVertex(pose, x2, y2, Z).setColor(color).setUv(1, 1).setLight(light);
        buffer.addVertex(pose, x2, y1, Z).setColor(color).setUv(1, 0).setLight(light);
        buffer.addVertex(pose, x1, y1, Z).setColor(color).setUv(0, 0).setLight(light);
    }
}
