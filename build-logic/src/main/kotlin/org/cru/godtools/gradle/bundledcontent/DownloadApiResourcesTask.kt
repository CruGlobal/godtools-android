package org.cru.godtools.gradle.bundledcontent

import de.undercouch.gradle.tasks.download.DownloadAction
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class DownloadApiResourcesTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val resources: RegularFileProperty

    @get:Input
    lateinit var api: String

    @get:OutputDirectory
    abstract val output: DirectoryProperty
    @get:Input
    var outputSubDir: String? = null

    @TaskAction
    fun downloadApiResources() {
        val resourcesJson = resources.asFile.get().loadJson()
        val downloadDir = output.dir(outputSubDir ?: ".").get().asFile

        resourcesJson.keySet().map { resource ->
            val fileName = resourcesJson.getString(resource)
            DownloadAction(project).apply {
                src("$api$resource")
                dest(downloadDir.resolve(fileName))
                overwrite(false)
                retries(2)
                tempAndMove(true)
            }.execute(true)
        }.forEach { it.get() }
    }
}
