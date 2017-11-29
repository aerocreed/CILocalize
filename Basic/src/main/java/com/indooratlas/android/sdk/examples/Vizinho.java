package com.indooratlas.android.sdk.examples;

import com.indooratlas.android.sdk.examples.WayPoint;

/**
 * Created by Germano on 08/11/2017.
 */

public class Vizinho {
    private WayPoint wayPoint;
    private double peso;

    public Vizinho(WayPoint wayPoint, double peso){
        this.wayPoint = wayPoint;
        this.peso = peso;
    }

    public WayPoint getWayPoint() {
        return wayPoint;
    }

    public void setWayPoint(WayPoint wayPoint) {
        this.wayPoint = wayPoint;
    }

    public double getPeso() {
        return peso;
    }

    @Override
    public String toString(){
        return wayPoint.toString() + " peso: " + peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }
}
