plugins {
    `kotlin-dsl`
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set((project.property("targetJavaVersion") as String).toInt())
}
