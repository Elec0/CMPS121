package com.example.alissawoo.cs121hw2;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class DetailActivity extends AppCompatActivity {

    public JSONObject jo = null;
    public JSONArray ja = null;
    public int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent i = getIntent();
        String title = i.getStringExtra("first");
        String description = i.getStringExtra("second");
        String date = i.getStringExtra("third");
        String time = i.getStringExtra("fourth");
        String gps = i.getStringExtra("fifth");

        index = i.getIntExtra("index",0);




        TextView t = (TextView) findViewById(R.id.textView3);
        TextView d = (TextView) findViewById(R.id.textView4);
        TextView d2 = (TextView) findViewById(R.id.textView5);
        TextView t2 = (TextView) findViewById(R.id.textView6);
        TextView g = (TextView) findViewById(R.id.textView7);
        Button delete = (Button)findViewById(R.id.button2);



        t.setText(title);
        d.setText(description);
        d2.setText(date);
        t2.setText(time);
        g.setText(gps);


        delete.setOnClickListener(new Button.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            public void onClick(View view){
                try{
                    File f = new File(getFilesDir(), "file.ser");
                    FileInputStream fi = new FileInputStream(f);
                    ObjectInputStream o = new ObjectInputStream(fi);
                    // Notice here that we are de-serializing a String object (instead of
                    // a JSONObject object) and passing the String to the JSONObject’s
                    // constructor. That’s because String is serializable and
                    // JSONObject is not. To convert a JSONObject back to a String, simply
                    // call the JSONObject’s toString method.
                    String j = null;
                    try{
                        j = (String) o.readObject();
                    }
                    catch(ClassNotFoundException c){
                        c.printStackTrace();
                    }
                    try {
                        jo = new JSONObject(j);
                        ja = jo.getJSONArray("data");
                    }
                    catch(JSONException e){
                        e.printStackTrace();
                    }
                }
                catch(Exception e){
//                    Log.d("Exception", e.toString());
                    return;
                }
                ja.remove(index);
                try{
                    File f = new File(getFilesDir(), "file.ser");
                    FileOutputStream fo = new FileOutputStream(f);
                    ObjectOutputStream o = new ObjectOutputStream(fo);
                    String j = jo.toString();
                    o.writeObject(j);
                    o.close();
                    fo.close();
                }
                catch(IOException e){

                }
                //pop the activity off the stack
                Intent d = new Intent(DetailActivity.this, MainActivity.class);
                d.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(d);
            }});

    }

    protected void onResume(Bundle savedInstanceState){
        //
    }
}


