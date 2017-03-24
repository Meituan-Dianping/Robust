package com.meituan.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.meituan.sample.robusttest.other.Hll;
import com.meituan.robust.Patch;
import com.meituan.robust.PatchExecutor;
import com.meituan.robust.RobustCallBack;
import com.meituan.sample.robusttest.ImageQualityUtil;
import com.meituan.sample.robusttest.NoField;
import com.meituan.sample.robusttest.SampleClass;
import com.meituan.sample.robusttest.State;
import com.meituan.sample.robusttest.Super;

/**
 *
 * For users of Robust you may only to use MainActivity or SecondActivity,other classes are used for test.<br>
 *<br>
 * If you just want to use Robust ,we recommend you just focus on MainActivity SecondActivity and PatchManipulateImp.Especially three buttons in MainActivity<br>
 *<br>
 * in the MainActivity have three buttons; "SHOW TEXT " Button will change the text in the MainActivity,you can patch the show text.<br>
 *<br>
 * "PATCH" button will load the patch ,the patch path can be configured in PatchManipulateImp.<br>
 *<br>
 * "JUMP_SECOND_ACTIVITY" button will jump to the second ACTIVITY,so you can patch a Activity.<br>
 *<br>
 * Attention to this ,We recommend that one patch is just for one built apk ,because every  built apk has its unique mapping.txt and resource id<br>
 *
 *@author mivanzhang
 */

public class MainActivity extends AppCompatActivity {

    TextView textView;
    Button button;
    State<Integer> state;

    Hll hll = new Hll(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.textView);
        state = new State<>(hll);
        Button patch = (Button) findViewById(R.id.patch);
        //beigin to patch
        patch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PatchExecutor(getApplicationContext(), new PatchManipulateImp(),  new Callback()).start();

            }
        });

        findViewById(R.id.jump_second_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =  new Intent(MainActivity.this,SecondActivity.class);
                startActivity(intent);
                Log.d("robusttest", (new NoField()).toString());
                Log.d("robusttest", ImageQualityUtil.getDefaultSize("asdasdasd"));
                SampleClass sampleClass=new SampleClass();
                sampleClass.multiple(-1);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "arrived in ", Toast.LENGTH_SHORT).show();
                state.setIndex(hll, 1, 1l, new Object());
                Log.d("robust", "state.get()  " + state.get().toString());
                Log.d("robust", " state.getIndex()  " + state.getIndex());
                Super s = new Super();
                Log.d("robust", "patch result before :" + s.check());
                Log.d("robust", "patch result after:" + s.protextedMethod());
                textView.setText(s.getText());
                s.getinstance();
            }
        });
        //test situation,
        try {
            ImageQualityUtil.loadImage(null, null, null, 1, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //patch  data report
    class Callback implements RobustCallBack {

        @Override
        public void onPatchListFetched(boolean result, boolean isNet) {
             System.out.println(" robust arrived in onPatchListFetched");
        }

        @Override
        public void onPatchFetched(boolean result, boolean isNet, Patch patch) {
            System.out.println(" robust arrived in onPatchFetched");
        }

        @Override
        public void onPatchApplied(boolean result, Patch patch) {
            System.out.println(" robust arrived in onPatchApplied");

        }

        @Override
        public void logNotify(String log, String where) {
            System.out.println(" robust arrived in logNotify");
        }

        @Override
        public void exceptionNotify(Throwable throwable, String where) {
            System.out.println(" robust arrived in exceptionNotify");
        }
    }


}
