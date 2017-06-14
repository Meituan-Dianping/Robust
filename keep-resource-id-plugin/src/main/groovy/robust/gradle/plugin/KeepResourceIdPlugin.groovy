package robust.gradle.plugin

import com.meituan.robust.Constants
import groovy.util.slurpersupport.GPathResult
import org.gradle.api.Plugin
import org.gradle.api.Project

public class KeepResourceIdPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.afterEvaluate {
            project.android.applicationVariants.each { variant ->
                def variantOutput = variant.outputs.first()
                //def variantName = variant.name.capitalize()
                String RDotTxtPath
                String path = project.projectDir.path;
                GPathResult robust = new XmlSlurper().parse(new File("${path}${File.separator}${Constants.ROBUST_XML}"))

                if (robust.resourceFix.RDotTxtFile.name.text() != null && !"".equals(robust.resourceFix.RDotTxtFile.name.text())) {
                    RDotTxtPath = robust.resourceFix.RDotTxtFile.name.text()
                } else {
                    RDotTxtPath = "${path}${Constants.DEFAULT_R_DOT_TXT_FILE}"
                }

                //keep resource id
                //def processResourcesTask = project.tasks.findByName("process${variantName}Resources")
                def resDir = variantOutput.processResources.resDir
                String resDirStr = resDir.absolutePath
                variantOutput.processResources.doFirst{
                //processResourcesTask.doFirst{
                    new KeepResourceId(RDotTxtPath, resDirStr).execute()
                    project.logger.quiet("robust: keep resource id applied!")
                }
            }
        }
    }
}