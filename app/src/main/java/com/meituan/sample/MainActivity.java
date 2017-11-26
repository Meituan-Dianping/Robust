package com.meituan.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.meituan.robust.PatchExecutor;
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
        new PatchExecutor(getApplicationContext(), new PatchManipulateImp(), new RobustCallBackSample()).start();
    }
}
