package com.wiggins.volley.http;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.NetworkOnMainThreadException;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.wiggins.volley.R;
import com.wiggins.volley.base.BaseApplication;
import com.wiggins.volley.bean.ResultDesc;
import com.wiggins.volley.bean.Weather;
import com.wiggins.volley.bean.WeatherInfo;
import com.wiggins.volley.listener.HttpListener;
import com.wiggins.volley.utils.BitmapCache;
import com.wiggins.volley.utils.Constant;
import com.wiggins.volley.utils.LogUtil;
import com.wiggins.volley.utils.StringUtil;
import com.wiggins.volley.utils.UIUtils;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description 网络请求封装
 * @Author 一花一世界
 */
public class HttpRequestUtil {

    public static RequestQueue requestQueue;//请求队列对象
    public static HttpListener httpListener;//自定义Http请求回调监听

    /**
     * @Description 上下文
     */
    public static Context getContext() {
        return BaseApplication.getContext();
    }

    /**
     * @Description 用于返回RequestQueue对象，如果为空则创建它
     */
    public static RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            synchronized (HttpRequestUtil.class) {
                if (requestQueue == null) {
                    // 建立Volley的Http请求队列
                    requestQueue = Volley.newRequestQueue(getContext());
                }
            }
        }
        return requestQueue;
    }

    /**
     * 通过ImageRequest来显示网络图片
     * 使用：
     * 1. 创建一个RequestQueue对象
     * 2. 创建一个ImageRequest对象
     * 3. 将Request对象添加到RequestQueue里面
     *
     * @param url       请求图片的地址
     * @param imageView 图片的容器ImageView
     */
    public static void setImageRequest(String url, final ImageView imageView) {
        //参数1：图片的URL地址
        //参数2：图片请求成功的回调，这里可以把返回的Bitmap参数设置到ImageView中
        //参数3和4：允许图片最大宽度和高度，如果指定网络图片的宽度或高度大于这里的最大值，则会对图片进行压缩，指定成0的话就表示不管图片有多大都不会进行压缩
        //参数5：图片的颜色属性，Bitmap.Config下的几个常量都可以在这里使用，其中ARGB_8888可以展示最好的颜色属性，每个图片像素占据4个字节的大小，而RGB_565则表示每个图片像素占据2个字节大小
        //参数6：图片请求失败的回调，这里可以设置当请求失败时在ImageView中显示一张默认图片
        ImageRequest imageRequest = new ImageRequest(url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap bitmap) {
                imageView.setImageBitmap(bitmap);
            }
        }, 0, 0, Bitmap.Config.RGB_565, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                imageView.setBackgroundResource(R.drawable.image_error);
            }
        });
        getRequestQueue().add(imageRequest);
    }

    /**
     * 通过ImageLoader来显示网络图片
     * 使用：
     * 1. 创建一个RequestQueue对象
     * 2. 创建一个ImageLoader对象
     * 3. 获取一个ImageListener对象
     * 4. 调用ImageLoader的get()方法加载网络上的图片
     * 特点：
     * 内部是使用ImageRequest来实现的，它不仅可以对图片进行缓存，还可以过滤掉重复的链接，避免重复发送请求。
     *
     * @param url       请求图片的地址
     * @param imageView 图片的容器ImageView
     */
    public static void setImageLoader(String url, ImageView imageView) {
        //参数1：RequestQueue对象
        //参数2：ImageCache对象
        ImageLoader loader = new ImageLoader(getRequestQueue(), new BitmapCache());
        //参数1：显示图片的ImageView控件
        //参数2：默认显示的图片
        //参数3：请求失败时显示的图片
        ImageLoader.ImageListener imageListener = ImageLoader.getImageListener(imageView, R.drawable.image_default, R.drawable.image_error);
        //参数1：图片的URL地址
        //参数2：ImageListener对象
        loader.get(url, imageListener);
        //参数1：图片的URL地址
        //参数2：ImageListener对象
        //参数3和4：图片允许的最大宽度和高度
        //loader.get(url, imageListener, 200, 200);
    }

    /**
     * 通过NetWorkImageView来显示网络图片
     * 使用：
     * 1. 创建一个RequestQueue对象
     * 2. 创建一个ImageLoader对象
     * 3. 在布局文件中添加一个NetworkImageView控件
     * 4. 在代码中获取该控件的实例
     * 5. 设置要加载的图片地址
     * 特点：
     * NetworkImageView是一个自定义控件，它继承自ImageView，具备ImageView控件的所有功能，并且在原生的基础之上加入了加载网络图片的功能。
     * 图片压缩：
     * NetworkImageView是一个控件，在加载图片的时候它会自动获取自身的宽高，然后对比网络图片的宽高，再决定是否需要对图片进行压缩。
     * 也就是说，压缩过程是在内部完全自动化的，并不需要我们关心，NetworkImageView会始终呈现给我们一张大小刚刚好的网络图片，不会多占用任何一点内存
     *
     * @param url              请求图片的地址
     * @param netWorkImageView 图片的容器NetworkImageView
     */
    public static void setNetWorkImageView(String url, NetworkImageView netWorkImageView) {
        ImageLoader loader = new ImageLoader(getRequestQueue(), new BitmapCache());
        netWorkImageView.setDefaultImageResId(R.drawable.image_default);
        netWorkImageView.setErrorImageResId(R.drawable.image_error);
        netWorkImageView.setImageUrl(url, loader);
    }

    /**
     * 示例请求
     *
     * @param url      访问服务器地址
     * @param tag      请求标签
     * @param params   请求参数
     * @param listener 自定义Http请求回调
     */
    public static void exampleString(String url, String tag, final Map<String, String> params, HttpListener listener) {
        // 创建当前请求对象
        StringRequest request = new StringRequest(Request.Method.POST, url, getListener(listener), getErrorListener(listener)) {

            // 请求参数
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }

            // 请求优先级
            @Override
            public Priority getPriority() {
                return Request.Priority.NORMAL;
            }

            // 请求头部
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("cookie", "your cookie");
                return headers;
            }
        };
        // 第一个代表超时时间：即超过20s认为超时，第三个参数代表最大重试次数，这里设置为1.0f代表如果超时则不重试
        request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 1, 1.0f));
        if (!StringUtil.isEmpty(tag)) {
            // 设置该请求的标签
            request.setTag(tag);
        }
        // 将请求添加到队列中
        getRequestQueue().add(request);
    }

    /**
     * get 请求
     *
     * @param url      访问服务器地址
     * @param listener 自定义Http请求回调
     */
    public static void getString(String url, HttpListener listener) {
        // 创建当前请求对象
        StringRequest request = new StringRequest(url, getListener(listener), getErrorListener(listener));
        // 第一个代表超时时间：即超过20s认为超时，第三个参数代表最大重试次数，这里设置为1.0f代表如果超时则不重试
        request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 1, 1.0f));
        // 将请求添加到队列中
        getRequestQueue().add(request);
    }

    /**
     * get 请求
     *
     * @param url      访问服务器地址
     * @param params   请求参数
     * @param listener 自定义Http请求回调
     */
    public static void getString(String url, final Map<String, String> params, HttpListener listener) {
        // 参数拼接
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        if (params != null && params.size() != 0) {
            for (String key : params.keySet()) {
                if (isFirst) {
                    sb.append(key + "=" + params.get(key));
                    isFirst = false;
                } else {
                    sb.append("&" + key + "=" + params.get(key));
                }
            }
            url += "?" + sb.toString();
        }
        // 创建当前请求对象
        StringRequest request = new StringRequest(url, getListener(listener), getErrorListener(listener));
        // 第一个代表超时时间：即超过20s认为超时，第三个参数代表最大重试次数，这里设置为1.0f代表如果超时则不重试
        request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 1, 1.0f));
        // 将请求添加到队列中
        getRequestQueue().add(request);
    }

    /**
     * post 请求
     *
     * @param url      访问服务器地址
     * @param params   请求参数
     * @param listener 自定义Http请求回调
     */
    public static void postString(String url, final Map<String, String> params, HttpListener listener) {
        // 创建当前请求对象
        StringRequest request = new StringRequest(Request.Method.POST, url, getListener(listener), getErrorListener(listener)) {

            // 请求参数
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };
        // 第一个代表超时时间：即超过20s认为超时，第三个参数代表最大重试次数，这里设置为1.0f代表如果超时则不重试
        request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 1, 1.0f));
        // 将请求添加到队列中
        getRequestQueue().add(request);
    }

    /**
     * JsonObjectRequest 请求
     *
     * @param method 请求方式
     * @param url    访问服务器地址
     * @param params 请求参数
     */
    public static void requestJsonObject(int method, String url, final Map<String, String> params) {
        // 创建当前请求对象
        JsonObjectRequest request = new JsonObjectRequest(method, url, params == null ? null : new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                //成功响应时回调此函数
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                //失败响应时回调此函数
            }
        });
        // 第一个代表超时时间：即超过20s认为超时，第三个参数代表最大重试次数，这里设置为1.0f代表如果超时则不重试
        request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 1, 1.0f));
        // 将请求添加到队列中
        getRequestQueue().add(request);
    }

    /**
     * JsonArrayRequest 请求
     *
     * @param url 访问服务器地址
     */
    public static void requestJsonArray(String url) {
        // 创建当前请求对象
        JsonArrayRequest request = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                //成功响应时回调此函数
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                //失败响应时回调此函数
            }
        });
        // 第一个代表超时时间：即超过20s认为超时，第三个参数代表最大重试次数，这里设置为1.0f代表如果超时则不重试
        request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 1, 1.0f));
        // 将请求添加到队列中
        getRequestQueue().add(request);
    }

    /**
     * 自定义GsonRequest<T> 请求
     *
     * @param method 请求方式，示例：Request.Method.POST
     * @param url    访问服务器地址
     * @param clazz  解析类
     * @param params 请求参数
     */
    public static <T> void requestGson(int method, String url, Class<T> clazz, final Map<String, String> params) {
        GsonRequest<T> request = new GsonRequest<T>(method, url, clazz, new Response.Listener<T>() {
            @Override
            public void onResponse(T t) {
                // Gson解析示例
                getGsonData(t);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };
        // 第一个代表超时时间：即超过20s认为超时，第三个参数代表最大重试次数，这里设置为1.0f代表如果超时则不重试
        request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 1, 1.0f));
        // 将请求添加到队列中
        getRequestQueue().add(request);
    }

    /**
     * 自定义XMLRequest请求
     *
     * @param method 请求方式，示例：Request.Method.POST
     * @param url    访问服务器地址
     * @param params 请求参数
     */
    public static void requestXml(int method, String url, final Map<String, String> params) {
        XMLRequest request = new XMLRequest(method, url, new Response.Listener<XmlPullParser>() {
            @Override
            public void onResponse(XmlPullParser xmlPullParser) {
                // XML解析示例
                getXmlPullParser(xmlPullParser);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };
        // 第一个代表超时时间：即超过20s认为超时，第三个参数代表最大重试次数，这里设置为1.0f代表如果超时则不重试
        request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 1, 1.0f));
        // 将请求添加到队列中
        getRequestQueue().add(request);
    }

    /**
     * @Description 请求缓存机制
     */
    public static void getCache(String url) {
        // 1.从缓存中读取请求:即先从缓存读取看是否有缓存数据，如果没有则请求网络数据
        Cache cache = getRequestQueue().getCache();
        Cache.Entry entry = cache.get(url);
        if (entry != null) {
            try {
                String data = new String(entry.data, "Utf-8");
                //处理data数据，将其转化为JSON，XML，Bitmap等等
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //缓存中不存在，做网络请求
        }
        // 2.缓存失效:它并不意味着删除缓存，Volley仍将使用缓存对象，直到服务器返回新数据，一旦接收到新数据将覆盖原来的缓存
        getRequestQueue().getCache().invalidate(url, true);
        // 3.关闭缓存:如果你想禁用特定Url的缓存可以使用以下方法
        getRequestQueue().getCache().remove(url);
        // 4.删除来自特定Url的缓存
        getRequestQueue().getCache().remove(url);
        // 5.删除所有缓存
        getRequestQueue().getCache().clear();
    }

    /**
     * @Description 取消网络请求
     */
    public static void cancelAll(Object tag) {
        if (tag != null) {
            getRequestQueue().cancelAll(tag);
        }
    }

    /**
     * @Description 重启当前请求队列
     */
    public static void start() {
        getRequestQueue().start();
    }

    /**
     * Http请求成功回调类
     *
     * @param listener 自定义Http请求回调
     * @return
     */
    public static Response.Listener<String> getListener(final HttpListener listener) {
        httpListener = listener;
        Response.Listener<String> mListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                httpListener.onSuccess(getReturnData(s));
            }
        };
        return mListener;
    }

    /**
     * Http请求失败回调类
     *
     * @param listener 自定义Http请求回调
     * @return
     */
    public static Response.ErrorListener getErrorListener(final HttpListener listener) {
        httpListener = listener;
        Response.ErrorListener mErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                httpListener.onFailure(volleyError);
            }
        };
        return mErrorListener;
    }

    /**
     * Gson数据解析
     *
     * @param t 泛型
     */
    public static <T> void getGsonData(T t) {
        Weather weather = (Weather) t;
        WeatherInfo weatherInfo = weather.getWeatherinfo();
        LogUtil.e(Constant.LOG_TAG, "GSON 数据解析: " + t.toString() + "\n"
                + "city->" + weatherInfo.getCity() + "  temp->" + weatherInfo.getTemp() + "  time->" + weatherInfo.getTime());
    }

    /**
     * XML数据解析
     *
     * @param xmlPullParser pull解析器
     */
    public static void getXmlPullParser(XmlPullParser xmlPullParser) {
        try {
            int eventType = xmlPullParser.getEventType();
            StringBuilder sb = new StringBuilder();
            boolean isFirst = true;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        String nodeName = xmlPullParser.getName();
                        if ("city".equals(nodeName)) {
                            String pName = xmlPullParser.getAttributeValue(0);
                            if (isFirst) {
                                sb.append(pName);
                                isFirst = false;
                            } else {
                                sb.append("," + pName);
                            }
                        }
                        break;
                }
                eventType = xmlPullParser.next();
            }
            LogUtil.e(Constant.LOG_TAG, "XML 数据解析: " + sb.toString());
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回数据解析
     *
     * @param result 请求返回字符串
     * @return
     */
    public static ResultDesc getReturnData(String result) {
        ResultDesc resultDesc = null;

        if (StringUtil.isEmpty(result)) {
            //返回数据为空
            resultDesc = dataRestructuring(-1, UIUtils.getString(R.string.back_abnormal_results), "");
            return resultDesc;
        }

        try {
            JSONObject jsonObject = new JSONObject(result);
            //返回码
            int error_code = jsonObject.getInt("error_code");
            //返回说明
            String reason = jsonObject.getString("reason");
            //返回数据
            String resultData = jsonObject.getString("result");

            resultDesc = dataRestructuring(error_code, reason, resultData);
        } catch (JSONException e) {
            resultDesc = dataRestructuring(-1, ExceptionCode(e), "");
        }

        return resultDesc;
    }

    /**
     * 数据重组
     *
     * @param error_code 返回码
     * @param reason     返回说明
     * @param resultData 返回数据
     * @return
     */
    public static ResultDesc dataRestructuring(int error_code, String reason, String resultData) {
        ResultDesc resultDesc = new ResultDesc();
        resultDesc.setError_code(error_code);
        resultDesc.setReason(reason);
        resultDesc.setResult(resultData);
        return resultDesc;
    }

    /**
     * 异常处理
     *
     * @param e 各类异常
     * @return
     */
    public static String ExceptionCode(Exception e) {
        if (e instanceof NetworkOnMainThreadException) {
            // 主线程中访问网络时异常
            // Android在4.0之前的版本支持在主线程中访问网络，但是在4.0以后对这部分程序进行了优化，也就是说访问网络的代码不能写在主线程中了。
            // 解决方法：采用多线程、异步加载的方式加载数据
            return UIUtils.getString(R.string.main_thread_access_network_exception);
        } else if (e instanceof SocketTimeoutException) {
            // 服务器响应超时
            return UIUtils.getString(R.string.server_response_timeout);
        } else if (e instanceof ConnectTimeoutException) {
            // 服务器请求超时
            return UIUtils.getString(R.string.server_request_timeout);
        } else if (e instanceof IOException) {
            // I/O异常
            return UIUtils.getString(R.string.io_exception);
        } else if (e instanceof JSONException) {
            // JSON格式转换异常
            return UIUtils.getString(R.string.json_format_conversion_exception);
        } else {
            // 其他异常
            return UIUtils.getString(R.string.back_abnormal_results);
        }
    }
}
