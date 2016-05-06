package com.shklianko.bsuir.schedulefetcher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    public final static String EXTRA_MESSAGE = "com.shklianko.bsuir.schedulefetcher.add_message";

    final ArrayList<String> groupsList = new ArrayList<String>();
    GroupsArrayAdapter groupsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        TextView mTitle = (TextView) myToolbar.findViewById(R.id.toolbar_title);
        mTitle.setText("Manage Groups");

        SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.groups_storage), Context.MODE_PRIVATE);
        Set<String> groupsSet =  sharedPref.getStringSet("groups_storage_set_id", null);

        if(groupsSet != null) {
            for (final String storedGroup : groupsSet) {
                groupsList.add(storedGroup);
            }
        }

        final ListView listview = (ListView) findViewById(R.id.grouplistview);
        groupsAdapter = new GroupsArrayAdapter(this, groupsList);
        listview.setAdapter(groupsAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
                /*view.animate().setDuration(2000).alpha(0)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                list.remove(item);
                                adapter.notifyDataSetChanged();
                                view.setAlpha(1);
                            }
                        });*/
            }

        });
    }

    public void selectGroup(View view) {
        RelativeLayout vwParentRow = (RelativeLayout)view.getParent();
        TextView child = (TextView)vwParentRow.getChildAt(0);

        Intent intent = new Intent(this, DisplayGroupScheduleActivity.class);
        intent.putExtra(EXTRA_MESSAGE, child.getText().toString());
        startActivity(intent);
    }

    public void removeGroup(View view) {

        RelativeLayout vwParentRow = (RelativeLayout)view.getParent();
        TextView child = (TextView)vwParentRow.getChildAt(0);

        /*int c = Color.CYAN;
        vwParentRow.setBackgroundColor(c);
        vwParentRow.refreshDrawableState();*/

        EditText editText = (EditText) findViewById(R.id.edit_message);
        String groupToRemove = child.getText().toString();

        SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.groups_storage), Context.MODE_PRIVATE);

        Set<String> groupsSet =  sharedPref.getStringSet("groups_storage_set_id", null);

        for (final String storedGroup : groupsSet) {
            if (storedGroup.equals(groupToRemove)) {
                groupsSet.remove( groupToRemove );
                groupsList.remove( groupToRemove );
                groupsAdapter.notifyDataSetChanged();
                editText.setText("");

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putStringSet("groups_storage_set_id", groupsSet);
                editor.commit();
                return;
            }
        }
    }

    public void addGroup(View view) {

        EditText editText = (EditText) findViewById(R.id.edit_message);
        String group = editText.getText().toString();
        if(group.trim().length() > 0) {

            SharedPreferences sharedPref = this.getSharedPreferences(
                    getString(R.string.groups_storage), Context.MODE_PRIVATE);

            Set<String> groupsSet = sharedPref.getStringSet("groups_storage_set_id", null);

            if (groupsSet != null) {
                for (final String storedGroup : groupsSet) {
                    if (storedGroup.equals(group)) {
                        return;
                    }
                }
            } else {
                groupsSet = new HashSet<>();
            }

            groupsSet.add(group);
            groupsList.add(group);
            groupsAdapter.notifyDataSetChanged();
            editText.setText("");

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putStringSet("groups_storage_set_id", groupsSet);
            editor.commit();
        }
    }
}

class GroupsArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final ArrayList<String> values;

    public GroupsArrayAdapter(Context context, ArrayList<String> values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.grouplistrowlayout, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.label);
        textView.setText(values.get(position));

        return rowView;
    }
}

