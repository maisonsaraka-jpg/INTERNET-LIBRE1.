package com.internetlibre.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.*;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.content.Context;
import android.os.Build;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private final int ORANGE = Color.rgb(244, 126, 32);
    private final int DARK = Color.rgb(45, 45, 45);
    private Button actionButton, optimizeButton, speedButton;
    private TextView statusText, detailsText, signalText, monitorText;
    private TextView pingValue, downloadValue, uploadValue, historyText;
    private Spinner operatorSpinner;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ArrayList<String> history = new ArrayList<>();
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        buildInterface();
        startNetworkMonitoring();
        updateSignalInfo();
    }

    private TextView metric(String label) {
        TextView t = new TextView(this);
        t.setText(label);
        t.setTextColor(DARK);
        t.setTextSize(15);
        t.setGravity(Gravity.CENTER);
        t.setPadding(4, 10, 4, 10);
        t.setBackgroundColor(Color.rgb(255, 246, 238));
        return t;
    }

    private void buildInterface() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(22, 24, 22, 20);
        root.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("INTERNET LIBRE");
        title.setTextColor(ORANGE);
        title.setTextSize(29);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView subtitle = new TextView(this);
        subtitle.setText("Votre assistant de stabilité Internet");
        subtitle.setTextColor(DARK);
        subtitle.setTextSize(14);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(-1, -2);
        subParams.setMargins(0, 5, 0, 16);
        root.addView(subtitle, subParams);

        TextView label = new TextView(this);
        label.setText("CHOISIR VOTRE OPÉRATEUR");
        label.setTextColor(DARK);
        label.setTextSize(13);
        label.setTypeface(null, Typeface.BOLD);
        root.addView(label);

        operatorSpinner = new Spinner(this);
        String[] operators = {"ORANGE CI", "MTN CI", "MOOV CI"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, operators);
        operatorSpinner.setAdapter(adapter);
        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(-1, 48);
        spinnerParams.setMargins(0, 2, 0, 10);
        root.addView(operatorSpinner, spinnerParams);

        actionButton = new Button(this);
        actionButton.setText("ILLIMITÉ");
        actionButton.setTextSize(21);
        actionButton.setTextColor(Color.WHITE);
        actionButton.setAllCaps(false);
        actionButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ORANGE));
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(210, 130);
        buttonParams.gravity = Gravity.CENTER;
        root.addView(actionButton, buttonParams);

        statusText = new TextView(this);
        statusText.setText("Prêt à tester votre connexion");
        statusText.setTextColor(DARK);
        statusText.setTextSize(16);
        statusText.setTypeface(null, Typeface.BOLD);
        statusText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(-1, -2);
        statusParams.setMargins(0, 8, 0, 3);
        root.addView(statusText, statusParams);

        detailsText = new TextView(this);
        detailsText.setText("Appuyez sur ILLIMITÉ pour vérifier l'accès à Internet.");
        detailsText.setTextColor(Color.GRAY);
        detailsText.setTextSize(12);
        detailsText.setGravity(Gravity.CENTER);
        root.addView(detailsText);

        TextView dashTitle = new TextView(this);
        dashTitle.setText("TABLEAU DE BORD RÉSEAU");
        dashTitle.setTextColor(ORANGE);
        dashTitle.setTextSize(16);
        dashTitle.setTypeface(null, Typeface.BOLD);
        dashTitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams dashParams = new LinearLayout.LayoutParams(-1, -2);
        dashParams.setMargins(0, 18, 0, 8);
        root.addView(dashTitle, dashParams);

        LinearLayout metrics = new LinearLayout(this);
        metrics.setOrientation(LinearLayout.HORIZONTAL);
        pingValue = metric("PING\n-- ms");
        downloadValue = metric("DOWNLOAD\n-- Mbps");
        uploadValue = metric("UPLOAD\n-- Mbps");
        LinearLayout.LayoutParams metricParams = new LinearLayout.LayoutParams(0, -2, 1);
        metricParams.setMargins(3, 0, 3, 0);
        metrics.addView(pingValue, metricParams);
        metrics.addView(downloadValue, metricParams);
        metrics.addView(uploadValue, metricParams);
        root.addView(metrics);

        speedButton = new Button(this);
        speedButton.setText("TESTER LE DÉBIT");
        speedButton.setTextColor(Color.WHITE);
        speedButton.setAllCaps(false);
        speedButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ORANGE));
        LinearLayout.LayoutParams speedParams = new LinearLayout.LayoutParams(-1, 48);
        speedParams.setMargins(0, 9, 0, 4);
        root.addView(speedButton, speedParams);

        signalText = new TextView(this);
        signalText.setText("Signal : analyse en cours…");
        signalText.setTextColor(DARK);
        signalText.setTextSize(13);
        signalText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams signalParams = new LinearLayout.LayoutParams(-1, -2);
        signalParams.setMargins(0, 8, 0, 2);
        root.addView(signalText, signalParams);

        monitorText = new TextView(this);
        monitorText.setText("Surveillance réseau : active");
        monitorText.setTextColor(ORANGE);
        monitorText.setTextSize(12);
        monitorText.setGravity(Gravity.CENTER);
        root.addView(monitorText);

        optimizeButton = new Button(this);
        optimizeButton.setText("OPTIMISER MON RÉSEAU");
        optimizeButton.setTextColor(Color.WHITE);
        optimizeButton.setAllCaps(false);
        optimizeButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ORANGE));
        LinearLayout.LayoutParams optParams = new LinearLayout.LayoutParams(-1, 48);
        optParams.setMargins(0, 8, 0, 5);
        root.addView(optimizeButton, optParams);

        TextView historyTitle = new TextView(this);
        historyTitle.setText("HISTORIQUE DES TESTS");
        historyTitle.setTextColor(ORANGE);
        historyTitle.setTextSize(14);
        historyTitle.setTypeface(null, Typeface.BOLD);
        historyTitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams histTitleParams = new LinearLayout.LayoutParams(-1, -2);
        histTitleParams.setMargins(0, 14, 0, 4);
        root.addView(historyTitle, histTitleParams);

        historyText = new TextView(this);
        historyText.setText("Aucun test de débit effectué.");
        historyText.setTextColor(Color.GRAY);
        historyText.setTextSize(12);
        historyText.setGravity(Gravity.CENTER);
        root.addView(historyText);

        TextView note = new TextView(this);
        note.setText("Les tests de débit consomment des données mobiles. Le résultat dépend du serveur de test, du téléphone et du réseau. Internet libre ne crée pas de forfait illimité et ne peut pas augmenter la puissance de l'antenne.");
        note.setTextColor(Color.GRAY);
        note.setTextSize(10);
        note.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(-1, -2);
        noteParams.setMargins(0, 14, 0, 0);
        root.addView(note, noteParams);

        scroll.addView(root);
        setContentView(scroll);
        actionButton.setOnClickListener(v -> startTest());
        optimizeButton.setOnClickListener(v -> showOptimizationAdvice());
        speedButton.setOnClickListener(v -> startSpeedTest());
    }

    private boolean internetReachable() {
        HttpURLConnection connection = null;
        try {
            long start = System.currentTimeMillis();
            URL url = new URL("https://www.google.com/generate_204");
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(2500);
            connection.setReadTimeout(2500);
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            int code = connection.getResponseCode();
            return code == 204 || (code >= 200 && code < 400);
        } catch (Exception e) {
            return false;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private long pingTest() {
        HttpURLConnection c = null;
        try {
            long start = System.currentTimeMillis();
            c = (HttpURLConnection) new URL("https://www.google.com/generate_204").openConnection();
            c.setConnectTimeout(4000);
            c.setReadTimeout(4000);
            c.setUseCaches(false);
            c.getResponseCode();
            return System.currentTimeMillis() - start;
        } catch (Exception e) {
            return -1;
        } finally {
            if (c != null) c.disconnect();
        }
    }

    private double downloadTest() {
        HttpURLConnection c = null;
        try {
            URL url = new URL("https://speed.cloudflare.com/__down?bytes=3000000");
            c = (HttpURLConnection) url.openConnection();
            c.setConnectTimeout(8000);
            c.setReadTimeout(12000);
            long start = System.nanoTime();
            byte[] buffer = new byte[8192];
            int count, total = 0;
            java.io.InputStream in = c.getInputStream();
            while ((count = in.read(buffer)) != -1) total += count;
            in.close();
            double seconds = (System.nanoTime() - start) / 1_000_000_000.0;
            if (seconds <= 0 || total == 0) return -1;
            return (total * 8.0 / 1_000_000.0) / seconds;
        } catch (Exception e) {
            return -1;
        } finally {
            if (c != null) c.disconnect();
        }
    }

    private double uploadTest() {
        HttpURLConnection c = null;
        try {
            byte[] data = new byte[500 * 1024];
            URL url = new URL("https://speed.cloudflare.com/__up");
            c = (HttpURLConnection) url.openConnection();
            c.setConnectTimeout(8000);
            c.setReadTimeout(12000);
            c.setRequestMethod("POST");
            c.setDoOutput(true);
            c.setFixedLengthStreamingMode(data.length);
            c.setRequestProperty("Content-Type", "application/octet-stream");
            long start = System.nanoTime();
            OutputStream out = c.getOutputStream();
            out.write(data);
            out.flush();
            out.close();
            c.getResponseCode();
            double seconds = (System.nanoTime() - start) / 1_000_000_000.0;
            if (seconds <= 0) return -1;
            return (data.length * 8.0 / 1_000_000.0) / seconds;
        } catch (Exception e) {
            return -1;
        } finally {
            if (c != null) c.disconnect();
        }
    }

    private void startTest() {
        actionButton.setEnabled(false);
        actionButton.setText("TEST...");
        actionButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.RED));
        statusText.setText("Vérification en cours…");
        detailsText.setText("Tests de connexion pendant 5 secondes.");
        long start = System.currentTimeMillis();
        executor.execute(() -> {
            int successes = 0;
            long totalMs = 0;
            for (int i = 0; i < 3; i++) {
                long t = System.currentTimeMillis();
                if (internetReachable()) { successes++; totalMs += System.currentTimeMillis() - t; }
                try { Thread.sleep(350); } catch (InterruptedException ignored) {}
            }
            final int ok = successes;
            final long avg = successes == 0 ? -1 : totalMs / successes;
            long remaining = Math.max(0, 5000 - (System.currentTimeMillis() - start));
            handler.postDelayed(() -> showResult(ok, avg), remaining);
        });
    }

    private void showResult(int successes, long averageMs) {
        actionButton.setEnabled(true);
        actionButton.setText("ILLIMITÉ");
        if (successes == 3) {
            actionButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(40, 160, 75)));
            statusText.setText("CONNEXION DISPONIBLE");
            detailsText.setText("3 tests réussis • Réponse moyenne : " + averageMs + " ms\nOpérateur choisi : " + operatorSpinner.getSelectedItem());
        } else {
            actionButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.RED));
            statusText.setText("CONNEXION À VÉRIFIER");
            detailsText.setText(successes + "/3 tests réussis. Vérifiez vos données mobiles, le signal ou réessayez.\nOpérateur choisi : " + operatorSpinner.getSelectedItem());
        }
    }

    private void startSpeedTest() {
        speedButton.setEnabled(false);
        speedButton.setText("MESURE EN COURS...");
        pingValue.setText("PING\n...");
        downloadValue.setText("DOWNLOAD\n...");
        uploadValue.setText("UPLOAD\n...");
        executor.execute(() -> {
            long ping = pingTest();
            double down = downloadTest();
            double up = uploadTest();
            handler.post(() -> {
                speedButton.setEnabled(true);
                speedButton.setText("TESTER LE DÉBIT");
                pingValue.setText("PING\n" + (ping < 0 ? "--" : ping + " ms"));
                downloadValue.setText("DOWNLOAD\n" + formatSpeed(down));
                uploadValue.setText("UPLOAD\n" + formatSpeed(up));
                String record = operatorSpinner.getSelectedItem() + " : Ping " +
                        (ping < 0 ? "--" : ping + " ms") + " | ↓ " + formatSpeed(down) +
                        " | ↑ " + formatSpeed(up);
                history.add(0, record);
                while (history.size() > 5) history.remove(history.size() - 1);
                StringBuilder sb = new StringBuilder();
                for (String item : history) sb.append("• ").append(item).append("\n");
                historyText.setText(sb.toString());
            });
        });
    }

    private String formatSpeed(double speed) {
        return speed < 0 ? "-- Mbps" : String.format(Locale.US, "%.2f Mbps", speed);
    }

    private void showOptimizationAdvice() {
        updateSignalInfo();
        String advice = "• Rapprochez-vous d'une fenêtre ou d'un endroit dégagé.\n• Activez puis désactivez le mode avion après quelques secondes.\n• Vérifiez que les données mobiles et votre forfait sont actifs.\n• Si le problème persiste, essayez un autre emplacement.";
        new android.app.AlertDialog.Builder(this)
                .setTitle("Conseils d'optimisation")
                .setMessage(advice + "\n\nL'application ne modifie pas directement les paramètres de l'opérateur.")
                .setPositiveButton("Compris", null).show();
    }

    private void startNetworkMonitoring() {
        if (connectivityManager == null) return;
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override public void onAvailable(Network network) {
                handler.post(() -> {
                    monitorText.setText("Surveillance réseau : connexion détectée");
                    monitorText.setTextColor(ORANGE);
                    updateSignalInfo();
                });
            }
            @Override public void onLost(Network network) {
                handler.post(() -> {
                    monitorText.setText("ALERTE : connexion perdue");
                    monitorText.setTextColor(Color.RED);
                    signalText.setText("Signal : réseau indisponible");
                });
            }
            @Override public void onCapabilitiesChanged(Network network, NetworkCapabilities caps) {
                handler.post(() -> {
                    monitorText.setText("Surveillance réseau : connexion active");
                    monitorText.setTextColor(ORANGE);
                    updateSignalInfo();
                });
            }
        };
        try {
            connectivityManager.registerNetworkCallback(
                    new NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(),
                    networkCallback);
        } catch (Exception ignored) {}
    }

    private void updateSignalInfo() {
        if (connectivityManager == null) return;
        try {
            Network active = connectivityManager.getActiveNetwork();
            NetworkCapabilities caps = active == null ? null : connectivityManager.getNetworkCapabilities(active);
            if (caps == null || !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                signalText.setText("Signal : aucune connexion Internet détectée");
                return;
            }
            String type = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ? "Wi-Fi" :
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ? "Réseau mobile" : "Réseau";
            if (Build.VERSION.SDK_INT >= 29) {
                int level = caps.getSignalStrength();
                signalText.setText(type + (level == NetworkCapabilities.SIGNAL_STRENGTH_UNSPECIFIED ?
                        " • Niveau de signal non communiqué" : " • Indicateur de signal : " + level));
            } else signalText.setText(type + " • Niveau de signal non disponible sur cette version Android");
        } catch (Exception e) {
            signalText.setText("Signal : information non disponible");
        }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (connectivityManager != null && networkCallback != null) {
            try { connectivityManager.unregisterNetworkCallback(networkCallback); } catch (Exception ignored) {}
        }
        executor.shutdownNow();
    }
}
