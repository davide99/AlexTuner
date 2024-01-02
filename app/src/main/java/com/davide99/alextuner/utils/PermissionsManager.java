package com.davide99.alextuner.utils;

import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class PermissionsManager {
    private final AppCompatActivity activity;
    private final String[] permissions;
    private final int requestCode;
    private final PermissionsGrantedCallback grantedCallback;
    private final PermissionsDeniedCallback deniedCallback;

    public interface PermissionsGrantedCallback {
        void onPermissionsGranted();
    }

    public interface PermissionsDeniedCallback {
        void onPermissionsDenied();
    }

    public PermissionsManager(
            AppCompatActivity activity,
            PermissionsGrantedCallback grantedCallback,
            PermissionsDeniedCallback deniedCallback,
            String... permissions
    ) {
        this.activity = activity;
        this.permissions = permissions;
        this.requestCode = Math.abs((int) System.currentTimeMillis());
        this.grantedCallback = grantedCallback;
        this.deniedCallback = deniedCallback;
    }

    public void requestPermissions() {
        if (checkPermissions()) {
            grantedCallback.onPermissionsGranted();
        } else {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        }
    }

    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        if (requestCode == this.requestCode) {
            if (checkPermissionsResult(grantResults)) {
                grantedCallback.onPermissionsGranted();
            } else {
                deniedCallback.onPermissionsDenied();
            }
        }
    }

    private boolean checkPermissions() {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private boolean checkPermissionsResult(@NonNull int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
