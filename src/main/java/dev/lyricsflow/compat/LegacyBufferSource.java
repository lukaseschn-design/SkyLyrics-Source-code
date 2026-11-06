package dev.lyricsflow.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

public class LegacyBufferSource implements AutoCloseable {
    private static final ThreadLocal<BufferSource> CURRENT = new ThreadLocal<>();

    private final SubmitNodeCollector collector;
    private final Map<RenderType, BufferedConsumer> buffers = new HashMap<>();

    protected LegacyBufferSource(SubmitNodeCollector collector) {
        this.collector = Objects.requireNonNull(collector, "collector");
    }

    public static BufferSource current() {
        return CURRENT.get();
    }

    public VertexConsumer getBuffer(RenderType type) {
        return buffers.computeIfAbsent(type, ignored -> new BufferedConsumer());
    }

    public void drawText(Component text, float x, float y, int color, boolean shadow, Matrix4f matrix,
                         Font.DisplayMode mode, int background, int light) {
        PoseStack pose = new PoseStack();
        pose.last().pose().set(matrix);
        collector.submitText(pose, x, y, text.getVisualOrderText(), shadow, mode, color, background, light, 0);
    }

    public void endBatch(RenderType type) {
        BufferedConsumer buffer = buffers.remove(type);
        if (buffer != null && !buffer.vertices.isEmpty()) {
            List<Vertex> vertices = List.copyOf(buffer.vertices);
            collector.submitCustomGeometry(new PoseStack(), type, (pose, output) -> {
                for (Vertex vertex : vertices) {
                    vertex.emit(output);
                }
            });
        }
    }

    public void endBatch() {
        for (RenderType type : List.copyOf(buffers.keySet())) {
            endBatch(type);
        }
    }

    @Override
    public void close() {
        endBatch();
        if (CURRENT.get() == this) {
            CURRENT.remove();
        }
    }

    public static final class BufferSource extends LegacyBufferSource {
        public BufferSource(SubmitNodeCollector collector) {
            super(collector);
            CURRENT.set(this);
        }
    }

    private static final class BufferedConsumer implements VertexConsumer {
        private final List<Vertex> vertices = new ArrayList<>();
        private Vertex last;

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            last = new Vertex(x, y, z);
            vertices.add(last);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            if (last != null) {
                last.red = red;
                last.green = green;
                last.blue = blue;
                last.alpha = alpha;
                last.packedColor = false;
            }
            return this;
        }

        @Override
        public VertexConsumer setColor(int color) {
            if (last != null) {
                last.color = color;
                last.packedColor = true;
            }
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            if (last != null) {
                last.u = u;
                last.v = v;
            }
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            if (last != null) {
                last.overlayU = u;
                last.overlayV = v;
            }
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            if (last != null) {
                last.lightU = u;
                last.lightV = v;
            }
            return this;
        }

        @Override
        public VertexConsumer setUv3(float u, float v) {
            if (last != null) {
                last.uv3u = u;
                last.uv3v = v;
            }
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            if (last != null) {
                last.normalX = x;
                last.normalY = y;
                last.normalZ = z;
            }
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            if (last != null) {
                last.lineWidth = width;
            }
            return this;
        }
    }

    private static final class Vertex {
        private final float x;
        private final float y;
        private final float z;
        private int color = -1;
        private boolean packedColor = true;
        private int red = 255;
        private int green = 255;
        private int blue = 255;
        private int alpha = 255;
        private float u;
        private float v;
        private int overlayU;
        private int overlayV;
        private int lightU;
        private int lightV;
        private float uv3u;
        private float uv3v;
        private float normalX;
        private float normalY = 1.0f;
        private float normalZ;
        private float lineWidth = 1.0f;

        private Vertex(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private void emit(VertexConsumer output) {
            output.addVertex(x, y, z);
            if (packedColor) {
                output.setColor(color);
            } else {
                output.setColor(red, green, blue, alpha);
            }
            output.setUv(u, v)
                .setUv1(overlayU, overlayV)
                .setUv2(lightU, lightV)
                .setUv3(uv3u, uv3v)
                .setNormal(normalX, normalY, normalZ)
                .setLineWidth(lineWidth);
        }
    }
}
