package robust.gradle.plugin

import com.meituan.robust.Constants
import groovy.util.slurpersupport.GPathResult
import org.gradle.api.Plugin
import org.gradle.api.Project

public class KeepResourceIdPlugin implements Plugin<Project> {

    String RDotTxtPath
    @Override
    void apply(Project project) {
        String path = project.projectDir.path;
        GPathResult robust = new XmlSlurper().parse(new File("${path}${File.separator}${Constants.ROBUST_XML}"))

        if (robust.resourceFix.RDotTxtFile.name.text() != null && !"".equals(robust.resourceFix.RDotTxtFile.name.text())) {
            RDotTxtPath = robust.resourceFix.RDotTxtFile.name.text()
        } else {
            RDotTxtPath = "${path}${Constants.DEFAULT_R_DOT_TXT_FILE}"
        }

        project.afterEvaluate {
            applyTask(project)
        }
    }

    private void applyTask(Project project) {
        project.android.applicationVariants.each { variant ->
            def variantOutput = variant.outputs.first()
            def variantName = variant.name.capitalize()
            //keep resource id
            KeepResourceIdTask keepResourceIdTask = project.tasks.create("keep${variantName}ResourceId", KeepResourceIdTask)
            keepResourceIdTask.resDir = variantOutput.processResources.resDir
            keepResourceIdTask.RDotTxtPath = RDotTxtPath
            variantOutput.processResources.dependsOn keepResourceIdTask
        }
    }

}