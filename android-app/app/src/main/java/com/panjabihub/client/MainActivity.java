package com.panjabihub.client;

import android.Manifest;
import android.app.*;
import android.app.job.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;\nimport java.net.URL;

public class MainActivity extends Activity {
    public static final String PREFS="panjabi_app";
    public static final String CHANNEL="orders";\n    private static final int CURRENT_VERSION_CODE=1;
    private WebView web;
    private Handler handler=new Handler(Looper.getMainLooper());
    private Runnable poller;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        createChannel();
        requestNotificationPermission();
        setupWeb();
        String base=getPreferencesBase();
        openApp(base);
        scheduleBackground(base);
        checkForUpdate(base);
    }

    private void setupWeb(){
        web=new WebView(this);
        web.setBackgroundColor(Color.WHITE);
        WebSettings s=web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        android.webkit.CookieManager.getInstance().setAcceptCookie(true);
        android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(web,true);
        web.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){
                Uri u=r.getUrl();
                String scheme=u.getScheme();
                if("tel".equals(scheme)||"mailto".equals(scheme)||"whatsapp".equals(scheme)){
                    startActivity(new Intent(Intent.ACTION_VIEW,u));
                    return true;
                }
                return false;
            }
            @Override public void onPageFinished(WebView v,String url){
                android.webkit.CookieManager.getInstance().flush();
                String cookie=android.webkit.CookieManager.getInstance().getCookie(getPreferencesBase());
                if(cookie!=null)getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString("session_cookie",cookie).apply();
            }
        });
        web.setWebChromeClient(new WebChromeClient());
        setContentView(web);
    }

    private String getPreferencesBase(){
        return "https://chitrakote.com/shop";
    }

    private void openApp(String base){
        web.loadUrl(base+"/portal/app.php");
        startForegroundPolling(base);
    }

    private String savedCookie(){
        return getSharedPreferences(PREFS,MODE_PRIVATE).getString("session_cookie","");
    }

    private void startForegroundPolling(String base){
        if(poller!=null)handler.removeCallbacks(poller);
        poller=new Runnable(){
            public void run(){
                new Thread(()->pollNotifications(base)).start();
                handler.postDelayed(this,60000);
            }
        };
        handler.postDelayed(poller,8000);
    }

    private void pollNotifications(String base){
        try{
            String cookie=savedCookie();
            if(cookie.isEmpty())return;
            int last=getSharedPreferences(PREFS,MODE_PRIVATE).getInt("last_notification",0);
            HttpURLConnection c=(HttpURLConnection)new URL(base+"/portal/api_notifications.php?after="+last).openConnection();
            c.setRequestProperty("Cookie",cookie);
            c.setConnectTimeout(8000);
            c.setReadTimeout(8000);
            if(c.getResponseCode()!=200)return;
            JSONObject root=new JSONObject(readAll(c.getInputStream()));
            org.json.JSONArray a=root.optJSONArray("items");
            int max=last;
            if(a!=null)for(int i=0;i<a.length();i++){
                JSONObject n=a.getJSONObject(i);
                int id=n.optInt("id",0);
                if(id>max)max=id;
                showNotification(id,n.optString("title","Order update"),n.optString("message",""));
            }
            if(max>last)getSharedPreferences(PREFS,MODE_PRIVATE).edit().putInt("last_notification",max).apply();
        }catch(Exception ignored){}
    }

    private void checkForUpdate(String base){
        new Thread(()->{
            try{
                HttpURLConnection c=(HttpURLConnection)new URL(base+"/apps/android/version.php").openConnection();
                c.setConnectTimeout(8000);
                c.setReadTimeout(8000);
                if(c.getResponseCode()!=200)return;
                JSONObject j=new JSONObject(readAll(c.getInputStream()));
                int latest=j.optInt("version_code",1);
                int min=j.optInt("minimum_supported_code",1);
                boolean force=j.optBoolean("force_update",false)||CURRENT_VERSION_CODE<min;
                String url=j.optString("apk_url","");
                String ver=j.optString("version_name","");
                String notes=j.optString("release_notes","");
                if(j.optBoolean("apk_ready",false)&&latest>CURRENT_VERSION_CODE&&!url.isEmpty())
                    runOnUiThread(()->showUpdateDialog(ver,notes,url,force));
            }catch(Exception ignored){}
        }).start();
    }

    private void showUpdateDialog(String ver,String notes,String url,boolean force){
        AlertDialog.Builder b=new AlertDialog.Builder(this)
            .setTitle("App update available · "+ver)
            .setMessage((notes==null||notes.isEmpty())?"A newer Android app version is available.":notes)
            .setPositiveButton("Download Update",(d,w)->startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url))));
        if(!force)b.setNegativeButton("Later",null);
        AlertDialog d=b.create();
        d.setCancelable(!force);
        d.show();
    }

    private String readAll(InputStream in)throws IOException{
        BufferedReader r=new BufferedReader(new InputStreamReader(in));
        StringBuilder b=new StringBuilder();
        String line;
        while((line=r.readLine())!=null)b.append(line);
        return b.toString();
    }

    private void showNotification(int id,String title,String msg){
        Intent i=new Intent(this,MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi=PendingIntent.getActivity(this,id,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,CHANNEL):new Notification.Builder(this);
        b.setSmallIcon(android.R.drawable.ic_dialog_info)
         .setContentTitle(title)
         .setContentText(msg)
         .setStyle(new Notification.BigTextStyle().bigText(msg))
         .setAutoCancel(true)
         .setContentIntent(pi);
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(id,b.build());
    }

    private void createChannel(){
        if(Build.VERSION.SDK_INT>=26){
            NotificationChannel c=new NotificationChannel(CHANNEL,"Orders & Delivery",NotificationManager.IMPORTANCE_DEFAULT);
            c.setDescription("Order, delivery and account updates");
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);
        }
    }

    private void requestNotificationPermission(){
        if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},11);
    }

    private void scheduleBackground(String base){
        JobScheduler js=(JobScheduler)getSystemService(JOB_SCHEDULER_SERVICE);
        ComponentName cn=new ComponentName(this,NotificationPollJob.class);
        PersistableBundle x=new PersistableBundle();
        x.putString("base_url",base);
        JobInfo job=new JobInfo.Builder(4101,cn)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPeriodic(15*60*1000L)
            .setExtras(x)
            .build();
        js.schedule(job);
    }

    @Override public void onBackPressed(){
        if(web!=null&&web.canGoBack())web.goBack();
        else super.onBackPressed();
    }
}
