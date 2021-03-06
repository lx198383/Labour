package com.labour.lar.fragment;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.labour.lar.BaseFragment;
import com.labour.lar.Constants;
import com.labour.lar.MainActivity;
import com.labour.lar.R;
import com.labour.lar.activity.RegistActivity;
import com.labour.lar.cache.TokenCache;
import com.labour.lar.cache.UserCache;
import com.labour.lar.cache.UserSignCache;
import com.labour.lar.map.LocationManager;
import com.labour.lar.module.User;
import com.labour.lar.util.AjaxResult;
import com.labour.lar.util.StringUtils;
import com.labour.lar.widget.BottomSelectDialog;
import com.labour.lar.widget.ProgressDialog;
import com.labour.lar.widget.toast.AppMsg;
import com.labour.lar.widget.toast.AppToast;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.OnClick;

public class KaoqinFrag extends BaseFragment implements AMapLocationListener, GeocodeSearch.OnGeocodeSearchListener {

    @BindView(R.id.title_tv)
    TextView title_tv;
    @BindView(R.id.back_iv)
    TextView back_iv;
    @BindView(R.id.right_header_btn)
    TextView right_header_btn;

    BottomSelectDialog dialog;

    @BindView(R.id.location_tv)
    TextView location_tv;

    @BindView(R.id.sign_btn)
    Button sign_btn;

    private LocationManager locationManager;
    private LatLonPoint latLonPoint;
    private String[] loginInTime;
    private String[] logoOutTime;
    /**
     *  1：签到
     *  2：签退
     */
    private int signState;

    @Override
    public int getFragmentLayoutId() {
        return R.layout.frag_kaoqin;
    }

