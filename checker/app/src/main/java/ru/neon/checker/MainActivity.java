package ru.neon.checker;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.TextView;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {
    EditText urlBox;
    TextView result;
    Handler ui = new Handler(Looper.getMainLooper());

    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        urlBox = findViewById(R.id.urlBox);
        result = findViewById(R.id.result);
        findViewById(R.id.checkBtn).setOnClickListener(v -> check(urlBox.getText().toString()));
        findViewById(R.id.btnGithub).setOnClickListener(v -> check("https://github.com"));
        findViewById(R.id.btnGoogle).setOnClickListener(v -> check("https://google.com"));
        findViewById(R.id.btnQwen).setOnClickListener(v -> check("https://chat.qwen.ai"));
        findViewById(R.id.btnAppteka).setOnClickListener(v -> check("https://appteka.store"));
        findViewById(R.id.btnTelegram).setOnClickListener(v -> check("https://t.me"));
        findViewById(R.id.btnAll).setOnClickListener(v -> checkAll());
    }

    String probe(String u) {
        if (!u.startsWith("http")) u = "https://" + u;
        long t0 = System.currentTimeMillis();
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(u).openConnection();
            c.setConnectTimeout(8000);
            c.setReadTimeout(8000);
            c.setRequestMethod("GET");
            int code = c.getResponseCode();
            long ms = System.currentTimeMillis() - t0;
            return getString(R.string.ok) + " " + u + " · HTTP " + code + " · " + ms + " " + getString(R.string.ms);
        } catch (Exception e) {
            return getString(R.string.fail) + " " + u + " · " + e.getClass().getSimpleName();
        }
    }

    void check(String u) {
        result.setText(R.string.checking);
        new Thread(() -> { final String o = probe(u); ui.post(() -> result.setText(o)); }).start();
    }

    void checkAll() {
        result.setText(R.string.checking);
        new Thread(() -> {
            StringBuilder sb = new StringBuilder();
            sb.append(probe("https://github.com")).append('\n');
            sb.append(probe("https://google.com")).append('\n');
            sb.append(probe("https://chat.qwen.ai")).append('\n');
            sb.append(probe("https://appteka.store")).append('\n');
            sb.append(probe("https://t.me"));
            final String o = sb.toString();
            ui.post(() -> result.setText(o));
        }).start();
    }
}
