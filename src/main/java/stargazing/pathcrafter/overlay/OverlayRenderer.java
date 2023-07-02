package stargazing.pathcrafter.overlay;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.AbstractQuadRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.*;
import org.lwjgl.opengl.GL11;
import stargazing.pathcrafter.Pathcrafter;
import stargazing.pathcrafter.structures.TerrainGraph;
import stargazing.pathcrafter.structures.Vertex;

import java.lang.Math;
import java.util.Objects;


public class OverlayRenderer implements WorldRenderEvents.End {
    // Very much don't know what to do here

    public static final int MAX_BUFFER_SIZE = 2097152;

    public OverlayRenderer() {

    }

    public void drawVertices(MatrixStack matrixStack) {
        Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        float offSet = 0.02f;
        float r=0.0f, g=1.0f, b=0.0f, a=1.0f;

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (Vertex v : Pathcrafter.terrain.getGraph().vertices) {
            buffer.vertex(positionMatrix, (float) v.x-offSet, (float) v.y, (float) v.z+offSet).color(r, g, b, a).next();
            buffer.vertex(positionMatrix, (float) v.x-offSet, (float) v.y, (float) v.z-offSet).color(r, g, b, a).next();
            buffer.vertex(positionMatrix, (float) v.x+offSet, (float) v.y, (float) v.z-offSet).color(r, g, b, a).next();
            buffer.vertex(positionMatrix, (float) v.x+offSet, (float) v.y, (float) v.z+offSet).color(r, g, b, a).next();
        }
        tessellator.draw();
    }

    public void test(WorldRenderContext context, MatrixStack matrixStack) {
        // if not generated, skip

        MinecraftClient player = MinecraftClient.getInstance();

        matrixStack.push();
        float scale = 0.015f;
        float sqrted = (float) Math.sqrt(0.5);
        float yOffset = 0.02f;

        if (Pathcrafter.terrain.getGraph().edges.size() != 0)
            for (TerrainGraph.Edge e : Pathcrafter.terrain.getGraph().edges.get(0)) {
                Vertex v = Pathcrafter.terrain.getGraph().getVertex(e.to);
                matrixStack.push();
                Vector3d textPos = new Vector3d(v.x, v.y, v.z);
                matrixStack.translate(textPos.x, textPos.y + yOffset, textPos.z);
                matrixStack.scale(scale, scale, scale);
                matrixStack.multiply(new Quaternionf(sqrted, 0.0f, 0.0f, sqrted));
                TextRenderer textRenderer = player.textRenderer;
                textRenderer.draw(matrixStack, String.format("%.3f", e.weight), 0.0f, 0.0f, 0x0000ffff);
                matrixStack.pop();
            }

        for (int i=0; i<Pathcrafter.terrain.getGraph().vertices.size(); i++) {
            Vertex v = Pathcrafter.terrain.getGraph().vertices.get(i);
            if (!(0.0 <= v.x && v.x <= 16.00 && 0.0 <= v.z && v.z <= 16.0)) continue;
            matrixStack.push();
            Vector3d textPos = new Vector3d(v.x, v.y, v.z);
            matrixStack.translate(textPos.x, textPos.y + yOffset, textPos.z);
            matrixStack.scale(scale, scale, scale);
            matrixStack.multiply(new Quaternionf(sqrted, 0.0f, 0.0f, sqrted));
            TextRenderer textRenderer = player.textRenderer;
            textRenderer.draw(matrixStack, String.format("%04d", i), -24f, 0.0f, 0x00ff00ff);
            matrixStack.pop();
        }

        matrixStack.pop();
    }

    @Override
    public void onEnd(WorldRenderContext context) {

        if (Pathcrafter.terrain == null) return;
        if (!Pathcrafter.debugRenderToggle) return;

        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();

        Camera camera = context.camera();

        MatrixStack matrixStack = context.matrixStack();
        matrixStack.push();
        matrixStack.loadIdentity();
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
        matrixStack.translate(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);


        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        RenderSystem.disableCull();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);

        drawVertices(matrixStack);
        test(context, matrixStack);

        matrixStack.pop();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.enableCull();
    }
}
