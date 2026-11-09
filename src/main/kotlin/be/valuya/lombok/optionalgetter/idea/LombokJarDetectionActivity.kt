package be.valuya.lombok.optionalgetter.idea

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.registry.Registry
import com.intellij.psi.PsiManager

/**
 * Switches the bundled Lombok plugin on for the overlay, so it generates `@Getter`, `@Builder`, … itself.
 *
 * The bundled plugin recognises a project as a Lombok project by the Maven coordinate
 * `org.projectlombok:lombok`. The overlay ships as `io.github.valuya:lombok-optionalgetter-overlay`, so the
 * bundled plugin turns itself off and every Lombok member, not only `getXxxOptional()`, is unresolved in the
 * editor. Its fallback, which finds any library holding the `lombok` package, sits behind the registry key
 * below and is off by default. This activity turns the key on once; the setting persists.
 *
 * Registered only when the bundled Lombok plugin is present, since that plugin declares the key.
 */
class LombokJarDetectionActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        val slowJarDetection = Registry.get(SLOW_JAR_DETECTION_KEY)
        if (slowJarDetection.asBoolean()) return
        slowJarDetection.setValue(true)
        LOG.info("Enabled $SLOW_JAR_DETECTION_KEY so the bundled Lombok plugin recognises the overlay jar")
        refreshOpenProjects()
    }

    /** Highlighting may already have run with Lombok off: drop what it cached and highlight again. */
    private suspend fun refreshOpenProjects() {
        val openProjects = ProjectManager.getInstance().openProjects
        for (openProject in openProjects) {
            if (openProject.isDisposed) continue
            writeAction {
                PsiManager.getInstance(openProject).dropPsiCaches()
            }
            DaemonCodeAnalyzer.getInstance(openProject).restart()
        }
    }

    companion object {
        const val SLOW_JAR_DETECTION_KEY = "lombok.slow.jar.detection.enabled"
        private val LOG = logger<LombokJarDetectionActivity>()
    }
}
