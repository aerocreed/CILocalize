package com.indooratlas.android.sdk.examples;

import java.util.List;

/**
 * Created by Germano on 08/11/2017.
 */

public class Rota {
    private List<WayPoint> rota;
    private int indice;

    public Rota(List<WayPoint> rota){
        this.rota = rota;
        this.indice = 0;
    }

    public void addWaypoint(WayPoint wayPoint){
        this.rota.add(wayPoint);
    }

    public WayPoint next(){
        return this.rota.get(indice++);
    }

    public List<WayPoint> getRota() {
        return rota;
    }

    public void setRota(List<WayPoint> rota) {
        this.rota = rota;
    }
}
