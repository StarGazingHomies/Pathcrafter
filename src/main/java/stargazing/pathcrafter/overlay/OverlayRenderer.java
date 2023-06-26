package stargazing.pathcrafter.overlay;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.AbstractQuadRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import stargazing.pathcrafter.Pathcrafter;

import java.util.Objects;


public class OverlayRenderer implements HudRenderCallback {
    // Very much don't know what to do here

    private VertexConsumer vertexUploader;

    public static final int MAX_BUFFER_SIZE = 2097152;
    private final BufferBuilder buffer = new BufferBuilder(MAX_BUFFER_SIZE);

    public OverlayRenderer() {

    }

    public void test(Vec3d campos) {

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        double x1 = 0.0f - campos.getX(), y1 = 64.0f - campos.getY(), z1 = 0.0f - campos.getZ();
        double x2 = 0.0f - campos.getX(), y2 = 64.0f - campos.getY(), z2 = 1.0f - campos.getZ();
        double x3 = 1.0f - campos.getX(), y3 = 64.0f - campos.getY(), z3 = 1.0f - campos.getZ();
        double x4 = 1.0f - campos.getX(), y4 = 64.0f - campos.getY(), z4 = 0.0f - campos.getZ();
        float r=0.0f, g=1.0f, b=0.0f, a=1.0f;

        buffer.vertex(x1, y1, z1).color(r, g, b, a).next();
        buffer.vertex(x2, y2, z2).color(r, g, b, a).next();
        buffer.vertex(x3, y3, z3).color(r, g, b, a).next();
        buffer.vertex(x4, y4, z4).color(r, g, b, a).next();

        BufferBuilder.BuiltBuffer builtBuffer = buffer.end();

        try (VertexBuffer vertexBuffer = new VertexBuffer()) {
            vertexBuffer.bind();
            vertexBuffer.upload(builtBuffer);
            vertexBuffer.draw();
        }
    }

    @Override
    public void onHudRender(MatrixStack matrixStack, float tickDelta) {
        MinecraftClient player = MinecraftClient.getInstance();
        Vec3d campos = Objects.requireNonNull(player.getCameraEntity()).getPos();

        Matrix4f matrix = matrixStack.peek().getPositionMatrix();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        matrixStack.push();

        player.getItemRenderer();

        //test(campos);
    }
}
