import io.github.kgpu.Kgpu
import io.github.kgpu.kshader.KShader

fun main(){
    Kgpu.init(true)
    KShader.init()

    kotlinx.coroutines.runBlocking {
        Application.run()    
    }
}