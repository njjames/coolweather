package com.coolweather.android;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by nj on 2018/2/23.
 */

public class ChooseAreaFragment extends Fragment {

    private static final int LEVEL_PROVINCE = 0;
    private static final int LEVEL_CITY = 1;
    private static final int LEVEL_COUNTY = 2;

    private Button mBackButton;
    private TextView mTitleText;
    private ListView mListView;
    private List<String> mDataList = new ArrayList<>();
    private ArrayAdapter<String> mAdapter;
    private int mCurrentLevel;
    private List<Province> mProvinceList;
    private Province mSelectProvince;
    private List<City> mCityList;
    private City mSelectCity;
    private ProgressDialog mProgressDialog;
    private List<County> mCountyList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //将我们自己定义的布局设置为Fragment的布局
        View view = inflater.inflate(R.layout.choose_area, container, false);
        //找到其中的控件，并设置为成员变量
        mBackButton = view.findViewById(R.id.back_button);
        mTitleText = view.findViewById(R.id.title_text);
        mListView = view.findViewById(R.id.list_view);
        //创建一个ArrayAdapter，然后设置给ListView，（这里是一个空的，在查询的时候在更新adapter）
        mAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, mDataList);
        mListView.setAdapter(mAdapter);
        //最后返回我们自定义的view
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //给ListView设置点击事件
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //如果当前是省的界面，那么就查询这个省所有的市
                if (mCurrentLevel == LEVEL_PROVINCE) {
                    //获取点击的省
                    mSelectProvince = mProvinceList.get(position);
                    //查询当前点击省的所有的市
                    queryCities();
                }else if(mCurrentLevel == LEVEL_CITY) { //如果当前是市的界面
                    //获取当前点击的市
                    mSelectCity = mCityList.get(position);
                    //查询当前点击市中的所有的县
                    queryCounties();
                }
            }
        });

        //给返回按钮添加处理逻辑
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentLevel == LEVEL_COUNTY) {
                    queryCities();
                }else if(mCurrentLevel == LEVEL_CITY) {
                    queryProvinces();
                }
            }
        });
        //查询所有的省
        queryProvinces();
    }

    /**
     * 查询所有省
     */
    private void queryProvinces() {
        //将标题设置为中国
        mTitleText.setText("中国");
        //把返回按钮设置隐藏
        mBackButton.setVisibility(View.GONE);
        //先从数据库中查询省的信息
        mProvinceList = DataSupport.findAll(Province.class);
        //如果数据库中查询到了就从数据库中获取，否则从服务器上获取
        if (mProvinceList.size() > 0) {
            mDataList.clear();
            //将省的名称添加到adapter对应的集合中
            for (Province province : mProvinceList) {
                mDataList.add(province.getProvinceName());
            }
            //通知adapter更新数据
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            //将当前界面设置为省
            mCurrentLevel = LEVEL_PROVINCE;
        }else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    /**
     * 根据当前选择的省，查询这个省所有的市
     */
    private void queryCities() {
        //设置标题内容为当前选择的省
        mTitleText.setText(mSelectProvince.getProvinceName());
        //设置返回按钮可见
        mBackButton.setVisibility(View.VISIBLE);
        //从数据库中查询当前省中所有的市
        mCityList = DataSupport.where("provinceId = ?", String.valueOf(mSelectProvince.getProvinceCode())).find(City.class);
        if (mCityList.size() > 0) {
            //必须先清空adapter集合中的内容
            mDataList.clear();
            for (City city : mCityList) {
                mDataList.add(city.getCityName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            mCurrentLevel = LEVEL_CITY;
        }else {
            String address = "http://guolin.tech/api/china/" + mSelectProvince.getProvinceCode();
            queryFromServer(address, "city");
        }
    }

    /**
     * 根据当前选择的市，查询这个市所有的县
     */
    private void queryCounties() {
        mTitleText.setText(mSelectCity.getCityName());
        mBackButton.setVisibility(View.VISIBLE);
        mCountyList = DataSupport.where("cityId = ?", String.valueOf(mSelectCity.getCityCode())).find(County.class);
        if (mCountyList.size() > 0) {
            mDataList.clear();
            for (County county : mCountyList) {
                mDataList.add(county.getCountyName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            mCurrentLevel = LEVEL_COUNTY;
        }else {
            String address = "http://guolin.tech/api/china/" + mSelectProvince.getProvinceCode() + "/" + mSelectCity.getCityCode();
            queryFromServer(address, "county");
        }
    }

    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                }else if("city".equals(type)) {
                    int provinceCode = mSelectProvince.getProvinceCode();
                    result = Utility.handleCityResponse(responseText, provinceCode);
                }else if("county".equals(type)) {
                    int cityCode = mSelectCity.getCityCode();
                    result = Utility.handleCountyResponse(responseText, cityCode);
                }
                //如果查询成功，数据已经存储到数据库中了，此时就去数据库中查询
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            }else if("city".equals(type)) {
                                queryCities();
                            }else if("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setTitle("加载中...");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }
}
