package org.neurobin.aapps.pushnotificationgcmclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class PushNotificationMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private static final String TAG = "MainActivity";

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ProgressBar mRegistrationProgressBar;
    private TextView mInformationTextView;
    public static boolean TOKEN_REFRESHED=true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gcm_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        Button rtbtn = (Button) findViewById(R.id.ret_btn);
        Button unreg_btn = (Button) findViewById(R.id.unreg_btn);
        rtbtn.setOnClickListener(this);
        unreg_btn.setOnClickListener(this);

        //GCM related part

        mRegistrationProgressBar = (ProgressBar) findViewById(R.id.registrationProgressBar);
        mInformationTextView = (TextView) findViewById(R.id.informationTextView);
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(GCMSharedPreferences.SENT_TOKEN_TO_SERVER, false);
                String tkmsg = "\nRegistration ID from GCM: " +
                        sharedPreferences.getString(GCMSharedPreferences.REG_ID, "N/A").substring(0,7);
                tkmsg = getString(R.string.gcm_send_message) + tkmsg;
                if (!intent.getExtras().getBoolean("register"))
                    tkmsg = getString(R.string.gcm_unregister_message);
                tkmsg = intent.getStringExtra("prefix") + tkmsg;
                if (sentToken) {
                    mInformationTextView.setText(tkmsg);
                } else {
                    mInformationTextView.setText(getString(R.string.token_error_message) + tkmsg);
                }
            }
        };


    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMSharedPreferences.REGISTRATION_COMPLETE));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.gcm_main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {
            Intent intent = new Intent(this, ScrollingActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ret_btn) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            if(sharedPreferences.getString(GCMSharedPreferences.REG_ID,"")!=null)
            {
                TOKEN_REFRESHED=false;
            }
            startRegistrationService(true, TOKEN_REFRESHED);
        }
        if (view.getId() == R.id.unreg_btn) {
            //try to unregister
            TOKEN_REFRESHED=false;
            startRegistrationService(false, TOKEN_REFRESHED);
        }

    }

    public void startRegistrationService(boolean reg, boolean tkr) {

        if (GCMCommonUtils.checkPlayServices(this)) {
            mRegistrationProgressBar = (ProgressBar) findViewById(R.id.registrationProgressBar);
            mRegistrationProgressBar.setVisibility(View.VISIBLE);
            TextView tv = (TextView) findViewById(R.id.informationTextView);
            EditText uname=(EditText)findViewById(R.id.etUserName);
            EditText uemail=(EditText)findViewById(R.id.etEmail);
            String name=uname.getText().toString();
            String email=uemail.getText().toString();

            if (reg) tv.setText(R.string.registering_message);
            else tv.setText(R.string.unregistering_message);
            Toast.makeText(this, "Background service started...", Toast.LENGTH_LONG).show();
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, MyGCMRegistrationIntentService.class);
            intent.putExtra("register", reg);
            intent.putExtra("tokenRefreshed", tkr);
            intent.putExtra("name",name);
            intent.putExtra("email",email);
            startService(intent);
        }

    }

}
