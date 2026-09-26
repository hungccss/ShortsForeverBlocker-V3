package com.hungccss.shortsforever;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(48, 48, 48, 48);
        box.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(this);
        title.setText("Shorts Forever Blocker");
        title.setTextSize(24f);
        box.addView(title);

        TextView info = new TextView(this);
        info.setText("Chặn YouTube Shorts trong app YouTube. Không cần root, không dùng Internet.\n\nSau khi bật Trợ năng một lần, Android thường tự nối lại dịch vụ sau khi khởi động máy.");
        info.setTextSize(16f);
        info.setPadding(0, 28, 0, 28);
        box.addView(info);

        status = new TextView(this);
        status.setTextSize(16f);
        status.setPadding(0, 0, 0, 24);
        box.addView(status);

        Button accessibility = new Button(this);
        accessibility.setText("BẬT CHẶN SHORTS (TRỢ NĂNG)");
        accessibility.setOnClickListener(v -> {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        });
        box.addView(accessibility);

        Button battery = new Button(this);
        battery.setText("CHO PHÉP CHẠY NỀN / TẮT TỐI ƯU PIN");
        battery.setOnClickListener(v -> openBatterySettings());
        box.addView(battery);

        setContentView(box);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (status != null) {
            status.setText(isAccessibilityEnabled() ? "Trạng thái: ĐANG BẬT" : "Trạng thái: CHƯA BẬT");
        }
    }

    private boolean isAccessibilityEnabled() {
        ComponentName expected = new ComponentName(this, ShortsBlockerService.class);
        String enabled = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabled == null) return false;

        TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
        splitter.setString(enabled);
        while (splitter.hasNext()) {
            ComponentName current = ComponentName.unflattenFromString(splitter.next());
            if (expected.equals(current)) return true;
        }
        return false;
    }

    private void openBatterySettings() {
        try {
            Intent direct = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            direct.setData(Uri.parse("package:" + getPackageName()));
            startActivity(direct);
        } catch (Exception e) {
            try {
                startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
            } catch (Exception ignored) {
                Toast.makeText(this, "Hãy tắt tối ưu pin cho ứng dụng trong Cài đặt.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
