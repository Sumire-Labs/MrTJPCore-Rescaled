import com.cleanroommc.gradle.api.ext.CleanroomExtension
val cleanroom = extensions.getByType<CleanroomExtension>()
repositories {
    mavenCentral()
    ivy("https://github.com/Refitbench/CodeChickenLib/releases/download") {
        patternLayout { artifact("[revision]/[artifact]-[revision]-universal.[ext]") }
        metadataSources { artifact() }
        content { includeModule("codechicken.lib", "codechickenlib") }
    }
    maven("https://cursemaven.com")
    maven("https://maven.cleanroommc.com") {
        content { includeGroup("mezz") }
    }
}
dependencies {
    add("implementation", "org.scala-lang:scala3-library_3:${providers.gradleProperty("scala_version").get()}")
    add("implementation", cleanroom.deobf("codechicken.lib:codechickenlib:${providers.gradleProperty("ccl_version").get()}"))
    add("compileOnly", cleanroom.deobf("mezz:jei:${providers.gradleProperty("jei_version").get()}@jar"))
    if (providers.gradleProperty("jei_smoke").orElse("false").get().toBoolean()) {
        add("runtimeOnly", cleanroom.deobf("mezz:jei:${providers.gradleProperty("jei_version").get()}@jar"))
    }
    add("runtimeOnly", (cleanroom.deobf("curse.maven:scalar-1012168:${providers.gradleProperty("scalar_file").get()}") as ModuleDependency).apply { isTransitive = false })
}
