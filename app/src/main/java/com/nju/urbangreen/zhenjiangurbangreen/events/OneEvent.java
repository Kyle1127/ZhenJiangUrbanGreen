package com.nju.urbangreen.zhenjiangurbangreen.events;

/**
 * Created by Liwei on 2016/11/25.
 */
//事件记录类
public class OneEvent {

    public String name;
    public String registrar;
    public String location;
    public String date_time;

    public OneEvent(String name, String registrar, String location, String date_time){
        this.name = name;
        this.registrar = registrar;
        this.location = location;
        this.date_time = date_time;

    }
}
