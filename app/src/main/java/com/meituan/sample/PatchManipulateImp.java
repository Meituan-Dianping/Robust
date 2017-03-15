package com.meituan.sample;

import android.content.Context;
import android.os.Environment;

import com.meituan.robust.Patch;
import com.meituan.robust.PatchManipulate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mivanzhang on 17/2/27.
 */

public class PatchManipulateImp extends PatchManipulate {
    @Override
    protected List<Patch> fetchPatchList(Context context) {
        //将app自己的robustApkHash上报给服务端，服务端根据robustApkHash来区分每一次apk build来给app下发补丁
        //String robustApkHash = RobustApkHashUtils.readRobustApkHash(context);
        Patch patch = new Patch();
        patch.setName("123");
        patch.setLocalPath(Environment.getExternalStorageDirectory().getPath()+ File.separator+"robust"+File.separator + "patch.dex");
        patch.setTempPath(Environment.getExternalStorageDirectory().getPath()+ File.separator+"robust"+File.separator + "patch");
        patch.setPatchesInfoImplClassFullName("com.meituan.robust.patch.PatchesInfoImpl");
        List  patches = new ArrayList<Patch>();
        patches.add(patch);
        return patches;
    }

    @Override
    protected boolean verifyPatch(Context context, Patch patch) {
        return true;
    }

    @Override
    protected boolean ensurePatchExist(Patch patch) {
        return true;
    }
}
