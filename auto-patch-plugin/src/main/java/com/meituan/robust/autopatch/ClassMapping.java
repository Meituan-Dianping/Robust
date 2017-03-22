package com.meituan.robust.autopatch;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mivanzhang on 16/7/29.
 * ClassMapping stores mapping info read from mapping.txt
 */
public class ClassMapping {
    //method 存储的信息有：返回值，方法名，参数列表，混淆后的名字
    //字段 存储的信息有：字段名，混淆后的名字

    private String className;
    private String valueName;
    private Map<String, String> memberMapping = new HashMap<>();

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getValueName() {
        return valueName;
    }

    public void setValueName(String valueName) {
        this.valueName = valueName;
    }

    public Map<String, String> getMemberMapping() {
        return memberMapping;
    }

    public void setMemberMapping(Map<String, String> memberMapping) {
        this.memberMapping = memberMapping;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassMapping)) return false;

        ClassMapping classMapping = (ClassMapping) o;

        return getClassName() != null ? getClassName().equals(classMapping.getClassName()) : classMapping.getClassName() == null;

    }

    @Override
    public int hashCode() {
        return getClassName() != null ? getClassName().hashCode() : 0;
    }
}
