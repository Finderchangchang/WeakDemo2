package wai.weakdemo;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by Administrator on 2016/7/2.
 */
public class NoticeService extends Service {
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

    /**
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    private Runnable myTasks = new Runnable() {
        @Override
        public void run() {
            //间隔多久
            Message msg = myHandler.obtainMessage();
            msg.obj = 0;
            myHandler.sendMessage(msg);
            System.out.println("```````````2");
            mLocationClient.startLocation();
            myHandler.postDelayed(myTasks, 15000);
        }
    };

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
        //mLocationClient.startLocation();

        new Thread(myTasks).start();
        myHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };
    }

    @Override
    public void onStart(Intent intent, int startId) {
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CPUKeepRunning");
        wakeLock.acquire();
        super.onStart(intent, startId);
        //保持cpu一直运行，不管屏幕是否黑屏

    }
}
