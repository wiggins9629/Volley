package com.wiggins.volley.listener;


import com.android.volley.VolleyError;
import com.wiggins.volley.bean.ResultDesc;

/**
 * @Description 自定义Http请求回调类
 * @Author 一花一世界
 */
public abstract class HttpListener {

    /**
     * Http请求成功时回调
     *
     * @param resultDesc 返回成功信息
     */
    public void onSuccess(ResultDesc resultDesc) {
    }

    /**
     * Http请求失败时回调
     *
     * @param volleyError 返回失败信息
     */
    public void onFailure(VolleyError volleyError) {
    }
}
