package wai.weakdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;

import okhttp3.Call;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    //5346755eb630edc048d895b9d812dadf
    public AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
    //声明AMapLocationClient类对象
    //初始化定位
    AMapLocationClient mLocationClient;
    private JobSchedulerManager mJobManager;

    // 动态注册锁屏等广播
    private ScreenReceiverUtil mScreenListener;
    // 1像素Activity管理类
    private ScreenManager mScreenManager;
    // 代码省略...
    private ScreenReceiverUtil.SreenStateListener
            mScreenListenerer = new ScreenReceiverUtil.SreenStateListener() {
        @Override
        public void onSreenOn() {
            // 移除"1像素"
            mScreenManager.finishActivity();
        }

        @Override
        public void onSreenOff() {
            // 接到锁屏广播，将SportsActivity切换到可见模式
            // "咕咚"、"乐动力"、"悦动圈"就是这么做滴
            // Intent intent =
            //new Intent(SportsActivity.this,SportsActivity.class);
            // startActivity(intent);
            // 如果你觉得，直接跳出SportActivity很不爽
            // 那么，我们就制造个"1像素"惨案
            mScreenManager.startActivity();
        }

        @Override
        public void onUserPresent() {
            // 解锁，暂不用，保留
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocationClient = new AMapLocationClient(this);
        startService(new Intent(this, LongRunningService.class));
        //startService(new Intent(this, PlayerMusicService.class));

        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationClient.setLocationOption(mLocationOption);
        //mLocationOption.setOnceLocation(true);
        //设置定位回调监听
        mLocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
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

        });
        //mLocationClient.startLocation();
        //mJobManager = JobSchedulerManager.getJobSchedulerInstance(this);
        //mJobManager.startJobScheduler();
        mScreenListener = new ScreenReceiverUtil(this);
        mScreenManager = ScreenManager.getScreenManagerInstance(this);
        mScreenListener.setScreenReceiverListener(mScreenListenerer);
    }

    @Override
    protected void onDestroy() {
        if (Contants.DEBUG)
            Log.d("", "onDestroy--->1像素保活被终止");
        if (!SystemUtils.isAPPALive(this, Contants.PACKAGE_NAME)) {
            Intent intentAlive = new Intent(this, MainActivity.class);
            intentAlive.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentAlive);
            Log.i("", "SinglePixelActivity---->APP被干掉了，我要重启它");
        }
        super.onDestroy();
    }
}
