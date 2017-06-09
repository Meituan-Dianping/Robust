package robust.gradle.plugin

import com.meituan.robust.autopatch.Config
import com.meituan.robust.patch.resources.config.RobustResourceConfig
import com.meituan.robust.patch.resources.config.RobustXmlResourceInfo
import com.meituan.robust.patch.resources.diff.APKDiffUtils
import org.gradle.api.Action
import org.gradle.api.Project

/**
 * Created by hedex on 17/2/21.
 */
public class RobustResourcePatchAction implements Action<Project> {
    public RobustResourcePatchAction() {
    }

    def execute(RobustXmlResourceInfo robustXmlResourceInfo) {
        RobustResourceConfig config = new RobustResourceConfig(robustXmlResourceInfo);
        APKDiffUtils.execute(config);
    }

    @Override
    void execute(Project project) {
        if (Config.isResourceFix) {

            project.logger.debug("robust: resource fix switch is on")

            if (null == Config.oldApkPath || Config.oldApkPath.trim() == "") {

                project.logger.error("robust: old apk patch is blank!!!!")
                throw new RuntimeException("robust: old apk patch is blank !!!!")
            } else {
                File oldApkFile = new File(Config.oldApkPath).exists()
                if (!oldApkFile.exists()) {
                    throw new RuntimeException("robust: old apk patch is not exists !!! path is : " + Config.oldApkPath)
                }
            }

            project.android.applicationVariants.each { variant ->
                def variantOutput = variant.outputs.first()
                def variantName = variant.name.capitalize()

                //keep resource id
                RobustKeepResourceIdTask keepResourceIdTask = project.tasks.create("robustKeep${variantName}ResourceId", RobustKeepResourceIdTask)
                keepResourceIdTask.resourcesDir = variantOutput.processResources.resourcesDir
                keepResourceIdTask.RDotTxtPath = resRDotTxtPath
                variantOutput.processResources.dependsOn keepResourceIdTask

                RobustXmlResourceInfo xmlResourceInfo = new RobustXmlResourceInfo()


                xmlResourceInfo.assetsExcludeStrings = Config.assetsExclude
                xmlResourceInfo.assetsIncludeStrings = Config.assetsInclude
                xmlResourceInfo.resExcludeStrings = Config.resExclude
                xmlResourceInfo.resIncludeStrings = Config.resInclude
                xmlResourceInfo.libExcludeStrings = Config.libExclude
                xmlResourceInfo.libIncludeStrings = Config.libInclude

                int bigFileSizeAtLeast = Config.bigFileSizeAtLeast
                if (bigFileSizeAtLeast > 0) {
                    xmlResourceInfo.bigFileSizeAtLeast = bigFileSizeAtLeast
                }

                xmlResourceInfo.oldApkPath = Config.oldApkPath

                String configNewApkPath = Config.newApkPath
                if (null == configNewApkPath || configNewApkPath.trim() == "") {
                    variant.outputs.each { output ->
                        xmlResourceInfo.newApkPath = output.outputFile
                    }
                } else {
                    xmlResourceInfo.newApkPath = configNewApkPath
                }

                def assembleTask = project.tasks.findByName("assemble${variant.name.capitalize()}")
                assembleTask.doLast {
                    //diff apk
                    project.logger.debug("robust: resource fix start")
                    try {
                        execute(xmlResourceInfo)
                    } catch (Exception e) {
                        throw RuntimeException(e)
                    }
                    project.logger.debug("robust: resource fix end")
                    Config.patchHasResource = true
                }

            }
        } else {
            project.logger.debug("robust: resource fix switch is off")
        }
    }
}