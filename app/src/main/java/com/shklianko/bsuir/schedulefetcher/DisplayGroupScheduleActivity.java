package com.shklianko.bsuir.schedulefetcher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.SAXParserFactory;

public class DisplayGroupScheduleActivity extends AppCompatActivity {

    private String activeGroupId;

    private SharedPreferences sharedPref;

    private static final String scheduleUrl = "http://www.bsuir.by/schedule/examSchedule.xhtml?id=";

    private TextView messageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Intent intent = getIntent();
        String groupId = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        this.activeGroupId = groupId;

        LinearLayout layout = (LinearLayout) findViewById(R.id.content);
        messageView = new TextView(this);
        layout.addView(messageView);

        TextView mTitle = (TextView) myToolbar.findViewById(R.id.toolbar_title);
        mTitle.setText("Group " + groupId);

        this.sharedPref = this.getSharedPreferences(
                getString(R.string.schedule_storage), Context.MODE_PRIVATE);

        if (!this.hasPersistedSchedule(groupId)) {
            this.refresh(groupId);
        } else {
            showStoredSchedule(groupId);
        }
    }

    private void showStoredSchedule(final String groupId) {
        this.displaySchedule(this.getStoredSchedule(groupId));
    }

    private void refresh(final String groupId) {
        this.fetchScheduleForGroup(groupId);
    }

    private void displaySchedule(final String scheduleHtml) {
        WebView webview = new WebView(this);
        LinearLayout layout = (LinearLayout) findViewById(R.id.content);
        webview.loadData(scheduleHtml, "text/html; charset=utf-8", "utf-8");
        layout.addView(webview);
    }

    private void displayMessage(final String message) {
        this.messageView.setText(message);
    }

    private void fetchScheduleForGroup(final String groupId) {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
                    String result = "error";
                    try {
                        URL url = new URL(scheduleUrl + groupId);
                        URLConnection conn = url.openConnection();

                        BufferedReader br = new BufferedReader(
                                new InputStreamReader(conn.getInputStream()), 8);
                        StringBuilder sb = new StringBuilder();
                        String inputLine;

                        while ((inputLine = br.readLine()) != null) {
                            sb.append(inputLine + "\n");
                        }

                        final String sourceHtml = sb.toString();
                        result = findScheduleInSourceHtml(sourceHtml);
                        if(result.contains("Расписание для группы")) {
                            storeFetchedSchedule(groupId, result);
                            displaySchedule(result);
                        } else {
                            displayMessage("Could not find schedule");
                        }

                        br.close();
                    } catch (final Exception e) {
                        e.printStackTrace();
                        displayMessage("Error occured while trying to fetch schedule.");
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    private String findScheduleInSourceHtml(final String sourceHtml) {
        return sourceHtml;
    }

    private boolean hasPersistedSchedule(final String groupId) {
        final String schedule = sharedPref.getString(groupId, null);
        return this.isValidSchedule(schedule);
    }

    private boolean isValidSchedule(final String schedule) {
        return schedule != null && schedule.length() > 0;
    }

    private void storeFetchedSchedule(final String groupId, final String schedule) {
        if (this.isValidSchedule(schedule)) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(groupId, schedule);
            editor.commit();
        }
    }

    private String getStoredSchedule(final String groupId) {
        return sharedPref.getString(groupId, "Schedule not found");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_group:
                this.finish();
                return true;

            case R.id.action_refresh:
                this.refresh(this.activeGroupId);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}

class RetrieveScheduleTask extends AsyncTask<String, Void, String> {

    private Exception exception;

    protected String doInBackground(final String... urls) {
        String result = "error";
        try {
            URL url = new URL(urls[0]);
            URLConnection conn = url.openConnection();

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String inputLine;

            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine + "\n");
            }

            final String sourceHtml = sb.toString();
            result = this.findScheduleInSourceHtml(sourceHtml);

            br.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private String findScheduleInSourceHtml(final String sourceHtml) {
        return sourceHtml;
    }

    protected void onPostExecute(String string) {
    }
}
