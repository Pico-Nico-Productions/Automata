package pro.piconico.automata.client.registry;

import java.util.function.Function;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Builder;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import net.minecraft.client.gl.RenderPipelines;

public class AutomataClientRenderPipelines {
    public static final RenderPipeline NO_CULL = register(AutomataClientRegistry.NO_CULL, builder -> builder.withCull(false), RenderPipelines.POSITION_COLOR_SNIPPET);

    private static RenderPipeline register(String name, Function<Builder, Builder> builder, Snippet... snippets) {
        Builder pipelineBase = RenderPipeline.builder(snippets).withLocation(AutomataClientRegistry.pipelineId(name));
        RenderPipeline pipeline = builder.apply(pipelineBase).build();

        return RenderPipelines.register(pipeline);
    }

    public static void initialize() {
    }
}
