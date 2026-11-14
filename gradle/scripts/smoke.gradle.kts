import com.cleanroommc.gradle.api.task.mc.RunMinecraft
import net.minecraftforge.renamer.gradle.RenameJar
import net.minecraftforge.renamer.gradle.RenamerExtension
import java.time.Duration

val sourceSets = extensions.getByType<SourceSetContainer>()

if (providers.gradleProperty("smoke_test").orElse("false").get().toBoolean()) {
    val smokeDirectory = layout.projectDirectory.dir("run/smoke-server").asFile
    val result = layout.buildDirectory.file("verification/server-smoke.txt").get().asFile
    tasks.named<RunMinecraft>("runServer") {
        timeout.set(Duration.ofMinutes(3))
        dependsOn(tasks.named("testClasses"))
        classpath(sourceSets.getByName("test").output)
        workingDir(smokeDirectory)
        systemProperty("mrtjpcore.smoke.result", result.absolutePath)
        doFirst {
            smokeDirectory.mkdirs()
            result.parentFile.mkdirs()
            result.delete()
            smokeDirectory.resolve("eula.txt").writeText("eula=true\n")
            smokeDirectory.resolve("server.properties").writeText("server-ip=127.0.0.1\nserver-port=0\nonline-mode=false\nlevel-type=FLAT\ngenerate-structures=false\nview-distance=3\n")
        }
        doLast { check(result.isFile && result.readText().trim() == "PASS") { "Server regression checks did not finish" } }
    }
}
val smokeJar = tasks.register<Jar>("smokeJar") {
    from(sourceSets.getByName("test").output)
    include("mrtjp/core/ServerSmokeMod*", "mrtjp/core/SmokeChecks*", "mcmod.info")
    archiveClassifier.set("smoke-mcp")
    destinationDirectory.set(layout.buildDirectory.dir("smoke"))
}
extensions.getByType<RenamerExtension>().classes("reobfSmokeJar", smokeJar) {
    map.setFrom(tasks.named<RenameJar>("reobfJar").map { it.map })
    libraries.from(sourceSets.getByName("main").output, configurations.named("compileClasspath"))
    output.set(layout.buildDirectory.file("smoke/MrTJPCore-Rescaled-smoke-tests.jar"))
}
val productionMods = configurations.create("productionSmokeMods")
dependencies {
    add(productionMods.name, "codechicken.lib:codechickenlib:${providers.gradleProperty("ccl_version").get()}") { isTransitive = false }
    add(productionMods.name, "curse.maven:scalar-1012168:${providers.gradleProperty("scalar_file").get()}") { isTransitive = false }
}
tasks.register<Sync>("stageProductionSmoke") {
    from(productionMods, tasks.named<RenameJar>("reobfJar").flatMap { it.output }, tasks.named<RenameJar>("reobfSmokeJar").flatMap { it.output })
    into(layout.buildDirectory.dir("production-smoke/mods"))
}

if (providers.gradleProperty("client_smoke").orElse("false").get().toBoolean()) {
    val directory = layout.projectDirectory.dir("run/smoke-client").asFile
    val result = layout.buildDirectory.file("verification/client-smoke.txt").get().asFile
    tasks.named<RunMinecraft>("runClient") {
        timeout.set(Duration.ofMinutes(3))
        dependsOn(tasks.named("testClasses"))
        classpath(sourceSets.getByName("test").output)
        workingDir(directory)
        systemProperty("mrtjpcore.client.result", result.absolutePath)
        doFirst { directory.mkdirs(); result.parentFile.mkdirs(); result.delete() }
        doLast { check(result.isFile && result.readText().trim() == "PASS") { "Client regression checks did not finish" } }
    }
}
