package ru.dean.dtclient.views;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;

import ru.dean.dtclient.R;
import ru.dean.dtclient.Storage;


public class SettingsActivity extends Activity {

    private CheckBox mcbAutoLogin;
    private SharedPreferences sPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mcbAutoLogin = (CheckBox) findViewById(R.id.cbAutoLogin);

        sPref = getSharedPreferences("dtclientsettings", MODE_PRIVATE);
        loadSettings();
    }

    public void onBackPressed() {
        super.onBackPressed();
        saveSettings();


    }

    private void saveSettings() {

        SharedPreferences.Editor ed = sPref.edit();
        if (mcbAutoLogin.isChecked()) {
            ed.putBoolean("autologin", true);
            ed.putString("login", Storage.Inst().user.Login);
            ed.putString("password", Storage.Inst().user.Password);

        } else {
            ed.putBoolean("autologin", false);
            ed.putString("login", "");
            ed.putString("password", "");
        }
        ed.commit();
    }
    private void loadSettings() {

        boolean autologin = sPref.getBoolean("autologin", false);
        if (autologin) {
            mcbAutoLogin.setChecked(true);
        }



    }


}
