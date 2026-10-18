// Root build file. All build behavior lives in build-logic convention plugins.
tasks.register("moduleCount") {
    doLast {
        println("Modules: ${rootProject.subprojects.size}")
    }
}
