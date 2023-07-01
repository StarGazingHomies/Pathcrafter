package stargazing.pathcrafter.overlay;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.AbstractQuadRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import stargazing.pathcrafter.Pathcrafter;
import stargazing.pathcrafter.structures.Vertex;

import java.util.Objects;


public class OverlayRenderer implements WorldRenderEvents.End {
    // Very much don't know what to do here

    public static final int MAX_BUFFER_SIZE = 2097152;

    public OverlayRenderer() {

    }

    public void test(WorldRenderContext context) {

        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();

        Camera camera = context.camera();

        MatrixStack matrixStack = new MatrixStack();
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
        matrixStack.translate(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);

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

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableCull();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);

        tessellator.draw();

        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.enableCull();
    }

    @Override
    public void onEnd(WorldRenderContext context) {
        MinecraftClient player = MinecraftClient.getInstance();
        Vec3d campos = Objects.requireNonNull(player.getCameraEntity()).getPos();
        context.matrixStack();

        if (Pathcrafter.terrain != null) test(context);
    }
}
