package com.rexvit.browser.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.rexvit.browser.App;
import com.rexvit.browser.R;
import com.rexvit.browser.constant.SettingsKeys;
import com.rexvit.browser.dialog.AppSettings;
import com.rexvit.browser.dialog.MessageBox;
import com.rexvit.browser.utils.Utils;

import java.util.Map;


public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int STORAGE_PERMISSION_CODE = 100;
    ActivityResultLauncher<String[]> rpl;
    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.POST_NOTIFICATIONS};
    public static String TAG = "BaseActivity";
    public App app;
    public Vibrator vibrator;
    public boolean shouldChangeThemeOnDemand = true;
    public boolean finishOnResume = false;
    public boolean isActivityRunning = false;
    private int isBackPressEventFired = 0;

    protected abstract int getLayoutResId();

    protected abstract void onExit();


  private int result ;
    @Override 
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        initiate();

        rpl = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                new ActivityResultCallback<Map<String, Boolean>>() {
                    @Override
                    public void onActivityResult(Map<String, Boolean> isGranted) {
                        boolean granted = true;
                        for (Map.Entry<String, Boolean> x : isGranted.entrySet()) {
                            logthis(x.getKey() + " is " + x.getValue());
                            if (!x.getValue()) granted = false;
                        }
                        if (granted)
                            logthis("Permissions granted for api 33+");
                    }
                }
        );




        if (Build.VERSION.SDK_INT >= 32) {
            askPer();
        }

    }








    public void closeApplication() {
        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage( getBaseContext().getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    protected void initiate() {
        this.vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        this.app = (App) getApplication();
        if (getLayoutResId() != -1) {
            setUpTheme();
            setContentView(getLayoutResId());
        }

    }


    private void setUpTheme() {
        if (this.shouldChangeThemeOnDemand) {
            SharedPreferences preferences = new AppSettings(this.app).getPreferences();
            String string = SettingsKeys.THEME_SETTINGS_KEY;
            String stringSys = getString(R.string.theme_def);
            String stringLight = getString(R.string.theme_light);
            String stringDark = getString(R.string.theme_dark);
            String string9 = preferences.getString(string, stringSys);
            if (string9.equals(stringSys)) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

            }
            else if (string9.equals(stringLight)) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                setTheme(R.style.application_theme_Light);
                Utils.chanceNotificationBarcolor(this);

            } else if (string9.equals(stringDark)) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                setTheme(R.style.application_theme_dark);
            }
          else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }
        }
    }

    @Override 
    public void onBackPressed() {
        super.onBackPressed();
        onExit();
    }


    public App getApp() {
        return this.app;
    }


    public Drawable getDrawableImage(int resId) {
        return getResources().getDrawable(resId);
    }

    public void startActivity(Class activityClass) {
        startActivity(new Intent(this, activityClass));
    }

    public void exit() {
        finish();
    }




    public int getColorFrom(int resColorId) {
        return getResources().getColor(resColorId);
    }

    public void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void toast(int message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void vibrate(int duration) {
        if (this.vibrator != null) {
            this.vibrator.vibrate(20L);
        }
    }

    
    public void exitActivityOnDoublePress() {
        if (this.isBackPressEventFired == 0) {
            Toast.makeText(this, "Press back once more to exit", Toast.LENGTH_SHORT).show();
            this.isBackPressEventFired = 1;
            new CountDownTimer(2000L, 1000L) { 
                @Override 
                public void onTick(long time) {
                }

                @Override 
                public void onFinish() {
                    BaseActivity.this.isBackPressEventFired = 0;
                }
            }.start();
        } else if (this.isBackPressEventFired == 1) {
            this.isBackPressEventFired = 0;
            finish();
        }
    }



   
    protected void initializeClickEvent(int... ids) {
        for (int viewId : ids) {
            this.findViewById(viewId).setOnClickListener(this);
        }
    }


    public void logthis(String msg) {
        Log.d(TAG, msg);
    }

    public boolean isStoragePermissionGranted() {

        boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        boolean hasPermission2 = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (hasPermission&&hasPermission2) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else {
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    private boolean hasReadPermissions() {
        return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean hasWritePermissions() {
        return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }


    private boolean askPer() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!allPermissionsGranted()) {
                rpl.launch(REQUIRED_PERMISSIONS);
            }
        }


        return true;
    }
    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    public void showSimpleHtmlMessageBox(String message) {
        MessageBox messageBox = new MessageBox(this);
        messageBox.setMessage(Html.fromHtml(message));
        messageBox.show();
    }

    public void showSimpleHtmlMessageBox(String message, MessageBox.OnOkListener okClickListener) {
        MessageBox messageBox = new MessageBox(this);
        messageBox.setMessage(Html.fromHtml(message));
        messageBox.clickListener = okClickListener;
        messageBox.show();
    }
}
