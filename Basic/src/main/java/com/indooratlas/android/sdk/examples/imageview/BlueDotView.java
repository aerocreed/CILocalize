package com.indooratlas.android.sdk.examples.imageview;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.provider.ContactsContract;
import android.util.AttributeSet;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.indooratlas.android.sdk.examples.R;
import com.indooratlas.android.sdk.examples.Rota;
import com.indooratlas.android.sdk.examples.WayPoint;
import com.indooratlas.android.sdk.resources.IAFloorPlan;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Extends great ImageView library by Dave Morrissey. See more:
 * https://github.com/davemorrissey/subsampling-scale-image-view.
 */
public class BlueDotView extends SubsamplingScaleImageView {

    private float radius = 0.5f;
    private PointF dotCenter = null;
    private PointF dotRed = null;
    private Rota rota = null;
    private IAFloorPlan iaFloorPlan = null;

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setDotCenter(PointF dotCenter, PointF dotRed, Rota rota, IAFloorPlan iaFloorPlan) {
        this.dotCenter = dotCenter;
        this.dotRed = dotRed;
        this.rota = rota;
        this.iaFloorPlan = iaFloorPlan;
    }

    public BlueDotView(Context context) {
        this(context, null);
    }

    public BlueDotView(Context context, AttributeSet attr) {
        super(context, attr);
        initialise();
    }

    private void initialise() {
        setWillNotDraw(false);
        setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_CENTER);
    }

    //TODO: Draw the route using the latitude and longitude values from each way point
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isReady()) {
            return;
        }
        PointF vPoint = null, vPoint2;

        if (dotCenter != null) {
            vPoint = sourceToViewCoord(dotCenter);
            float scaledRadius = getScale() * radius;
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(getResources().getColor(R.color.ia_blue));
            canvas.drawCircle(vPoint.x, vPoint.y, scaledRadius, paint);
        }

        //Se o destino foi escolhido
        if (dotRed != null && vPoint != null) {
            vPoint2 = sourceToViewCoord(dotRed);
            float scaledRadius = getScale() * radius;
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(getResources().getColor(R.color.ia_green));
            canvas.drawCircle(vPoint2.x, vPoint2.y, scaledRadius, paint);

            Paint paintRoute = new Paint();
            paintRoute.setAntiAlias(true);
            paintRoute.setStyle(Paint.Style.FILL);
            paintRoute.setColor(getResources().getColor(R.color.ia_red));
            paintRoute.setStrokeWidth(30); //Aumenta a espessura da linha
            /*while (true){
                WayPoint wp = rota.next();
                if(wp == null)
                    break;

            }*/
            if(rota != null && iaFloorPlan != null){
                PointF pointF1, pointF2;
                for(int i=0; i<rota.getRota().size()-1; i++) {
                    pointF1 = sourceToViewCoord(iaFloorPlan.coordinateToPoint(rota.getRota().get(i).getLatLng()));
                    pointF2 = sourceToViewCoord(iaFloorPlan.coordinateToPoint(rota.getRota().get(i+1).getLatLng()));
                    canvas.drawLine(pointF1.x, pointF1.y, pointF2.x, pointF2.y, paintRoute);
                }
            }
            //for(int i=0; i<jsonArray.length(); i++) {

//                canvas.drawLine(vPoint.x, vPoint.y, vPoint2.x, vPoint2.y, paintRoute);
            //}

        }
    }
}
