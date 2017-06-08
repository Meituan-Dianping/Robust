package com.meituan.robust.patch.resources.diff.data;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by hedingxu on 17/5/31.
 */

public class BaseDiffData {
    public String diffTypeName;

    public BaseDiffData() {
        diffTypeName = "base";
    }

    // common
    public Set<DataUnit> addSet = new HashSet<>();
    public Set<DataUnit> modSet = new HashSet<>();
    public Set<DataUnit> delSet = new HashSet<>();

    // diff
    public Set<DataUnit> diffModSet = new HashSet<>();


    public void clear() {
        // common
        addSet.clear();
        modSet.clear();
        delSet.clear();

        // diff
        diffModSet.clear();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DiffTypeName :" + diffTypeName);
        //// TODO: 17/5/31
        return stringBuilder.toString();
    }

    public boolean isEmpty(){
        return addSet.isEmpty() && modSet.isEmpty() && delSet.isEmpty() && diffModSet.isEmpty();
    }

}
