package com.indooratlas.android.sdk.examples;

import com.indooratlas.android.sdk.resources.IALatLng;

import java.util.List;

/**
 * Created by Germano on 08/11/2017.
 */

public class WayPoint {
    int id;
    String tag, tipo;
    IALatLng latLng;

    public WayPoint(int id, String tag, String tipo, IALatLng latLng){
        this.id = id;
        this.tag = tag;
        this.tipo = tipo;
        this.latLng = latLng;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public IALatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(IALatLng latLng) {
        this.latLng = latLng;
    }

    @Override
    public String toString(){
        return "id: " + this.id + " - tag: " + this.tag + " - tipo: " + this.tipo;
    }
}
