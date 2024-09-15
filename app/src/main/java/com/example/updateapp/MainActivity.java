package com.example.updateapp;

import static android.app.ProgressDialog.show;
import static androidx.core.view.accessibility.AccessibilityEventCompat.setAction;
import static java.lang.Math.log;

import android.os.Bundle;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;

public class MainActivity extends AppCompatActivity {


    private ActivityResultLauncher activityResultLauncher;
    private AppUpdateManager appUpdateManager;
    private static int DAYS_FOR_FLEXIBLE_UPDATE = 7;
    private static int DAYS_FOR_IMMEDIATE_UPDATE = 14;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        /*

        Follow us on Facebook Page: Android Squad
        Author: Mi Tayef - Majharul Islam Tayef

        Contact:

        WhatsApp: +880 1622656866

        Linkedin: Majharul Islam Tayef
        Link: https://www.linkedin.com/in/mitayef/

        Facebook: Mi Tayef
        Link: https://www.facebook.com/MiTayef.09

        Facebook Pages: Android Squad
        Link: https://www.facebook.com/AndroidSquadOfficial

        Github: Android Squad
        Link: https://github.com/MiTayef

         */




        checkInAppUpdate();

    } // Close onCreate Method Here

    // =============================== In App Update Method START HERE ======================

    private void checkInAppUpdate(){
        appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());
        appUpdateManager.registerListener(listener);

        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE){

                if (appUpdateInfo.clientVersionStalenessDays() != null
                        && appUpdateInfo.clientVersionStalenessDays() >= DAYS_FOR_IMMEDIATE_UPDATE
                        && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    // Request the update.
                    startInAppUpdate(appUpdateInfo, AppUpdateType.IMMEDIATE);
                } else if (appUpdateInfo.clientVersionStalenessDays() != null
                        && appUpdateInfo.clientVersionStalenessDays() >= DAYS_FOR_FLEXIBLE_UPDATE
                        && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    // Request the update.
                    startInAppUpdate(appUpdateInfo, AppUpdateType.FLEXIBLE);
                }

            }
        });

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() != RESULT_OK) {
                            //   log("Update flow failed! Result code: " + result.getResultCode());
                        }
                    }
                });

    } // Close checkInAppUpdate Method Here

    InstallStateUpdatedListener listener = state -> {
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            popupSnackbarForCompleteUpdate();
        }
    };

    // Displays the snackbar notification and call to action.
    private void popupSnackbarForCompleteUpdate() {
        Snackbar snackbar =
                Snackbar.make(
                        findViewById(R.id.main),
                        "An update has just been downloaded.",
                        Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("RESTART", view -> appUpdateManager.completeUpdate());
        snackbar.setActionTextColor(
                getResources().getColor(R.color.green));
        snackbar.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        appUpdateManager.unregisterListener(listener);
    }


    // Checks that the update is not stalled during 'onResume()'.
    // However, you should execute this check at all app entry points.
    @Override
    protected void onResume() {
        super.onResume();

        appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(appUpdateInfo -> {
                    // If the update is downloaded but not installed,
                    // notify the user to complete the update.
                    if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                        popupSnackbarForCompleteUpdate();
                    }
                });
    }


    private void startInAppUpdate(AppUpdateInfo appUpdateInfo, Integer updateType){
        appUpdateManager.startUpdateFlowForResult(

                appUpdateInfo,
                activityResultLauncher,
                AppUpdateOptions.newBuilder(updateType).build());
    }

    // =============================== In App Update Method CLOSE HERE ======================



} // Close Public Class