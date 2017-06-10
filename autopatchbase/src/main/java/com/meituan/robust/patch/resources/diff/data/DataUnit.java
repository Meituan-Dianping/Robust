package com.meituan.robust.patch.resources.diff.data;

/**
 *
 */
public class DataUnit {

    public String name;
    public String oldMd5;
    public String newMd5;
    public String diffMd5;
    public long newCrc;

    public DataUnit() {
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(name);
        stringBuffer.append(",");
        stringBuffer.append(oldMd5);
        stringBuffer.append(",");
        stringBuffer.append(newMd5);
        stringBuffer.append(",");
        stringBuffer.append(diffMd5);
        stringBuffer.append(",");
        stringBuffer.append(newCrc);
        return stringBuffer.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DataUnit){
            DataUnit diffData = (DataUnit)obj;
            if (this.name.equals(diffData.name)){
                return true;
            }
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}