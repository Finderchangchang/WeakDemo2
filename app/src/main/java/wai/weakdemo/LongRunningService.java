package wai.weakdemo;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;

import java.util.Date;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by Finder丶畅畅 on 2017/11/6 07:12
 * QQ群481606175
 */

public class LongRunningService extends Service {
    private Handler myHandler = new Handler();
    private String httpResult = "0";
    private PowerManager pm;
    private PowerManager.WakeLock wakeLock;
    public AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
    //声明AMapLocationClient类对象
    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            System.out.println("```````````" + aMapLocation.getLatitude() + ":" + aMapLocation.getLongitude());
            mLocationClient.stopLocation();
            OkGo.post("http://kuaipao.myejq.com/App/Android/Deliver/AddLatLng.aspx")
                    .params("did", "274")
                    .params("lat", aMapLocation.getLatitude())
                    .params("lng", aMapLocation.getLongitude())
                    .params("Glat", aMapLocation.getLatitude())
                    .params("Glng", aMapLocation.getLongitude())
                    .execute(new AbsCallback<String>() {
                        @Override
                        public String convertSuccess(Response response) throws Exception {
                            return null;
                        }

                        @Override
                        public void onSuccess(String s, Call call, Response response) {
                            System.out.println("```````````3" + s);
                        }
                    });
        }
    };
    //初始化定位
    AMapLocationClient mLocationClient;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLocationClient = new AMapLocationClient(this);
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationClient.setLocationOption(mLocationOption);
        mLocationOption.setOnceLocation(true);
        mLocationOption.setOnceLocationLatest(false);
        mLocationOption.setNeedAddress(false);
        mLocationOption.setLocationCacheEnable(false);
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CPUKeepRunning");
        wakeLock.acquire();
        Notification notification = new Notification();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        //notification.setLatestEventInfo(this, "My title", "My content", pendingIntent);
        //把该service创建为前台service
        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                wakeLock.acquire();
                mLocationClient.startLocation();
            }
        }).start();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 10 * 1000; // 这是一小时的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            manager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        }else {
            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
