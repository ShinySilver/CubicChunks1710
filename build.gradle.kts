
plugins {
    id("com.gtnewhorizons.gtnhconvention")
}

tasks.register<Sync>("syncRwgDevJar") {
    dependsOn(tasks.named("shadowJar"))
    from(tasks.named<AbstractArchiveTask>("shadowJar").flatMap { it.archiveFile })
    into(layout.buildDirectory.dir("rwg"))
    rename { "cubicchunks-dev.jar" }
}