    @Override
    public void initView() {
        back_iv.setVisibility(View.INVISIBLE);
        title_tv.setText("考勤打卡");
        Drawable d = getResources().getDrawable(R.mipmap.more_icon);
        right_header_btn.setCompoundDrawablesWithIntrinsicBounds(d,null,null,null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        locationManager = new LocationManager(context,true,this,this);
        if(locationManager.createClientIfNeeded()){
            locationManager.startLocation();
        }

        loadSignTime();
    }

    @OnClick({R.id.right_header_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.right_header_btn:
                showMoreDialog();
                break;
            case R.id.sign_btn:
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.release();
    }

    private void showMoreDialog(){
         dialog = new BottomSelectDialog(context,new BottomSelectDialog.BottomSelectDialogListener() {
            @Override
            public int getLayout() {
                return R.layout.menu_kaoqin;
            }
            @Override
            public void initView(View view) {
                View.OnClickListener onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int id = v.getId();
                        if(id == R.id.refresh_btn){
                            location_tv.setText("正在重新定位，请稍后...");
                            locationManager.startLocation();
                        } else if(id == R.id.refresh_sign_btn){
                            loadSignTime();
                        }

                        dialog.dismiss();
                    }
                };

                View refresh_btn = view.findViewById(R.id.refresh_btn);
                View refresh_sign_btn = view.findViewById(R.id.refresh_sign_btn);
                refresh_btn.setOnClickListener(onClickListener);
                refresh_sign_btn.setOnClickListener(onClickListener);
            }
            @Override
            public void onClick(Dialog dialog, int rate) {

            }
        });
        dialog.showAtLocation(mRootView,Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if(amapLocation != null) {
            Log.e("amap", "KaoqinFrag 定位成功， lat: " + amapLocation.getLatitude() + " lon: " + amapLocation.getLongitude() + " locationType: " + amapLocation.getLocationType());
            if (amapLocation.getErrorCode() == 0) {
                latLonPoint = new LatLonPoint(amapLocation.getLatitude(), amapLocation.getLongitude());
                locationManager.getAddress(latLonPoint);
            }else {
                location_tv.setText("定位失败，请重试！");
            }
        } else {
            location_tv.setText("定位失败，请重试！");
        }
    }

    /**
     * 逆地理编码回调
     */
    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getRegeocodeAddress() != null
                    && result.getRegeocodeAddress().getFormatAddress() != null) {
                String addressName = result.getRegeocodeAddress().getFormatAddress()
                        + "附近";
                location_tv.setText(addressName);
            } else {
                AppToast.show(context, "定位失败！");
            }
        } else {
            AppToast.show(context, "定位失败："+rCode+"！");
        }
    }

    /**
     * 地理编码查询回调
     */
    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    private void loadSignTime() {
        final ProgressDialog dialog = ProgressDialog.createDialog(context);
        dialog.show();

        getTime(1, new OnLoadTimeListener() {
            @Override
            public void onSuccess(String time) {
                loginInTime = time.split("-");
                getTime(2, new OnLoadTimeListener() {
                    @Override
                    public void onSuccess(String time) {
                        dialog.dismiss();
                        logoOutTime = time.split("-");

                        signState = getSignState();
                        sign_btn.setTag(signState);
                        if(signState == 1){
                            sign_btn.setEnabled(true);
                            sign_btn.setText("签到");
                        } else if(signState == 2){
                            sign_btn.setEnabled(true);
                            sign_btn.setText("签退");
                        } else {
                            sign_btn.setEnabled(false);
                            sign_btn.setText("签到");
                        }
                    }
                    @Override
                    public void onError() {
                        dialog.dismiss();
                    }
                });
            }
            @Override
            public void onError() {
                dialog.dismiss();
            }
        });
    }


    //获取签到时间
    public void getTime(int id,OnLoadTimeListener loadTimeCallback) {
        TokenCache tokenCache = TokenCache.getInstance(context);
        final Map<String,String> param = new HashMap<>();
        param.put("token",tokenCache.get());
        param.put("id",id+"");
        String jsonParams = JSON.toJSONString(param);

        String url = Constants.HTTP_BASE + "/api/sysdict";
        OkGo.<String>post(url).upJson(jsonParams).tag("request_tag").execute(new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                AjaxResult jr = new AjaxResult(response.body());
                if(jr.getSuccess() == 1){
                    JSONObject jo = jr.getData();
                    String loginInTime = jo.getString("dictvalue");
                    loadTimeCallback.onSuccess(loginInTime);
                } else {
                    AppToast.show(context,"获取数据出错!");
                    loadTimeCallback.onError();
                }
            }
            @Override
            public void onError(Response<String> response) {
                loadTimeCallback.onError();
                AppToast.show(context,"获取数据出错!");
            }
        });
    }

    /**
     * 1：签到
     * 2：签退
     * 取当天时间进行比较：
     * 签到开始时间到签退开始时间都能签到
     * 签退开始时间以后到能签退
     */
    private int getSignState() {
        int signState = 0;
        try {
            Calendar cali1 = formatTime(loginInTime[0]);//签到开始时间
            Calendar cali2 = formatTime(loginInTime[1]);//签到结束时间

            Calendar calo1 = formatTime(logoOutTime[0]);//签退开始时间
            Calendar calo2 = formatTime(logoOutTime[1]);//签退结束时间

            Calendar now = Calendar.getInstance();
            now.setTimeZone(TimeZone.getDefault());
            if(now.after(cali1) && now.before(calo1)) {
                signState = 1;
            } else if(now.after(calo1)){
                signState = 2;
            }

        } catch (ParseException e){
            e.printStackTrace();
        }
        return signState;
    }

    private Calendar formatTime(String time) throws ParseException {
        SimpleDateFormat sf1 = new SimpleDateFormat("yyyy-MM-dd");
        String datestr = sf1.format(new Date());
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = sf.parse(datestr + " " + time +":00");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.clear(Calendar.MILLISECOND);
        Log.i("kaoqin",sf.format(cal.getTime()));
        return cal;
    }

    public static interface OnLoadTimeListener {
        public void onSuccess(String time);
        public void onError();
    }

    private void signInOut(){

        UserCache userCache = UserCache.getInstance(context);
        if(userCache.get() == null){
            AppToast.show(context,Constants.LOGIN_ERROR_TIP);
            return;
        }
        if(latLonPoint == null){
            AppToast.show(context,"定位信息获取失败，请重新获取！");
            return;
        }
        SimpleDateFormat sf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sf2 = new SimpleDateFormat("HH:mm:ss");
        String clockdate = sf1.format(new Date());
        String clockintime = sf2.format(new Date());

        User user =  userCache.get();
        int userId =user.getId();
        Constants.ROLE role = user.getRole();
        int employee_id = 0;
        int staff_id = 0;
        int manager_id = 0;
        if(role == Constants.ROLE.employee){//工人
            employee_id = 0;
        } else if(role == Constants.ROLE.staff){
            staff_id = 0;
        } else if(role == Constants.ROLE.manager){
            manager_id = 0;
        }

        final Map<String,String> param = new HashMap<>();
        param.put("usertype",user.getProle());
        param.put("userid",userId+"");
        param.put("clockdate",clockdate);
        param.put("clockintime",clockintime);
        param.put("clockingeo",latLonPoint.getLongitude()+"," + latLonPoint.getLatitude());
        param.put("employee_id",employee_id+"");//工人id
        param.put("staff_id",staff_id+"");//作业队成员id
        param.put("manager_id",manager_id+"");//项目成员id
        String jsonParams = JSON.toJSONString(param);

        String url = Constants.HTTP_BASE + "/api/clockin";
        if(signState == 2){
            url = Constants.HTTP_BASE + "/api/clockout";
        }
        ProgressDialog dialog = ProgressDialog.createDialog(context);
        dialog.show();

        OkGo.<String>post(url).upJson(jsonParams).tag("request_tag").execute(new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                dialog.dismiss();
                AjaxResult jr = new AjaxResult(response.body());
                if(jr.getSuccess() == 1){
                    JSONObject jo = jr.getData();
                    AppToast.show(context,"操作成功!");

                    Intent intent = new Intent();
                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context) ;
                    intent.setAction(Constants.SIGN_CHECK_IN_OUT_RECEIVER_ACTION);
                    intent.putExtra("signState",signState);
                    localBroadcastManager.sendBroadcast(intent) ;
                } else {
                    AppToast.show(context,jr.getMsg());
                }
            }
            @Override
            public void onError(Response<String> response) {
                dialog.dismiss();
                AppToast.show(context,"操作失败!");
            }
        });
    }
}
