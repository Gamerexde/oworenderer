import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import io.github.kgpu.kshader.KShader

fun main(){
    KShader.init()

    GlobalScope.launch {
        Application.run()    
    }
}