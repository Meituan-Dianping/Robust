package robust.gradle.plugin.config;

/**
 * **************************************
 * 项目名称:Robust
 *
 * @Author yangjilai* 邮箱：cangming@126.com
 * 创建时间: 2020-03-29     19:08
 * 用途：
 * **************************************
 */
class GlobalConfig {
    static boolean turnOnDebug = false;
    static String patchPkg;
    static List<String> hotfixPkgs;
    static List<String> exceptPkgs;
    static List<String> noNeedReflectClass;
}
