package robust.gradle.plugin

import com.meituan.robust.patch.resources.config.RobustXmlResourceInfo
import org.gradle.api.Plugin
import org.gradle.api.Project
/**
 * Created by hedex on 17/2/21.
 */
public class RobustPatchPlugin implements Plugin<Project> {
    public static final String ROBUST_BUILD_OUTPUTS = "build" + File.separator + "outputs" + File.separator + "robust" + File.separator
    public final static String ROBUST_XML = "robust.xml"
    File outputFolderTest
    String resRDotTxtPath
    String originApkPath
    boolean resourcesEnable

    @Override
    public void apply(Project project) {
        def android = project.extensions.android
        outputFolderTest = new File(project.buildDir, "robust-test");

//        if (outputFolderTest.exists()) {
//            outputFolderTest.delete();
//        }

//        RDotTxtPath = "~/workspaceForMyself/robust/app/robust/R.txt"
//        "~/workspaceForMyself/robust/app/robust/old.apk"
        //read robust.xml
        def robust = new XmlSlurper().parse(new File(project.projectDir, ROBUST_XML))
        def resources = robust.resources

        if (null != resources.enable && "true".equals(String.valueOf(resources.enable.text()))) {
            resourcesEnable = true;
        }

        if (!resourcesEnable) {
            return
        }

        def tempRTxtPath = res.rTxtPath.name.text()
        if (tempRTxtPath != null && !"".equals(tempRTxtPath)) {
            resRDotTxtPath = tempRTxtPath
        }

        def tempOriginApkPath = resources.originApkPath.name.text()
        if (tempOriginApkPath != null && !"".equals(tempOriginApkPath)) {
            originApkPath = tempOriginApkPath
        }

        for (name in resources.soFilter.name) {
            lib_pattern.add(name.text());
        }

        for (name in resources.filter.name) {
            res_pattern.add(name.text());
        }

        for (name in resources.ignore.name) {
            res_ignoreChange.add(name.text());
        }

        if (null != resources.diffFileSizeLimit.name) {
            res_largeModSize = Integer.valueOf(resources.diffFileSizeLimit.name.text())
        }

        project.afterEvaluate {
            if (null == resRDotTxtPath || "".equals(resRDotTxtPath)){
                File file = new File(resRDotTxtPath);
                if (!file.exists() || file.length() == 0){

                    return ;
                }
            }

            android.applicationVariants.all { variant ->

                def variantOutput = variant.outputs.first()
                def variantName = variant.name.capitalize()

                //resource id
                RobustKeepResourceIdTask keepResourceIdTask = project.tasks.create("robustKeep${variantName}ResourceId", RobustKeepResourceIdTask)
                keepResourceIdTask.resourcesDir = variantOutput.processResources.resourcesDir
                keepResourceIdTask.RDotTxtPath = resRDotTxtPath
                variantOutput.processResources.dependsOn keepResourceIdTask

                RobustResourcePatchTask resourcesPatch = new RobustResourcePatchTask()

                RobustXmlResourceInfo xmlResourceInfo = new RobustXmlResourceInfo()

                //todo
                xmlResourceInfo.oldApkPath = originApkPath
                xmlResourceInfo.assetsExcludeStrings = assetsExcludeStrings
                xmlResourceInfo.assetsIncludeStrings = assetsIncludeStrings
                xmlResourceInfo.resExcludeStrings = resExcludeStrings
                xmlResourceInfo.resIncludeStrings = resIncludeStrings
                xmlResourceInfo.libExcludeStrings = libExcludeStrings
                xmlResourceInfo.libExcludeStrings = libExcludeStrings

                if (bigFileSizeAtLeast > 0){
                    xmlResourceInfo.bigFileSizeAtLeast = bigFileSizeAtLeast
                }

                variant.outputs.each { output ->
                    resourcesPatch.newApkPath = output.outputFile
                    resourcesPatch.outputFolder = project.buildDir.absolutePath + File.separator + "outputs" + File.separator + "robust" + File.separator + variant.dirName
                }

                def assembleTask = project.tasks.findByName("assemble${variant.name.capitalize()}")
                assembleTask.doLast {
                    resourcesPatch.execute(xmlResourceInfo)
                }

            }
        }
    }

}