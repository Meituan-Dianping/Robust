package robust.gradle.plugin

import com.meituan.robust.tools.aapt.AaptResourceCollector
import com.meituan.robust.tools.aapt.AaptUtil
import com.meituan.robust.tools.aapt.PatchUtil
import com.meituan.robust.tools.aapt.RDotTxtEntry
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
/**
 * Created by hedex on 17/2/21.
 */
class KeepResourceIdTask extends DefaultTask {

    @Input
    String resDir
    @Input
    String RDotTxtPath

    KeepResourceIdTask() {
    }

    @TaskAction
    def keepResourceId() {
        if (null == RDotTxtPath || "".equals(RDotTxtPath.trim())) {
            File file = new File(RDotTxtPath)
            if (!file.exists() || file.length() == 0) {
                project.logger.error("apply R.txt file ${RDotTxtPath} failed")
                return
            }
        }
        String idsXml = resDir + File.separator + "values" + File.separator + "ids.xml"
        String publicXml = resDir + File.separator + "values" + File.separator + "public.xml"
        File oldIdsXmlFile = new File(idsXml)
        if (oldIdsXmlFile.exists()) {
            oldIdsXmlFile.delete()
        }

        File oldPublicXml = new File(publicXml)
        if (oldPublicXml.exists()) {
            oldPublicXml.delete()
        }

        List<String> resourceDirectoryList = new ArrayList<String>()
        resourceDirectoryList.add(resDir)

        project.logger.debug("idsXml path:" + idsXml)
        project.logger.debug("publicXml path:" + publicXml)
        Map<RDotTxtEntry.RType, Set<RDotTxtEntry>> rTypeResourceMap = PatchUtil.readRTxt(RDotTxtPath)

        // use aapt util to parse rTypeResourceMap(R.txt),and get the public.xml and ids.xml
        AaptResourceCollector aaptResourceCollector = AaptUtil.collectResource(resourceDirectoryList, rTypeResourceMap)
        PatchUtil.generatePublicResourceXml(aaptResourceCollector, idsXml, publicXml)
    }
}

