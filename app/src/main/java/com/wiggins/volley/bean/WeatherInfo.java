package com.wiggins.volley.bean;

/**
 * @Description 天气数据
 * @Author 一花一世界
 */

public class WeatherInfo {

    private String city;
    private String temp;
    private String time;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "WeatherInfo{" +
                "city='" + city + '\'' +
                ", temp='" + temp + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
