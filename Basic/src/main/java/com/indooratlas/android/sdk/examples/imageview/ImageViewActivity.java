package com.indooratlas.android.sdk.examples.imageview;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PointF;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.google.android.gms.maps.model.LatLng;
import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IALocationRequest;
import com.indooratlas.android.sdk.IARegion;
import com.indooratlas.android.sdk.examples.Grafo;
import com.indooratlas.android.sdk.examples.No;
import com.indooratlas.android.sdk.examples.R;
import com.indooratlas.android.sdk.examples.Rota;
import com.indooratlas.android.sdk.examples.SdkExample;
import com.indooratlas.android.sdk.examples.Vizinho;
import com.indooratlas.android.sdk.examples.WayPoint;
import com.indooratlas.android.sdk.examples.utils.ExampleUtils;
import com.indooratlas.android.sdk.resources.IAFloorPlan;
import com.indooratlas.android.sdk.resources.IALatLng;
import com.indooratlas.android.sdk.resources.IALocationListenerSupport;
import com.indooratlas.android.sdk.resources.IAResourceManager;
import com.indooratlas.android.sdk.resources.IAResult;
import com.indooratlas.android.sdk.resources.IAResultCallback;
import com.indooratlas.android.sdk.resources.IATask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;

@SdkExample(description = R.string.example_imageview_description)
public class ImageViewActivity extends FragmentActivity {

    private static final String TAG = "IndoorAtlasExample";

    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1;

    // blue dot radius in meters
    private static final float dotRadius = 1.0f;

    private IALocationManager mIALocationManager;
    private IAResourceManager mFloorPlanManager;
    private IATask<IAFloorPlan> mPendingAsyncResult;
    private IAFloorPlan mFloorPlan;
    private BlueDotView mImageView;
    private BlueDotView mImageView2;
    private double locationLat, locationLng;
    private long mDownloadId;
    private DownloadManager mDownloadManager;

    private JSONArray jsonArray;
    private Rota rota;
    private Grafo grafo;
    private ArrayList<WayPoint> wayPoints;

    private IALocationListener mLocationListener = new IALocationListenerSupport() {
        @Override
        public void onLocationChanged(IALocation location) {
            Log.d(TAG, "location is: " + location.getLatitude() + "," + location.getLongitude());

            if (mImageView != null && mImageView.isReady()) {
                IALatLng latLng = new IALatLng(location.getLatitude(), location.getLongitude());
                latLng = closestWaypoint(latLng);
                IALatLng latLng2 = new IALatLng(locationLat, locationLng);
                PointF point = mFloorPlan.coordinateToPoint(latLng);
                PointF point2 = mFloorPlan.coordinateToPoint(latLng2);

                //Toast.makeText(ImageViewActivity.this, "lat: " + latLng.latitude + "lng: " + latLng.longitude, Toast.LENGTH_LONG).show();

                mImageView.setDotCenter(point, point2, rota, mFloorPlan);
                mImageView.postInvalidate();

            }
        }
    };

    private IARegion.Listener mRegionListener = new IARegion.Listener() {

        @Override
        public void onEnterRegion(IARegion region) {
            if (region.getType() == IARegion.TYPE_FLOOR_PLAN) {
                String id = region.getId();
                String name = region.getName();
                Log.d(TAG, "floorPlan changed to " + name);
                Toast.makeText(ImageViewActivity.this, name, Toast.LENGTH_SHORT).show();
                fetchFloorPlan(id);
            }
        }

        @Override
        public void onExitRegion(IARegion region) {
            // leaving a previously entered region
        }

    };

