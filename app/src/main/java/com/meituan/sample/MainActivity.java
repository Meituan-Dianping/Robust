package com.meituan.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.meituan.Hll;
import com.meituan.robust.Patch;
import com.meituan.robust.PatchExecutor;
import com.meituan.robust.RobustCallBack;

import java.lang.reflect.Constructor;

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
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //除了get方法有拦截之外，其他的几个方法没有做拦截，可能是处理get set方法的误杀
                Toast.makeText(MainActivity.this, "arrived in ", Toast.LENGTH_SHORT).show();
                state.setIndex(hll, 1, 1l, new Object());
                Log.d("robust", "state.get()  " + state.get().toString());
                Log.d("robust", " state.getIndex()  " + state.getIndex());
                Super s = new Super();
                Log.d("robust", "patch result before :" + s.check());
                Log.d("robust", "patch result after:" + s.protextedMethod());
                textView.setText(s.getText());
                s.getinstance();
//                Log.d("robust", "s.getinstance(new Super())==  :    " + s.getinstance(new Super()));
            }
        });
        Button patch = (Button) findViewById(R.id.patch);
        patch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                PatchUtils.applyPatch(MainActivity.this, "group", "1.0", "channel", 1233445l, "qwertyuiop1", "QQQQQQQQQQQ");
                new PatchExecutor(getApplicationContext(), new PatchManipulateImp(),  new Callback()).start();

            }
        });

        try {
            ImageQualityUtil.loadImage(null, null, null, 1, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        findViewById(R.id.jump_second_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = (Intent) invokeReflectConstruct("android.content.Intent", new Object[]{MainActivity.this, SecondActivity.class}, new Class[]{Context.class, Class.class});
                startActivity(intent);
                Log.d("robusttest", (new NoField()).toString());
                Log.d("robusttest", ImageQualityUtil.getDefaultSize("asdasdasd"));
                SampleClass sampleClass=new SampleClass();
                sampleClass.multiple(-1);
            }
        });

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

    private static View getChildView(View view) {
        if (!(view instanceof ViewGroup) || ((ViewGroup) view).getChildCount() < 1) {
            return view;
        }
        ViewGroup viewGroup = (ViewGroup) view;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View text = getChildView(viewGroup.getChildAt(i));
            if (text instanceof TextView) {
                if ("作品累计票房".equals(((TextView) text).getText())) {
                    return text;
                }
            }
        }
        return null;

    }

    public static Object invokeReflectConstruct(String className, Object[] parameter, Class[] args) {
        try {
            Class clazz = Class.forName(className);
            Constructor constructor = clazz.getDeclaredConstructor(args);
            constructor.setAccessible(true);
            return constructor.newInstance(parameter);
        } catch (Exception e) {
            Log.d("robust", "invokeReflectConstruct construct error " + className + "   parameter   " + parameter);
            e.printStackTrace();
        }
        throw new RuntimeException("invokeReflectConstruct error ");
    }

}
