package com.engindeniz.appupdatetest;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.engindeniz.appupdatetest.databinding.ActivityMainBinding;
import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.AppUpdaterUtils;
import com.github.javiersantos.appupdater.enums.AppUpdaterError;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.github.javiersantos.appupdater.objects.Update;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        AppUpdater appUpdater = new AppUpdater(this)
                .setUpdateFrom(UpdateFrom.JSON)
                .setUpdateJSON("https://raw.githubusercontent.com/Engindeniz00/UpdateAppTest/master/app/update-changelog.json")
                .setDisplay(Display.DIALOG)
                .setButtonUpdateClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new AppUpdaterUtils(MainActivity.this)
                                .setUpdateFrom(UpdateFrom.JSON)
                                .setUpdateJSON("https://raw.githubusercontent.com/Engindeniz00/UpdateAppTest/master/app/update-changelog.json")
                                .withListener(new AppUpdaterUtils.UpdateListener() {
                                    @Override
                                    public void onSuccess(Update update, Boolean isUpdateAvailable) {
                                        Log.d(TAG, "onSuccess: downloadURl :  "+update.getUrlToDownload());
                                        Log.d(TAG, "onSuccess: latestVer :  "+update.getLatestVersion());
                                    }

                                    @Override
                                    public void onFailed(AppUpdaterError error) {

                                    }
                                })
                                .start();
                    }
                });

        appUpdater.start();




        Log.d(TAG, "onCreate: "+binding.versionTextView.getText().toString());
    }
}