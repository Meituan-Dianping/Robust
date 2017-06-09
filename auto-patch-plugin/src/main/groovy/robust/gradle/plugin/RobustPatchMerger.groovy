package robust.gradle.plugin

import com.meituan.robust.Constants
import com.meituan.robust.autopatch.Config
import com.meituan.robust.common.FileUtil
import com.meituan.robust.patch.resources.config.RobustResourceConfig

import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
/**
 * Created by hedex on 17/2/21.
 */
public class RobustPatchMerger {
    private RobustPatchMerger() {
    }

    def static File getPatchDexPart() {
        return new File(Config.robustGenerateDirectory, Constants.PATACH_APK_NAME)
    }

    def static File getPatchResourcePart() {
        return new File(Config.robustGenerateDirectory, RobustResourceConfig.PATCH_RESOURCES_APK)
    }

    def static mergeDexPartAndResourcePart() {
        // name is patch.apk
        File dexPartFile = getPatchDexPart()

        // name is patch_resources.apk
        File resourcePartFile = getPatchResourcePart()

        boolean hasDex = Config.patchHasDex && dexPartFile.exists() && dexPartFile.length() > 0;
        boolean hasResource = Config.patchHasResource && resourcePartFile.exists() && resourcePartFile.length() > 0;

        if (hasDex & hasResource) {
            File mergeFile = new File(dexPartFile.absolutePath + "merge_temp")

            ZipOutputStream mergeZipOutputStream  = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(mergeFile)));

            //merge
            ZipFile dexPartZipFile = new ZipFile(dexPartFile)

            ZipEntry classesDexEntry = dexPartZipFile.getEntry("classes.dex")
            FileUtil.addZipEntry(mergeZipOutputStream, classesDexEntry, dexPartZipFile.getInputStream(classesDexEntry))

            //copy ap file
            ZipFile resourcePartZipFile = new ZipFile(resourcePartFile)
            final Enumeration<? extends ZipEntry> entries = resourcePartZipFile.entries();
            while (entries.hasMoreElements()) {
                //ZipEntry zipEntry = entries.nextElement();//保守
                ZipEntry zipEntry = new ZipEntry(entries.nextElement().name);
                if (null != zipEntry ) {
                    FileUtil.addZipEntry(mergeZipOutputStream, zipEntry, resourcePartZipFile.getInputStream(zipEntry))
                }
            }

            dexPartZipFile.close()
            resourcePartZipFile.close()
            if (null != mergeZipOutputStream) {
                mergeZipOutputStream.close()
            }
            if (dexPartFile.exists()){
                dexPartFile.delete()
            }
            mergeFile.renameTo(dexPartFile.absolutePath)
        } else {
            if (hasDex) {
            }
            if (hasResource) {
                resourcePartFile.renameTo(dexPartFile.absolutePath)
            }
        }

        //todo deleteTmpFiles when online
//        deleteTmpFiles()
    }


    def deleteTmpFiles() {
        File diretcory = new File(Config.robustGenerateDirectory);
        if (!diretcory.isDirectory()) {
            throw new RuntimeException("patch directry " + Config.robustGenerateDirectory + " dones not exist");
        } else {
            diretcory.listFiles(new FilenameFilter() {
                @Override
                boolean accept(File file, String s) {
                    return !(Constants.PATACH_APK_NAME.equals(s))
                }
            }).each {
                if (it.isDirectory()) {
                    it.deleteDir()
                } else {
                    it.delete()
                }
            }
        }
    }

}