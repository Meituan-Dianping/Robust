package robust.gradle.plugin

import com.meituan.robust.patch.resources.config.RobustResourceConfig
import com.meituan.robust.patch.resources.config.RobustXmlResourceInfo
import com.meituan.robust.patch.resources.diff.APKDiffUtils

/**
 * Created by hedex on 17/2/21.
 */
public class RobustResourcePatchTask {
    public RobustResourcePatchTask() {
    }

    def execute(RobustXmlResourceInfo patchInfo) {
        RobustResourceConfig config = new RobustResourceConfig(patchInfo);
        APKDiffUtils.execute(config);
    }
}