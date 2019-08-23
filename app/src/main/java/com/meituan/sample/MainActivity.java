package com.meituan.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.meituan.robust.PatchExecutor;
import com.meituan.robust.RollbackListener;
import com.meituan.robust.RollbackManager;

import java.util.Map;

/**
 * For users of Robust you may only to use MainActivity or SecondActivity,other classes are used for test.<br>
 * <br>
 * If you just want to use Robust ,we recommend you just focus on MainActivity SecondActivity and PatchManipulateImp.Especially three buttons in MainActivity<br>
 * <br>
 * in the MainActivity have three buttons; "SHOW TEXT " Button will change the text in the MainActivity,you can patch the show text.<br>
 * <br>
 * "PATCH" button will load the patch ,the patch path can be configured in PatchManipulateImp.<br>
 * <br>
 * "JUMP_SECOND_ACTIVITY" button will jump to the second ACTIVITY,so you can patch a Activity.<br>
 * <br>
 * Attention to this ,We recommend that one patch is just for one built apk ,because every  built apk has its unique mapping.txt and resource id<br>
 *
 * @author mivanzhang
 */

public class MainActivity extends AppCompatActivity {

    TextView textView;
    Button button;
    private static final String TAG = "MainActivity";
    private Map<String, Boolean> rollBacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.textView);
        Button patch = (Button) findViewById(R.id.patch);
        //beigin to patch
        patch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isGrantSDCardReadPermission()) {
                    runRobust();
                } else {
                    requestPermission();
                }
            }
        });

        findViewById(R.id.jump_second_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                startActivity(intent);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "arrived in ", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private boolean isGrantSDCardReadPermission() {
        return PermissionUtils.isGrantSDCardReadPermission(this);
    }

    private void requestPermission() {
        PermissionUtils.requestSDCardReadPermission(this, REQUEST_CODE_SDCARD_READ);
    }

    private static final int REQUEST_CODE_SDCARD_READ = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_SDCARD_READ:
                handlePermissionResult();
                break;

            default:
                break;
        }
    }

    private void handlePermissionResult() {
        if (isGrantSDCardReadPermission()) {
            runRobust();
        } else {
            Toast.makeText(this, "failure because without sd card read permission", Toast.LENGTH_SHORT).show();
        }

    }

    private void runRobust() {
        initRollbackListener();
        new PatchExecutor(getApplicationContext(), new PatchManipulateImp(), new RobustCallBackSample()).start();
    }

    private void initRollbackListener() {
        //TODO: 初始化时从本地取出，反序列化为map
        rollBacks = new ArrayMap<>();

        RollbackManager.getInstance().setRollbackListener(new RollbackListener() {
            @Override
            public void onRollback(String methodsId, String methodLongName, Throwable e) {
                Log.e(TAG, "补丁$methodsId 发生异常，执行回滚！");
                saveRollbackFlag(methodsId);
            }

            private void saveRollbackFlag(String methodsId) {
                rollBacks.put(methodsId, true);
                //TODO:存储标志位到本地
            }

            @Override
            public boolean getRollback(String methodsId) {
                boolean rollback = rollBacks.get(methodsId) != null ? rollBacks.get(methodsId) : false;
                Log.d(TAG, "获取补丁$methodsId 的回滚状态为：$rollback");
                return rollback;
            }
        });
    }

    /**
     * 当有新补丁时清空rollbacks，标记所有位置为不回滚
     */
    public void notifyPatchUpdated() {
        if (rollBacks != null) {
            rollBacks.clear();
        }
        //TODO:存储标志位到本地
    }
}
