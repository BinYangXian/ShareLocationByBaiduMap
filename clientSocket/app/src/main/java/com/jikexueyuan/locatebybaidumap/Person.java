package com.jikexueyuan.locatebybaidumap;

import com.baidu.mapapi.model.inner.GeoPoint;

/**
 * Created by fangc on 2016/5/26.
 */
public class Person {


    private int type;
    private GeoPoint gp;
    private String adress;
    private String name;
    private boolean sex;

    public Person(int type, GeoPoint gp, String adress, String name, boolean sex) {
        this.type = type;
        this.gp = gp;
        this.adress = adress;
        this.name = name;
        this.sex = sex;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public GeoPoint getGp() {
        return gp;
    }

    public void setGp(GeoPoint gp) {
        this.gp = gp;
    }

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSex() {
        return sex;
    }

    public void setSex(boolean sex) {
        this.sex = sex;
    }
}

