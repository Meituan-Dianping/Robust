package robust.gradle.plugin.config


class RobustExtension {
    public boolean turnOnDebug = false;
    public String patchPkg;
    public List<String> hotfixPkgs;
    public List<String> exceptPkgs;
    public List<String> noNeedReflectClass;
}