package com.panjabihub.client;

import android.app.*;
import android.app.job.*;
import android.content.*;
import org.json.*;
import java.io.*;
import java.net.*;

public class NotificationPollJob extends JobService {
    @Override public boolean onStartJob(JobParameters p){
        new Thread(()->{
            try{poll(p.getExtras().getString("base_url",""));}
            catch(Exception ignored){}
            finally{jobFinished(p,false);}
        }).start();
        return true;
    }

    @Override public boolean onStopJob(JobParameters p){return true;}

    private void poll(String base)throws Exception{
        if(base==null||base.isEmpty())return;
        SharedPreferences prefs=getSharedPreferences(MainActivity.PREFS,MODE_PRIVATE);
        String cookie=prefs.getString("session_cookie","");
        if(cookie.isEmpty())return;
        int last=prefs.getInt("last_notification",0);

        HttpURLConnection c=(HttpURLConnection)new URL(base+"/portal/api_notifications.php?after="+last).openConnection();
        c.setRequestProperty("Cookie",cookie);
        c.setConnectTimeout(8000);
        c.setReadTimeout(8000);
        if(c.getResponseCode()!=200)return;

        BufferedReader r=new BufferedReader(new InputStreamReader(c.getInputStream()));
        StringBuilder sb=new StringBuilder();
        String line;
        while((line=r.readLine())!=null)sb.append(line);

        JSONObject root=new JSONObject(sb.toString());
        JSONArray a=root.optJSONArray("items");
        int max=last;
        if(a!=null)for(int i=0;i<a.length();i++){
            JSONObject n=a.getJSONObject(i);
            int id=n.optInt("id",0);
            if(id>max)max=id;
            notifyUser(id,n.optString("title","Order update"),n.optString("message",""));
        }
        if(max>last)prefs.edit().putInt("last_notification",max).apply();
    }

    private void notifyUser(int id,String title,String msg){
        NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if(android.os.Build.VERSION.SDK_INT>=26&&nm.getNotificationChannel(MainActivity.CHANNEL)==null)
            nm.createNotificationChannel(new NotificationChannel(MainActivity.CHANNEL,"Orders & Delivery",NotificationManager.IMPORTANCE_DEFAULT));

        Intent i=new Intent(this,MainActivity.class);
        PendingIntent pi=PendingIntent.getActivity(this,id,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder b=android.os.Build.VERSION.SDK_INT>=26?new Notification.Builder(this,MainActivity.CHANNEL):new Notification.Builder(this);
        b.setSmallIcon(android.R.drawable.ic_dialog_info)
         .setContentTitle(title)
         .setContentText(msg)
         .setStyle(new Notification.BigTextStyle().bigText(msg))
         .setAutoCancel(true)
         .setContentIntent(pi);
        nm.notify(id,b.build());
    }
}
