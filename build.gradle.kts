// Root build file. All build behavior lives in build-logic convention plugins.
tasks.register("moduleCount") {
    doLast {
        println("Modules: ${rootProject.subprojects.size}")
    }
}

// Architectural gate (AGENTS.md): feature modules must never depend on
// privilege implementation modules — only :core:privileges APIs.
tasks.register("verifyModuleBoundaries") {
    description = "Fails if any :feature:* module depends on a :privilege:* implementation module."
    group = "verification"
    doLast {
        val violations = rootProject.subprojects
            .filter { it.path.startsWith(":feature:") }
            .flatMap { p ->
                p.configurations.flatMap { cfg ->
                    cfg.allDependencies
                        .withType(org.gradle.api.artifacts.ProjectDependency::class.java)
                        .map { dep -> "${p.path} -> ${dep.path} (${cfg.name})" }
                        .filter { it.contains("-> :privilege:") }
                }
            }
        check(violations.isEmpty()) {
            "Feature modules must not depend on :privilege:* implementation modules:\n" +
                violations.joinToString("\n")
        }
        println("Module boundary check passed for all :feature:* modules.")
    }
}
