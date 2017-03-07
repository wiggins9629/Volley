package com.wiggins.volley;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.wiggins.volley.adapter.TodayHistoryQueryAdapter;
import com.wiggins.volley.base.BaseActivity;
import com.wiggins.volley.bean.ResultDesc;
import com.wiggins.volley.bean.TodayHistoryQuery;
import com.wiggins.volley.http.HttpRequestUtil;
import com.wiggins.volley.listener.HttpListener;
import com.wiggins.volley.utils.Constant;
import com.wiggins.volley.utils.StringUtil;
import com.wiggins.volley.utils.ToastUtil;
import com.wiggins.volley.utils.UIUtils;
import com.wiggins.volley.widget.TitleView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private MainActivity mActivity = null;
    private TitleView titleView;
    private EditText mEdtData;
    private Button mBtnQuery;
    private TextView mTvEmpty;
    private ListView mLvData;

    private List<TodayHistoryQuery> todayHistoryQuery;
    private TodayHistoryQueryAdapter todayHistoryQueryAdapter;
    private Gson gson = null;
    private String data = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;

        initView();
        initData();
        setLinstener();
    }

    private void initData() {
        if (gson == null) {
            gson = new Gson();
        }
        if (todayHistoryQuery == null) {
            todayHistoryQuery = new ArrayList<>();
        }
        if (todayHistoryQueryAdapter == null) {
            todayHistoryQueryAdapter = new TodayHistoryQueryAdapter(todayHistoryQuery, mActivity);
            mLvData.setAdapter(todayHistoryQueryAdapter);
        } else {
            todayHistoryQueryAdapter.notifyDataSetChanged();
        }
        // 自定义Gson数据请求示例
        // HttpRequestUtil.requestGson(Request.Method.GET, "http://www.weather.com.cn/data/sk/101010100.html", Weather.class, null);
        // 自定义Xml数据请求示例
        // HttpRequestUtil.requestXml(Request.Method.GET, "http://flash.weather.com.cn/wmaps/xml/china.xml", null);
    }

    private void initView() {
        titleView = (TitleView) findViewById(R.id.titleView);
        titleView.setAppTitle(UIUtils.getString(R.string.event_list));
        titleView.setLeftImageVisibility(View.GONE);
        mEdtData = (EditText) findViewById(R.id.edt_data);
        mBtnQuery = (Button) findViewById(R.id.btn_query);
        mTvEmpty = (TextView) findViewById(R.id.tv_empty);
        mLvData = (ListView) findViewById(R.id.lv_data);
        mLvData.setEmptyView(mTvEmpty);
    }

    private void setLinstener() {
        mBtnQuery.setOnClickListener(this);
        mLvData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(mActivity, TodayHistoryDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("e_id", String.valueOf(todayHistoryQuery.get(position).getE_id()));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    /**
     * @Description 历史上的今天 事件列表
     */
    private void getTodayHistoryQuery() {
        Map<String, String> params = new HashMap<>();
        params.put("key", Constant.APP_KEY);
        params.put("date", data);
        HttpRequestUtil.getString(Constant.queryEvent, params, new HttpListener() {
            @Override
            public void onSuccess(ResultDesc resultDesc) {
                super.onSuccess(resultDesc);
                todayHistoryQuery.clear();
                if (resultDesc.getError_code() == 0) {
                    try {
                        JSONArray jsonArray = new JSONArray(resultDesc.getResult());
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                            TodayHistoryQuery bean = gson.fromJson(jsonObject.toString(), TodayHistoryQuery.class);
                            todayHistoryQuery.add(bean);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    todayHistoryQueryAdapter.setData(todayHistoryQuery);
                    Log.e(Constant.LOG_TAG, "历史上的今天 - 事件列表:" + todayHistoryQuery.toString());
                } else {
                    todayHistoryQueryAdapter.setData(todayHistoryQuery);
                    ToastUtil.showText(resultDesc.getReason());
                }
            }

            @Override
            public void onFailure(VolleyError volleyError) {
                super.onFailure(volleyError);
                ToastUtil.showText(volleyError.getMessage());
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_query:
                data = mEdtData.getText().toString().trim();
                if (StringUtil.isEmpty(data)) {
                    ToastUtil.showText(UIUtils.getString(R.string.query_date_not_empty));
                    return;
                }
                getTodayHistoryQuery();
                break;
        }
    }
}
