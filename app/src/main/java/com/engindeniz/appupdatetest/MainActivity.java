package com.engindeniz.appupdatetest;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.engindeniz.appupdatetest.databinding.ActivityMainBinding;
import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.AppUpdaterUtils;
import com.github.javiersantos.appupdater.BuildConfig;
import com.github.javiersantos.appupdater.enums.AppUpdaterError;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.github.javiersantos.appupdater.objects.Update;

import java.io.File;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private static final String DOWNLOAD_STRING = "download";
    private static final String APK_STRING = "app-debug.apk";
    private ProgressBar downloadAppProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


        downloadAppProgress = binding.downloadAppProgress;

        extraStoragePermission();
        isStoragePermissionGranted();

        if(isNetworkAvailable(MainActivity.this))
        {
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
                                            Log.d(TAG, "onSuccess: versionCode : "+update.getLatestVersionCode());

                                            downloadUpdate(MainActivity.this,update.getUrlToDownload(),update.getLatestVersion());
                                        }

                                        @Override
                                        public void onFailed(AppUpdaterError error) {
                                            Log.d(TAG, "onFailed: on update available");
                                        }
                                    })
                                    .start();
                        }
                    });

            appUpdater.start();

        }
        else
        {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }

        Log.d(TAG, "onCreate: "+binding.versionTextView.getText().toString());
    }

    public static boolean isNetworkAvailable(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();
        if (activeNetworkInfo != null) { // connected to the internet
            if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                return true;
            } else if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                return true;
            }
        }
        Toast.makeText(context, R.string.no_Internet, Toast.LENGTH_SHORT).show();
        return false;
    }

    private void extraStoragePermission()
    {
        //installtion permission

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(!getPackageManager().canRequestPackageInstalls()){
                startActivityForResult(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                        .setData(Uri.parse(String.format("package:%s", getPackageName()))), 1);
            }
        }
//Storage Permission

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }
    private boolean isStoragePermissionGranted() {
        if (MainActivity.this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED && MainActivity.this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG,"Storage permission is granted");
            return true;
        } else {
            Log.d(TAG,"Storage permission is revoked");
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return false;
        }
    }

    private void downloadUpdate(final Context context, URL downUrl, String versionString)
    {
        try{
            StringBuilder newUrlPath = new StringBuilder();
            newUrlPath.append(downUrl)
                    .append("/")
                    .append(DOWNLOAD_STRING)
                    .append("/").append(versionString)
                    .append("/")
                    .append(APK_STRING);

            Log.d(TAG, "onSuccess: new url to download:"+newUrlPath);

            Log.d(TAG, "downloadUpdate: file path dowload"+Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
            final File apk_file_path = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),APK_STRING);
            Log.d(TAG, "downloadUpdate: apk file path: "+apk_file_path);

            if (apk_file_path.exists())
            {
                Log.d(TAG, "downloadUpdate: is deleted "+apk_file_path.delete());
            }

            Log.d(TAG,"Downloading request on url :"+newUrlPath);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(newUrlPath.toString()));
            request.setDescription(versionString);
            request.setTitle(getResources().getString(R.string.app_name));

            final Uri uri = Uri.parse("file://" + apk_file_path);
            Log.d(TAG,"Downloading uri:"+uri);
            request.setDestinationUri(uri);

            // get download service and enqueue file
            final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            final long downloadId = manager.enqueue(request);

            downloadAppProgress.setVisibility(View.VISIBLE);
            new Thread(new Runnable() {
                @SuppressLint("Range")
                @Override
                public void run() {
                    boolean downloading = true;
                    while(downloading) {
                        DownloadManager.Query q = new DownloadManager.Query();
                        q.setFilterById(downloadId);
                        Cursor cursor = manager.query(q);
                        cursor.moveToFirst();

                        final int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                        if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                            downloading = false;
                        }
                        @SuppressLint("Range") int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                        if (bytes_total != 0) {
                            final int dl_progress = (int) ((bytes_downloaded * 100L) / bytes_total);

                            Activity activity = (Activity) context;
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    downloadAppProgress.setProgress((int) dl_progress);

                                }
                            });
                        }
                        cursor.close();

                    }
                }
            }).start();

            //set BroadcastReceiver to install app when .apk is downloaded
            BroadcastReceiver onComplete = new BroadcastReceiver() {
                public void onReceive(Context ctxt, Intent intent) {
                    //BroadcastReceiver on Complete
                    if (apk_file_path.exists()) {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                Uri apkUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileprovider", apk_file_path);
                                Log.d(TAG, "onReceive: apk uri : "+apkUri);
                                intent = new Intent(Intent.ACTION_VIEW);
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                                Log.d(TAG, "onReceive: NEW VER");
                            }
                            else {
                                Uri apkUri = Uri.fromFile(apk_file_path);
                                intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(apkUri, manager.getMimeTypeForDownloadedFile(downloadId));
                                intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                Log.d(TAG, "onReceive: OLD VER");
                            }
                        Log.d(TAG, "onReceive: ACTIVIY STARTED");
                        context.startActivity(intent);
                    }else{
                        Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                        downloadAppProgress.setVisibility(View.GONE);
                    }
                    Log.d(TAG, "onReceive: ACTIVITY UNREGISTER");
                    context.unregisterReceiver(this);
                }
            };
            Log.d(TAG, "downloadUpdate: NEW INTENT DOWNLOAD COMPLETE");
            context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            downloadAppProgress.setVisibility(View.GONE);
        }

    }
}