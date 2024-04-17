package org.nativeSupport

import org.nativeSupport.maven.m2
import java.io.File

val home: String by lazy { System.getProperty("user.home") }
operator fun String.div(child: String) = "$this${File.separatorChar}$child"

object gradle {
    val cache by lazy { home / ".gradle" / "caches" / "modules-2" / "files-2.1" }
}

object maven {
    val m2 = home / ".m2"
}

object xdg {
    val cache by lazy { System.getenv("XDG_CACHE_HOME") ?: (home / "cache") }
}

fun nativeLibraryFrom(path: String): NativeLibrary =
    when {
        path.startsWith(gradle.cache) -> {
            // gav / hash / filename
            val gavhf = path.removePrefix(gradle.cache).drop(1) // leading separator
            val (g, a, v, h, f) = gavhf.split(File.separatorChar)
            NativeLibrary(gradle.cache, g, a, v, h, f)
        }
        path.startsWith(m2) -> TODO()
        else -> error("Unknown library path $path")
    }

//val String.gavhf: List<String>
//    get() {
//        var string = this
//        val f = string.substringAfterLast(File.separatorChar)
//        string = string.substringBeforeLast(File.separatorChar)
//        val h = string.substringAfterLast(File.separatorChar)
//    }

data class NativeLibrary(val location: String,
                         val group: String, val name: String, val version: String,
                         val hash: String, val filename: String)