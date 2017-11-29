package com.indooratlas.android.sdk.examples;

import java.util.List;

/**
 * Created by Germano on 08/11/2017.
 */

public class Grafo {
    private List<No> nos;

    public Grafo(List<No> lista){
        nos = lista;
    }

    public void addNo(No no){
        nos.add(no);
    }

    public void removeNo(No no){
        //...
    }

    public No getNo(int index){
        return nos.get(index);
    }
}
