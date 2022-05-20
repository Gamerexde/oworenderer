import io.github.kgpu.*
import io.github.kgpu.kshader.*

object Shaders {
    
    const val VERTEX = """
        #version 450

        out gl_PerVertex {
            vec4 gl_Position;
        };

        const vec2 positions[3] = vec2[3](
            vec2(0.0, -0.5),
            vec2(0.5, 0.5),
            vec2(-0.5, 0.5)
        );

        void main() {
            gl_Position = vec4(positions[gl_VertexIndex], 0.0, 1.0);
        }
    """

    const val FRAG = """
        #version 450

        layout(location = 0) out vec4 outColor;

        void main() {
            outColor = vec4(1.0, 0.0, 0.0, 1.0);
        }
    """
}

object Application {
    val TITLE = "Application Title Here"

    suspend fun run(){
        val window = Window()
        window.setTitle(TITLE)

        val adapter = Kgpu.requestAdapterAsync(window)
        val device = adapter.requestDeviceAsync();
        val vertexShader = device.createShaderModule(KShader.compile("vertex", Shaders.VERTEX, KShaderType.VERTEX))
        val fragShader = device.createShaderModule(KShader.compile("frag", Shaders.FRAG, KShaderType.FRAGMENT))

        val pipelineLayout = device.createPipelineLayout(PipelineLayoutDescriptor())

        val pipelineDesc = createRenderPipeline(pipelineLayout, vertexShader, fragShader)
        val pipeline = device.createRenderPipeline(pipelineDesc)
        val swapChainDescriptor = SwapChainDescriptor(device, TextureFormat.BGRA8_UNORM);

        var swapChain = window.configureSwapChain(swapChainDescriptor)
        window.onResize = { size : WindowSize -> 
            swapChain = window.configureSwapChain(swapChainDescriptor)
        }

        Kgpu.runLoop(window) {
            val swapChainTexture = swapChain.getCurrentTextureView();
            val cmdEncoder = device.createCommandEncoder();

            val colorAttachment = RenderPassColorAttachmentDescriptor(swapChainTexture, Color.WHITE)
            val renderPassEncoder = cmdEncoder.beginRenderPass(RenderPassDescriptor(colorAttachment))
            renderPassEncoder.setPipeline(pipeline)
            renderPassEncoder.draw(3, 1)
            renderPassEncoder.endPass()

            val cmdBuffer = cmdEncoder.finish()
            val queue = device.getDefaultQueue()
            queue.submit(cmdBuffer)
            swapChain.present();
        }
    }
}

private fun createRenderPipeline(
    pipelineLayout: PipelineLayout,
    vertexModule: ShaderModule,
    fragModule: ShaderModule
): RenderPipelineDescriptor {
    return RenderPipelineDescriptor(
        pipelineLayout,
        ProgrammableStageDescriptor(vertexModule, "main"),
        ProgrammableStageDescriptor(fragModule, "main"),
        PrimitiveTopology.TRIANGLE_LIST,
        RasterizationStateDescriptor(),
        arrayOf(
            ColorStateDescriptor(
                TextureFormat.BGRA8_UNORM,
                BlendDescriptor(),
                BlendDescriptor(),
                0xF
            )
        ),
        Kgpu.undefined,
        VertexStateDescriptor(null),
        1,
        0xFFFFFFFF,
        false
    )
}