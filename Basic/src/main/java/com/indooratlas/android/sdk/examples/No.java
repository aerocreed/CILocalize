package com.indooratlas.android.sdk.examples;

import java.util.List;

/**
 * Created by Germano on 08/11/2017.
 */

public class No{
    private WayPoint wayPoint;
    private List<Vizinho> vizinhos;

    public No(WayPoint wayPoint, List<Vizinho> vizinhos){
        this.wayPoint = wayPoint;
        this.vizinhos = vizinhos;
    }

    public void addVizinho(Vizinho vizinho){
        this.vizinhos.add(vizinho);
    }

    public void removeVizinho(Vizinho vizinho){
        //...
    }

    public WayPoint getWayPoint() {
        return wayPoint;
    }

    public void setWayPoint(WayPoint wayPoint) {
        this.wayPoint = wayPoint;
    }

    public List<Vizinho> getVizinhos() {
        return vizinhos;
    }

    public void setVizinhos(List<Vizinho> vizinhos) {
        this.vizinhos = vizinhos;
    }

    @Override
    public String toString(){
        String str = wayPoint.toString();
        return str;
    }
}
