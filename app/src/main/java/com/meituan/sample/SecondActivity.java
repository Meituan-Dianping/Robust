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

import com.meituan.robust.patch.annotaion.Add;
import com.meituan.robust.patch.annotaion.Modify;

import java.lang.reflect.Field;

public class SecondActivity extends AppCompatActivity implements View.OnClickListener {

    protected static String name = "SecondActivity";
    private ListView listView;
    private String[] multiArr = {"列表1", "列表2", "列表3", "列表4"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        listView = (ListView) findViewById(R.id.listview);
        TextView textView = (TextView) findViewById(R.id.secondtext);
        textView.setOnClickListener(v -> {
//                    RobustModify.modify();
                    Log.d("robust", " onclick  in Listener");
                }
        );
        //change text on the  SecondActivity
        textView.setText(getTextInfo());

        //test array
        BaseAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, multiArr);
        listView.setAdapter(adapter);
    }

    @Modify
    public String getTextInfo() {
        getArray();
//        return "error occur " ;
        return "error fixed";
    }

    @Add
    public String[] getArray() {
       return new String[]{"hello","world"};
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {

        return super.onCreateView(name, context, attrs);
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
}
