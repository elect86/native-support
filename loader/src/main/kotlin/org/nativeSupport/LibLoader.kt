package org.nativeSupport

import java.io.File
import java.lang.management.ManagementFactory

object LibLoader {

//    val cachePath = ""

    /** load all conventional native libraries */
    fun load() {
        val sep = File.separatorChar
        val natives = ManagementFactory.getRuntimeMXBean().classPath
                .split(File.pathSeparatorChar)
                .filter { it.endsWith("-natives-${OS.current}-${Arch.current}.jar") }
                .map(::nativeLibraryFrom)
        for (native in natives) {
            if (File())
        }
    }
}