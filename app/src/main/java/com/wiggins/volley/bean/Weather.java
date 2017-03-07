package com.wiggins.volley.bean;

/**
 * @Description 天气数据
 * @Author 一花一世界
 */

public class Weather {

    private WeatherInfo weatherinfo;

    public WeatherInfo getWeatherinfo() {
        return weatherinfo;
    }

    public void setWeatherinfo(WeatherInfo weatherinfo) {
        this.weatherinfo = weatherinfo;
    }

    @Override
    public String toString() {
        return "Weather{" +
                "weatherinfo=" + weatherinfo +
                '}';
    }
}
