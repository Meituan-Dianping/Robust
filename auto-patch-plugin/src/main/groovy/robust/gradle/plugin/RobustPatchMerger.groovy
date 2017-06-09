package robust.gradle.plugin

import com.meituan.robust.Constants
import com.meituan.robust.autopatch.Config
/**
 * Created by hedex on 17/2/21.
 */
public class RobustPatchMerger {
    private RobustPatchMerger() {
    }

    def File getPatchDexPart(){
        return new File(Config.robustGenerateDirectory, Constants.PATACH_APK_NAME)
    }

    def File getPatchResourcePart(){
        return new File(Config.robustGenerateDirectory, Constants.PATACH_APK_NAME)
    }

    def mergeDexPartAndResourcePart(){
        File dexPart = getPatchDexPart()
        File resourcePart = getPatchResourcePart()
        extractDex2Resource()

        dexPart.renameTo()

//        FileUtil.

        deleteTmpFiles()

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