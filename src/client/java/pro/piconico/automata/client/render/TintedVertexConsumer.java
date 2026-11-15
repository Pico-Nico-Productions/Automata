package pro.piconico.automata.client.render;

import net.minecraft.client.render.VertexConsumer;

public class TintedVertexConsumer implements VertexConsumer {
    private final VertexConsumer vertexConsumer;
    private final int tintArgb;

    public TintedVertexConsumer(VertexConsumer vertexConsumer, int tintArgb) {
        this.vertexConsumer = vertexConsumer;
        this.tintArgb = tintArgb;
    }

    @Override
    public VertexConsumer color(int argb) {
        int red = (argb >> 16) & 0xFF;
        int green = (argb >> 8) & 0xFF;
        int blue = argb & 0xFF;
        int alpha = argb >>> 24;

        return color(red, green, blue, alpha);
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        int tintRed = (tintArgb >> 16) & 0xFF;
        int tintGreen = (tintArgb >> 8) & 0xFF;
        int tintBlue = tintArgb & 0xFF;
        int tintAlpha = tintArgb >>> 24;

        return vertexConsumer.color( //
            red * tintRed / 255, //
            green * tintGreen / 255, //
            blue * tintBlue / 255, //
            alpha * tintAlpha / 255 //
        );
    }

    @Override
    public VertexConsumer light(int u, int v) {
        return vertexConsumer.light(u, v);
    }

    @Override
    public VertexConsumer lineWidth(float width) {
        return vertexConsumer.lineWidth(width);
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        return vertexConsumer.normal(x, y, z);
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        return vertexConsumer.overlay(u, v);
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        return vertexConsumer.texture(u, v);
    }

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        return vertexConsumer.vertex(x, y, z);
    }
}
