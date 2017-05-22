package robust.gradle.plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;

/**
 * Created by zhangmeng on 2017/5/10.
 */

public abstract class InsertcodeStrategy {
    protected  List<String> hotfixPackageList = new ArrayList<>();
    protected  List<String> hotfixMethodList = new ArrayList<>();
    protected  List<String> exceptPackageList = new ArrayList<>();
    protected  List<String> exceptMethodList = new ArrayList<>();
    protected  boolean isHotfixMethodLevel = false;
    protected  boolean isExceptMethodLevel = false;
    protected AtomicInteger insertMethodCount = new AtomicInteger(0);
    public HashMap<String, Integer> methodMap = new HashMap();

    public InsertcodeStrategy(List<String> hotfixPackageList, List<String> hotfixMethodList, List<String> exceptPackageList, List<String> exceptMethodList, boolean isHotfixMethodLevel, boolean isExceptMethodLevel) {
        this.hotfixPackageList = hotfixPackageList;
        this.hotfixMethodList = hotfixMethodList;
        this.exceptPackageList = exceptPackageList;
        this.exceptMethodList = exceptMethodList;
        this.isHotfixMethodLevel = isHotfixMethodLevel;
        this.isExceptMethodLevel = isExceptMethodLevel;
        insertMethodCount.set(0);
    }

    protected abstract void insertCode(List<CtClass> box, File jarFile) throws CannotCompileException, IOException, NotFoundException;
    protected  boolean isNeedInsertClass(String className) {

        //这样子可以在需要埋点的剔除指定的类
        for (String exceptName : exceptPackageList) {
            if (className.startsWith(exceptName)) {
                return false;
            }
        }
        for (String name : hotfixPackageList) {
            if (className.startsWith(name)) {
                return true;
            }
        }
        return false;
    }

    protected void zipFile(byte[] classBytesArray, ZipOutputStream zos, String entryName){
        try {
            ZipEntry entry = new ZipEntry(entryName);
            zos.putNextEntry(entry);
            zos.write(classBytesArray, 0, classBytesArray.length);
            zos.closeEntry();
            zos.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
