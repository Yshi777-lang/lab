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
    }

    void check(String u) {
        if (!u.startsWith("http")) u = "https://" + u;
        final String fu = u;
        result.setText(R.string.checking);
        long t0 = System.currentTimeMillis();
        new Thread(() -> {
            String out;
            try {
                HttpURLConnection c = (HttpURLConnection) new URL(fu).openConnection();
                c.setConnectTimeout(8000);
                c.setReadTimeout(8000);
                c.setRequestMethod("GET");
                int code = c.getResponseCode();
                long ms = System.currentTimeMillis() - t0;
                out = getString(R.string.ok) + " " + fu + "\nHTTP " + code + " · " + ms + " " + getString(R.string.ms);
            } catch (Exception e) {
                out = getString(R.string.fail) + " " + fu + "\n" + e.getClass().getSimpleName();
            }
            final String o = out;
            ui.post(() -> result.setText(o));
        }).start();
    }
}
