package robust.gradle.plugin

import com.meituan.robust.tools.aapt.AaptResourceCollector
import com.meituan.robust.tools.aapt.AaptUtil
import com.meituan.robust.tools.aapt.PatchUtil
import com.meituan.robust.tools.aapt.RDotTxtEntry
/**
 * Created by hedex on 17/2/21.
 *
 * task:mergeReleaseResources will clean variantOutput.processResources.resDir
 * task:processReleaseResources will use ids.xml and public.xml
 * case:
 * app/build/intermediates/res/merged/release/values/ids.xml
 * app/build/intermediates/res/merged/release/values/public.xml
 */
class KeepResourceId {
    String resDir = ""
    String RDotTxtPath = ""

    KeepResourceId(String RDotTxtPathStr, String resDirStr) {
        resDir = resDirStr
        RDotTxtPath = RDotTxtPathStr
    }

    void execute() {
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

        Map<RDotTxtEntry.RType, Set<RDotTxtEntry>> rTypeResourceMap = PatchUtil.readRTxt(RDotTxtPath)

        // use aapt util to parse rTypeResourceMap(R.txt),and get the public.xml and ids.xml
        AaptResourceCollector aaptResourceCollector = AaptUtil.collectResource(resourceDirectoryList, rTypeResourceMap)
        PatchUtil.generatePublicResourceXml(aaptResourceCollector, idsXml, publicXml)
    }
}