    void setRota(int idfile){
        try {
            this.jsonArray = ImageViewActivity.readJsonFromFile(getResources().openRawResource(idfile));
            this.rota = new Rota(new ArrayList<WayPoint>());
            this.wayPoints = new ArrayList<>();
            No no;
            ArrayList<No> nos = new ArrayList<>();
            WayPoint wayPoint;
            String tag, tipo;
            IALatLng iaLatLng;
            int id;
            JSONObject jo;
            //Preenche o grafo
            for(int i=0; i<jsonArray.length(); i++) {
                jo = jsonArray.getJSONObject(i);
                tag = jo.getString("tag");
                tipo = jo.getString("tipo");
                iaLatLng = new IALatLng(jo.getDouble("lat"), jo.getDouble("lgt"));
                id = jo.getInt("index");
                wayPoints.add(new WayPoint(id, tag, tipo, iaLatLng));
                no = new No(wayPoints.get(i), new ArrayList<Vizinho>());
                nos.add(no);
            }
            this.grafo = new Grafo(nos);
            //Para cada nó, preenche seus vizinhos
            for(int i=0; i<jsonArray.length(); i++) {
                jo = jsonArray.getJSONObject(i);
                JSONArray jsonVizinhos = jo.getJSONArray("nghbr");
                JSONArray distancias = jo.getJSONArray("dist");
                //Toast.makeText(ImageViewActivity.this, "id: " + i + " - vizinhos: " + jsonVizinhos.toString(), Toast.LENGTH_LONG).show();
                //Toast.makeText(ImageViewActivity.this, "id: " + i + " - distancias: " + distancias.toString(), Toast.LENGTH_LONG).show();
                for(int j=0; j<jsonVizinhos.length(); j++){
                    int index_vizinho = jsonVizinhos.getInt(j);
                    double peso = distancias.getDouble(j);
                    Vizinho vizinho = new Vizinho(grafo.getNo(index_vizinho).getWayPoint(), peso);
                    grafo.getNo(i).addVizinho(vizinho);
                }
            }
            try {
                rota.getRota().add(wayPoints.get(1));
                rota.getRota().add(wayPoints.get(2));
                rota.getRota().add(wayPoints.get(9));
                rota.getRota().add(wayPoints.get(10));
                rota.getRota().add(wayPoints.get(14));
                rota.getRota().add(wayPoints.get(15));
                rota.getRota().add(wayPoints.get(16));
                rota.getRota().add(wayPoints.get(17));
                rota.getRota().add(wayPoints.get(18));
                rota.getRota().add(wayPoints.get(19));
                rota.getRota().add(wayPoints.get(20));
            } catch (Exception e){
                Toast.makeText(ImageViewActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e){
            Toast.makeText(ImageViewActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        // prevent the screen going to sleep while app is on foreground
        findViewById(android.R.id.content).setKeepScreenOn(true);

        mImageView = (BlueDotView) findViewById(R.id.imageView);
        mImageView2 = (BlueDotView) findViewById(R.id.imageViewRed);

        mDownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        mIALocationManager = IALocationManager.create(this);
        mFloorPlanManager = IAResourceManager.create(this);

        locationLat = -7.16232046;
        locationLng = -34.81714502;
        //locationLat = -7.16238566;
        //locationLng = -34.81720202;
        //locationLat = -7.11355057;
        //locationLng = -34.87753697;

        //setRota(R.raw.floor_1);
        setRota(R.raw.floor_3);

        /* optional setup of floor plan id
           if setLocation is not called, then location manager tries to find
           location automatically */
        final String floorPlanId = getString(R.string.indooratlas_floor_plan_id);
        if (!TextUtils.isEmpty(floorPlanId)) {
            final IALocation location = IALocation.from(IARegion.floorPlan(floorPlanId));
            mIALocationManager.setLocation(location);
        }

        // Setup long click listener for sharing traceId
        ExampleUtils.shareTraceId(findViewById(R.id.imageView), ImageViewActivity.this,
                mIALocationManager);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIALocationManager.destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ensurePermissions();
        // starts receiving location updates
        mIALocationManager.requestLocationUpdates(IALocationRequest.create(), mLocationListener);
        mIALocationManager.registerRegionListener(mRegionListener);
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIALocationManager.removeLocationUpdates(mLocationListener);
        mIALocationManager.unregisterRegionListener(mRegionListener);
        unregisterReceiver(onComplete);
    }

    /**
     * Methods for fetching floor plan data and bitmap image.
     * Method {@link #fetchFloorPlan(String id)} fetches floor plan data including URL to bitmap
     */

     /*  Broadcast receiver for floor plan image download */
    private BroadcastReceiver onComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
            if (id != mDownloadId) {
                Log.w(TAG, "Ignore unrelated download");
                return;
            }
            Log.w(TAG, "Image download completed");
            Bundle extras = intent.getExtras();
            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterById(extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID));
            Cursor c = mDownloadManager.query(q);

            if (c.moveToFirst()) {
                int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    // process download
                    String filePath = c.getString(c.getColumnIndex(
                            DownloadManager.COLUMN_LOCAL_FILENAME));
                    showFloorPlanImage(filePath);
                }
            }
            c.close();
        }
    };

