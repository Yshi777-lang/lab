package ru.neon.checker;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.TextView;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends Activity {
    EditText urlBox;
    TextView result;
    Handler ui = new Handler(Looper.getMainLooper());

    static final String LISTS_URL = "https://raw.githubusercontent.com/Yshi777-lang/lab/main/checker-lists.json";
    volatile String[] white = {"https://yandex.ru", "https://gosuslugi.ru", "https://vk.com", "https://ozon.ru", "https://wildberries.ru", "https://sberbank.ru"};
    volatile String[] black = {"https://t.me", "https://discord.com", "https://x.com", "https://instagram.com", "https://facebook.com", "https://linkedin.com"};

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
        findViewById(R.id.btnWhite).setOnClickListener(v -> checkList(true));
        findViewById(R.id.btnBlack).setOnClickListener(v -> checkList(false));
        loadLists();
    }

    void loadLists() {
        new Thread(() -> {
            try {
                HttpURLConnection c = (HttpURLConnection) new URL(LISTS_URL).openConnection();
                c.setConnectTimeout(8000);
                c.setReadTimeout(8000);
                if (c.getResponseCode() == 200) {
                    java.io.InputStream in = c.getInputStream();
                    java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
                    byte[] buf = new byte[4096];
                    int n;
                    while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
                    JSONObject o = new JSONObject(bo.toString("UTF-8"));
                    JSONArray w = o.optJSONArray("white");
                    JSONArray k = o.optJSONArray("black");
                    if (w != null && w.length() > 0) white = toArray(w);
                    if (k != null && k.length() > 0) black = toArray(k);
                }
            } catch (Exception e) {
            }
        }).start();
    }

    String[] toArray(JSONArray a) throws Exception {
        String[] r = new String[a.length()];
        for (int i = 0; i < a.length(); i++) r[i] = a.getString(i);
        return r;
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

    String exitIp() {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL("https://api.ipify.org").openConnection();
            c.setConnectTimeout(8000);
            c.setReadTimeout(8000);
            java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(c.getInputStream()));
            String ip = r.readLine();
            return getString(R.string.exit_ip) + " " + (ip == null ? "-" : ip.trim());
        } catch (Exception e) {
            return getString(R.string.exit_ip) + " -";
        }
    }

    void checkList(boolean isWhite) {
        result.setText(R.string.checking);
        new Thread(() -> {
            String[] list = isWhite ? white : black;
            StringBuilder sb = new StringBuilder();
            sb.append(isWhite ? getString(R.string.white_rf) : getString(R.string.black));
            sb.append(" · ").append(exitIp()).append('\n');
            for (int i = 0; i < list.length; i++) {
                sb.append(probe(list[i]));
                if (i < list.length - 1) sb.append('\n');
            }
            final String o = sb.toString();
            ui.post(() -> result.setText(o));
        }).start();
    }
}
