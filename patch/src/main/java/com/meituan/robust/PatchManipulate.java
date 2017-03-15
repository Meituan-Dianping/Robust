package com.meituan.robust;

import android.content.Context;

import java.util.List;

/**
 * Created by hedex on 16/6/20.
 */
public abstract class PatchManipulate {
    /**
     * 获取补丁列表
     *
     * @param context
     * @return 相应的补丁列表
     */
    protected abstract List<Patch> fetchPatchList(Context context);

    /**
     * 验证补丁文件md5是否一致
     * 如果不存在，则动态下载
     *
     * @param context
     * @param patch
     * @return 校验结果
     */
    protected abstract boolean verifyPatch(Context context, Patch patch);

    /**
     * 努力确保补丁文件存在，验证md5是否一致。
     * 如果不存在，则动态下载
     *
     * @param patch
     * @return 是否存在
     */
    protected abstract boolean ensurePatchExist(Patch patch);
}
