package com.droiddevhub.notesapp.Model;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionHelper {

    private static final int PERMISSION_REQUEST_CODE = 1234; // Một mã yêu cầu quyền duy nhất

    // Kiểm tra và yêu cầu quyền thông báo
    public static void checkAndRequestNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Quyền chưa được cấp, yêu cầu quyền từ người dùng
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    // Xử lý kết quả yêu cầu quyền
    public static void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults, Context context) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền đã được cấp
                Toast.makeText(context, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                // Quyền bị từ chối
                Toast.makeText(context, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

