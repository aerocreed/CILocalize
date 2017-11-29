package com.indooratlas.android.sdk.examples;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.indooratlas.android.sdk.examples.imageview.ImageViewActivity;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Germano on 29/11/2017.
 */

//https://www.youtube.com/watch?v=nQnyAXJxngY

public class MainActivity extends ActionBarActivity {
    String[] items;
    ArrayList<String> listitems;
    ArrayAdapter<String> adapter;
    ListView listView;
    EditText editText;
    private JSONArray jsonArray;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        listView = (ListView)findViewById(R.id.listview);
        editText = (EditText)findViewById(R.id.txtsearch);
        initList();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().equals("")){
                    //reset listview
                    initList();
                }
                else {
                    //perform search
                    searchItem(charSequence.toString());
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void searchItem(String textToSearch){
        for(String item: items){
            if(!item.contains(textToSearch)){
                listitems.remove(item);
            }
        }

        adapter.notifyDataSetChanged();
    }

    public void initList(){
        //https://stackoverflow.com/questions/6938291/dynamically-inserting-string-into-a-string-array-in-android
        ArrayList<String> stringArrayList = new ArrayList<>();
        try {
            this.jsonArray = ImageViewActivity.readJsonFromFile(getResources().openRawResource(R.raw.floor_4));
            String tag, tipo;
            int count = 0;
            JSONObject jo;
            //Preenche o grafo
            for(int i=0; i<jsonArray.length(); i++) {
                jo = jsonArray.getJSONObject(i);
                tag = jo.getString("tag");
                tipo = jo.getString("tipo");
                if(tipo.equals("porta")){
                    stringArrayList.add(tag);
                }
            }
        } catch (JSONException e){
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        //items = new String[]{"Canada", "China", "Japan", "USA"};
        items = stringArrayList.toArray(new String[stringArrayList.size()]);
        listitems = new ArrayList<>(Arrays.asList(items));
        adapter = new ArrayAdapter<>(this, R.layout.list_item, R.id.txtitem, listitems);
        listView.setAdapter(adapter);
    }
}
