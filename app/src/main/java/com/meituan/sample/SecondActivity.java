package com.meituan.sample;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.meituan.sample.robusttest.other.Hll;
import com.meituan.robust.patch.RobustModify;
import com.meituan.robust.patch.annotaion.Add;
import com.meituan.robust.patch.annotaion.Modify;
import com.meituan.sample.robusttest.ConcreateClass;
import com.meituan.sample.robusttest.People;
import com.meituan.sample.robusttest.State;
import com.meituan.sample.robusttest.Super;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class SecondActivity extends AppCompatActivity implements View.OnClickListener {

    protected String flag = "flagstring";
    protected static String name = "zhang";
    public Hll hll = new Hll(true);
    private People people = new People();
    public static State state = new State(new Hll(true));
    private ListView listView;
    private String[] multiArr = {"列表1", "列表2", "列表3", "列表4"};
    private String inlineToString(){
     return super.toString();
 }
    @Override
    @Modify
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println(inlineToString());
        setContentView(R.layout.activity_main2);

        listView = (ListView) findViewById(R.id.listview);
        //test lambda expression
        TextView textView = (TextView) findViewById(R.id.secondtext);
        textView.setOnClickListener(v -> {
                    RobustModify.modify();
                    //test inner class accessibility
                    people.setAddr("asdasd");
                    getInfo(state, new Super(), 1l);
                    Log.d("robust", " onclick  in Listener");
                }
        );
        //change text on the  SecondActivity
        textView.setText(getTextInfo(new Object[]{(name)}));
//        Class clsArr = null;
//        try {
//            clsArr = Class.forName("[Ljava/lang/Object;");
//            System.out.println(clsArr.getName());
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
        // belows are test!!,you may ignore
        // belows are test!!,you may ignore
        // belows are test!!,you may ignore
        // belows are test!!,you may ignore

        //test inner class
        new Handler().postDelayed(new PreloadWebviewRunnable(this), 1100);
        Log.d("robust", hll.getStrings(1, flag));
        Log.d("robust", getString(R.string.app_name));



        //test for static methods
        Log.d("robust", "getValue is   " + getFieldValue("a", hll));
        Log.d("robust", "==========" + getInfo(state, new Super(), 1L) + "=============");

        //test for bundle
        Bundle bundle=new Bundle();
        bundle.putInt("asd",1);
        bundle.getFloat("asd");
        //test array
        BaseAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, multiArr);
        listView.setAdapter(adapter);
    }
    /**
     * if you change the return value you will change the show text,in the demo we built a patch to change the text
     */
    @Modify
    public String getTextInfo(Object[] meituan) {
        People p = new People();
        p.setName("mivazhang");
        p.setCates("  AutoPatch");
        people.setName(" I am Patch");
        ConcreateClass concreateClass = new ConcreateClass();
        getArray(meituan);
        //打开这部分注释，查看修复效果
//        Arrays.fill(multiArr,"修复后的数据");
//        return  "修复后：you make it!!   name is " + p.getName()  +  "   \npatch success   " + people.getName() ;
        return "error occur " + concreateClass.getA();

    }

    @Add
    public String[] getArray(Object[] meituan) {
        People p = new People();
        p.setName("mivazhang");
       return new String[]{p.getName(),"meituan"};
    }

// another usage of Modify anntation
//    @Modify(value = "com.meituan.sample.SecondActivity.onCreate(android.os.Bundle)")
    private String getInfo(State stae, Super s, long l) {
        String json = "[1,2,3,4,5]";
        Gson gson = new Gson();
        List<Integer> myObject = gson.fromJson(json, new TypeToken<List<Integer>>() {
        }.getType());
        TextView textView = new TextView(this);
        textView.setOnClickListener(v -> {
                    people.setAddr("getInfo ");
                    Log.d("robust", " getInfo onclick  in Listener");
                }
        );

        return "you make it!!   " + getTextI1(flag) + myObject;
    }


    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {

        return super.onCreateView(name, context, attrs);
    }




    @Add
    public State getTextI2(String baidu) {
        Bundle baseBundle = new Bundle();
        baseBundle.get("asdas");
        return new State(new Hll(false));
    }

    public State getTextI1(String baidu) {
        Bundle baseBundle = new Bundle();
        baseBundle.get("asdas");
        return new State(new Hll(false));
    }

    public static String[] methodWithArrayParameters(String[] flag) {
        return flag;
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(SecondActivity.this, "from implements onclick ", Toast.LENGTH_SHORT).show();

    }

    public static Field getReflectField(String name, Object instance) throws NoSuchFieldException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(name);
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                return field;
            } catch (NoSuchFieldException e) {
                // ignore and search next
            }
        }
        throw new NoSuchFieldException("Field " + name + " not found in " + instance.getClass());
    }

    public static Object getFieldValue(String name, Object instance) {
        try {
            return getReflectField(name, instance).get(instance);
        } catch (Exception e) {
            Log.d("robust", "getField error " + name + "   target   " + instance);
            e.printStackTrace();
        }
        return null;
    }

    class PreloadWebviewRunnable implements Runnable {

        WeakReference<SecondActivity> activityWeakReference;

        PreloadWebviewRunnable(SecondActivity activity) {
            activityWeakReference = new WeakReference<SecondActivity>(activity);
        }


        @Override
        public void run() {

            Toast.makeText(activityWeakReference.get(), "from PreloadWebviewRunnable PreloadWebviewRunnable ", Toast.LENGTH_SHORT).show();

        }
    }


    class OP<T> {
        T a;

        T test(T p) {
            Object op[] = new Object[10];
            op[1] = p;
            return (T) op;
        }
    }
}
