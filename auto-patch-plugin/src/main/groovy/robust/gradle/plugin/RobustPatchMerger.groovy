package robust.gradle.plugin

import com.meituan.robust.Constants
import com.meituan.robust.autopatch.Config
import com.meituan.robust.common.ResourceConstant
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
        boolean hasResource = Config.patchHasResource resourcePartFile.exists() && resourcePartFile.length() > 0;


        if (hasDex & hasResource) {
            //merge
            ZipFile dexPartZipFile = new ZipFile(dexPartFile)

            ZipEntry classesDexEntry = dexPartZipFile.getEntry("classes.dex")

            ZipOutputStream resourcePartZipOutputStream = null;
            try {
                resourcePartZipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(resourcePartFile)));
                zipEntry2ZipOutputStream(dexPartZipFile, classesDexEntry, resourcePartZipOutputStream);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            } finally {
                if (null != resourcePartZipOutputStream) {
                    resourcePartZipOutputStream.close()
                }
                dexPartZipFile.close()
            }

            resourcePartFile.renameTo(dexPartFile.absolutePath)
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

    private
    static void zipEntry2ZipOutputStream(ZipFile zipFile, ZipEntry zipEntry, ZipOutputStream outputStream) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = zipFile.getInputStream(zipEntry);
            outputStream.putNextEntry(new ZipEntry(zipEntry));
            byte[] buffer = new byte[ResourceConstant.BUFFER_SIZE];

            for (int length = inputStream.read(buffer); length != -1; length = inputStream.read(buffer)) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.closeEntry();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }
}