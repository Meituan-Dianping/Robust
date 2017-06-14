package robust.gradle.plugin

import com.meituan.robust.Constants
import com.meituan.robust.autopatch.Config
import com.meituan.robust.common.ResourceConstant
import com.meituan.robust.patch.resources.config.RobustResourceConfig
import com.meituan.robust.patch.resources.config.RobustXmlResourceInfo
import com.meituan.robust.patch.resources.diff.APKDiffUtils
import groovy.util.slurpersupport.GPathResult
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
                File oldApkFile = new File(Config.oldApkPath)
                if (!oldApkFile.exists()) {
                    throw new RuntimeException("robust: old apk patch is not exists !!! path is : " + Config.oldApkPath)
                }
            }

            project.android.applicationVariants.each { variant ->
                //=======keep resource id start========
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
                //=======keep resource id end========

                RobustXmlResourceInfo xmlResourceInfo = new RobustXmlResourceInfo()


                xmlResourceInfo.assetsExcludeStrings = Config.assetsExclude
                //排除掉robust自身产生的一些文件
                xmlResourceInfo.assetsExcludeStrings.add(Constants.ROBUST_APK_HASH_FILE_NAME);
                xmlResourceInfo.assetsExcludeStrings.add(ResourceConstant.ROBUST_RESOURCES_DIFF);

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

                    def assembleTask = project.tasks.findByName("assemble${variant.name.capitalize()}")
                    assembleTask.doLast {
                        //diff apk
                        project.logger.debug("robust: resource fix start")
//                        try {
                            execute(xmlResourceInfo)
//                        } catch (Exception e) {
//                            project.logger.error("robust: An Error Has Occurred \n"
//                                    + "robust error: " + e.toString())
//                        }
                        project.logger.debug("robust: resource fix end")
                        Config.patchHasResource = true

                        RobustPatchMerger.mergeDexPartAndResourcePart()
                        throw new RuntimeException("*** auto patch end successfully! ***, patch path is :" + new File(Config.robustGenerateDirectory, Constants.PATACH_APK_NAME).toPath())
                    }

                } else {
                    xmlResourceInfo.newApkPath = configNewApkPath

                    def transformClassesWithRobustTask = project.tasks.findByName("transformClassesWithAutoPatchTransformFor${variant.name.capitalize()}")
                    transformClassesWithRobustTask.doLast {
                        //diff apk
                        project.logger.debug("robust: resource fix start")
//                        try {
                            execute(xmlResourceInfo)
//                        } catch (Exception e) {
//                            project.logger.debug("robust: An Error Has Occurred")
//                        }
                        project.logger.debug("robust: resource fix end")
                        Config.patchHasResource = true

                        RobustPatchMerger.mergeDexPartAndResourcePart()

                        throw new RuntimeException("*** auto patch end successfully! ***, patch path is :" + new File(Config.robustGenerateDirectory, Constants.PATACH_APK_NAME).toPath())
                    }
                }
            }
        } else {
            project.logger.debug("robust: resource fix switch is off")
        }
    }
}