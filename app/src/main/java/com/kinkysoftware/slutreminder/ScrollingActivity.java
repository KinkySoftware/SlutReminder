package com.kinkysoftware.slutreminder;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScrollingActivity extends AppCompatActivity {

    Context ctx = this;
    String TAG = "SlutReminderMessages";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ListView listview = (ListView) findViewById(R.id.listview);
        String[] values = new String[] { "Android", "iPhone", "WindowsMobile"};

        final SharedPreferences pref = ctx.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        Set<String> storedMessageSet = pref.getStringSet(TAG, null);
        Set<String> defaultList = new HashSet(Arrays.asList(values));

        if (storedMessageSet.isEmpty()){
            SharedPreferences.Editor editor = pref.edit();
            Set<String> messageSet = pref.getStringSet(TAG, defaultList);
            editor.putStringSet(TAG, messageSet);
            editor.commit();
        }

        storedMessageSet = pref.getStringSet(TAG, null);
        final List<String> storedMessageList = new ArrayList(Arrays.asList(storedMessageSet.toArray(new String[storedMessageSet.size()])));

        final StableArrayAdapter adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, storedMessageList);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @TargetApi(16)
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    final int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);

                final AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setTitle("Edit Message");

                final EditText input = new EditText(ctx);
                input.setText(item, TextView.BufferType.EDITABLE);

                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        storedMessageList.remove(item);
                        String message = input.getText().toString();
                        storedMessageList.add(position,message);
                        SharedPreferences tempPref = ctx.getSharedPreferences(TAG, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = tempPref.edit();
                        editor.putStringSet(TAG, new HashSet<>(storedMessageList)).apply();
                        adapter.mIdMap.put(message, position);
                        adapter.notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO: Add a confirmation for deletion
                        view.animate().setDuration(1000).alpha(0)
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        storedMessageList.remove(item);
                                        adapter.notifyDataSetChanged();
                                        view.setAlpha(1);
                                    }
                        });
                    }
                });

                builder.show();
            }

        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setTitle("Add Message");

                final EditText input = new EditText(ctx);

                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String message = input.getText().toString();
                        storedMessageList.add(storedMessageList.size(),message);
                        SharedPreferences tempPref = ctx.getSharedPreferences(TAG, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = tempPref.edit();
                        editor.putStringSet(TAG, new HashSet<>(storedMessageList)).apply();
                        adapter.mIdMap.put(message, storedMessageList.size());
                        adapter.notifyDataSetChanged();
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