    private void showFloorPlanImage(String filePath) {
        Log.w(TAG, "showFloorPlanImage: " + filePath);
        mImageView.setRadius(mFloorPlan.getMetersToPixels() * dotRadius);
        mImageView.setImage(ImageSource.uri(filePath));
    }

    /**
     * Fetches floor plan data from IndoorAtlas server. Some room for cleaning up!!
     */
    private void fetchFloorPlan(String id) {
        cancelPendingNetworkCalls();
        final IATask<IAFloorPlan> asyncResult = mFloorPlanManager.fetchFloorPlanWithId(id);
        mPendingAsyncResult = asyncResult;
        if (mPendingAsyncResult != null) {
            mPendingAsyncResult.setCallback(new IAResultCallback<IAFloorPlan>() {
                @Override
                public void onResult(IAResult<IAFloorPlan> result) {
                    Log.d(TAG, "fetch floor plan result:" + result);
                    if (result.isSuccess() && result.getResult() != null) {
                        mFloorPlan = result.getResult();
                        String fileName = mFloorPlan.getId() + ".img";
                        String filePath = Environment.getExternalStorageDirectory() + "/"
                                + Environment.DIRECTORY_DOWNLOADS + "/" + fileName;
                        File file = new File(filePath);
                        if (!file.exists()) {
                            DownloadManager.Request request =
                                    new DownloadManager.Request(Uri.parse(mFloorPlan.getUrl()));
                            request.setDescription("IndoorAtlas floor plan");
                            request.setTitle("Floor plan");
                            // requires android 3.2 or later to compile
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                request.allowScanningByMediaScanner();
                                request.setNotificationVisibility(DownloadManager.
                                        Request.VISIBILITY_HIDDEN);
                            }
                            request.setDestinationInExternalPublicDir(Environment.
                                    DIRECTORY_DOWNLOADS, fileName);

                            mDownloadId = mDownloadManager.enqueue(request);
                        } else {
                            showFloorPlanImage(filePath);
                        }
                    } else {
                        // do something with error
                        if (!asyncResult.isCancelled()) {
                            Toast.makeText(ImageViewActivity.this,
                                    (result.getError() != null
                                            ? "error loading floor plan: " + result.getError()
                                            : "access to floor plan denied"), Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                }
            }, Looper.getMainLooper()); // deliver callbacks in main thread
        }
    }

    private void cancelPendingNetworkCalls() {
        if (mPendingAsyncResult != null && !mPendingAsyncResult.isCancelled()) {
            mPendingAsyncResult.cancel();
        }
    }


    private void ensurePermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case REQUEST_CODE_WRITE_EXTERNAL_STORAGE:

                if (grantResults.length == 0
                        || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, R.string.storage_permission_denied_message,
                            Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }

    }

    //https://stackoverflow.com/questions/6349759/using-json-file-in-android-app-resources
    public static JSONArray readJsonFromFile(InputStream is) throws JSONException {
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String jsonString = writer.toString();
        return new JSONArray(jsonString);
    }

    //Return the IALatLng from the shorter WayPoint
    public IALatLng closestWaypoint(IALatLng latLng){
        double distance, shorter_distance = Double.MAX_VALUE;
        IALatLng shorter_wayPoint = null;
        for(WayPoint wayPoint: this.wayPoints){
            distance = calcDistance(latLng, wayPoint.getLatLng());
            if(distance < shorter_distance){
                shorter_distance = distance;
                shorter_wayPoint = wayPoint.getLatLng();
            }
        }
        return shorter_wayPoint;
    }

    //http://carlosdelfino.eti.br/cursoarduino/geoprocessamento/calculando-distancias-com-base-em-coordenadas-de-gps/
    public double calcDistance(IALatLng latLng_inicial, IALatLng latLng_final) {

        double lat_inicial = latLng_inicial.latitude,
               long_inicial = latLng_inicial.longitude,
               lat_final = latLng_final.latitude,
               long_final = latLng_final.longitude;
        double d2r = 0.017453292519943295769236;

        double dlong = (long_final - long_inicial) * d2r;
        double dlat = (lat_final - lat_inicial) * d2r;

        double temp_sin = Math.sin(dlat/2.0);
        double temp_cos = Math.cos(lat_inicial * d2r);
        double temp_sin2 = Math.sin(dlong/2.0);

        double a = (temp_sin * temp_sin) + (temp_cos * temp_cos) * (temp_sin2 * temp_sin2);
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));

        return 6368.1 * c;
    }

}

