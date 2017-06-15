package com.meituan.robust.autopatch

import com.meituan.robust.Constants
import com.meituan.robust.utils.JavaUtils

class ReadXML {
    private static robust;

    public static void readXMl(String path) {
        robust = new XmlSlurper().parse(new File("${path}${File.separator}${Constants.ROBUST_XML}"))

        //读取配置的补丁包名
        if (robust.patchPackname.name.text() != null && !"".equals(robust.patchPackname.name.text()))
            Config.patchPackageName = robust.patchPackname.name.text()

        Config.isManual = robust.switch.manual != null && "true".equals(String.valueOf(robust.switch.manual.text()))
        //读取mapping文件
        if (robust.switch.proguard.text() != null && !"".equals(robust.switch.proguard.text()))
            Config.supportProGuard = Boolean.valueOf(robust.switch.proguard.text()).booleanValue();

        if (robust.mappingFile.name.text() != null && !"".equals(robust.mappingFile.name.text())) {
            Config.mappingFilePath = robust.mappingFile.name.text()
        } else {
            Config.mappingFilePath = "${path}${Constants.DEFAULT_MAPPING_FILE}"
        }

        if (Config.supportProGuard && (Config.mappingFilePath == null || "".equals(Config.mappingFilePath) || !(new File(Config.mappingFilePath)).exists())) {
            throw new RuntimeException("Not found ${Config.mappingFilePath}, please put it on your project's robust dir or change your robust.xml !");
        }

        for (name in robust.patchPackClass.name) {
            Config.modifiedClassNameList.add(String.valueOf(name.text()).trim());
        }

        for (name in robust.patchMethodSignure.name) {
            if (!JavaUtils.isMethodSignureContainPatchClassName(String.valueOf(name.text()), Config.modifiedClassNameList))
                throw new RuntimeException("input patchMethodSignure in robust.xml error,there are more than one patch classes,you need to config full class name and java method sigure");
            Config.patchMethodSignureSet.add(String.valueOf(name.text()).trim());
        }

        for (name in robust.packname.name) {
            Config.hotfixPackageList.add(name.text());
        }
        for (name in robust.newlyAddClass.name) {
            Config.newlyAddedClassNameList.add(name.text());
        }
        if (robust.switch.catchReflectException.text() != null && !"".equals(robust.switch.catchReflectException.text()))
            Config.catchReflectException = Boolean.valueOf(robust.switch.catchReflectException.text()).booleanValue();

        if (robust.switch.patchLog.text() != null && !"".equals(robust.switch.patchLog.text()))
            Constants.isLogging = Boolean.valueOf(robust.switch.patchLog.text()).booleanValue();

        for (name in robust.noNeedReflectClass.name) {
            Config.noNeedReflectClassSet.add(name.text());
        }

        if (robust.switch.fixResources.text() != null && !"".equals(robust.switch.fixResources.text())) {
            Config.isResourceFix = Boolean.valueOf(robust.switch.fixResources.text()).booleanValue();
        }

        if (robust.switch.debug.text() != null && !"".equals(robust.switch.debug.text())) {
            Config.debug = Boolean.valueOf(robust.switch.debug.text()).booleanValue();
        }

        if (robust.resourceFix.RDotTxtFile.name.text() != null && !"".equals(robust.resourceFix.RDotTxtFile.name.text())) {
            Config.RDotTxtFilePath = robust.resourceFix.RDotTxtFile.name.text()
        } else {
            Config.RDotTxtFilePath = "${path}${Constants.DEFAULT_R_DOT_TXT_FILE}"
        }

        if (robust.resourceFix.oldApkPath.name.text() != null && !"".equals(robust.resourceFix.oldApkPath.name.text())) {
            Config.oldApkPath = robust.resourceFix.oldApkPath.name.text()
        } else {
            Config.oldApkPath = "${path}${Constants.DEFAULT_OLD_APK_PATH}"
        }

        if (robust.resourceFix.newApkPath.name.text() != null && !"".equals(robust.resourceFix.newApkPath.name.text())) {
            Config.newApkPath = robust.resourceFix.newApkPath.name.text()
        } else {
            Config.newApkPath = "${path}${Constants.DEFAULT_NEW_APK_PATH}"
            if (!new File(Config.newApkPath).exists()){
                Config.newApkPath = ""
            }
        }

        for (name in robust.resourceFix.assets.include.name) {
            Config.assetsInclude.add(name.text());
        }

        for (name in robust.resourceFix.assets.exclude.name) {
            Config.assetsExclude.add(name.text());
        }

        for (name in robust.resourceFix.res.include.name) {
            Config.resInclude.add(name.text());
        }

        for (name in robust.resourceFix.res.exclude.name) {
            Config.resExclude.add(name.text());
        }

        for (name in robust.resourceFix.lib.include.name) {
            Config.libInclude.add(name.text());
        }

        for (name in robust.resourceFix.lib.exclude.name) {
            Config.libExclude.add(name.text());
        }

    }
}
