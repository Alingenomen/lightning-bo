package com.example.android.lightningtest;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.Response;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private OkHttpClient client;
    private ArrayList<Lightning> lightningArraylist = new ArrayList<>();
    private ArrayList<Marker> lightningMarkers = new ArrayList<>();

    private final class EchoWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;
        private GoogleMap mapToPass;

        String msgToSend = "{\"west\":-11,\"east\":20,\"north\":60,\"south\":40}";

        @Override
        public void onOpen(WebSocket webSocket, okhttp3.Response response) {
            webSocket.send(msgToSend);
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            parseJsonObject(text);
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            // output("Receiving bytes : " + bytes.hex());
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
            // output("Closing : " + code + " / " + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
            // output("Error : " + t.getMessage());
        }

        public void setGoogleMap(GoogleMap googleMap){
            mapToPass = googleMap;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        client = new OkHttpClient();
        startWebSocket(mMap);

        // Add a marker in Sydney and move the camera
        LatLng illinois = new LatLng(40.8173, -88.8981);
        mMap.addMarker(new MarkerOptions().position(illinois).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(illinois));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(6.0f));

        styleTheMap();
    }

    /**
     * Set map style using the stylefile in
     * the resource folder raw/mapstyle.json
     */
    private void styleTheMap() {
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle));
    }

    private void startWebSocket(GoogleMap googleMap) {
        Request request = new Request.Builder().url("ws://ws.blitzortung.org:8068").build();
        EchoWebSocketListener listener = new EchoWebSocketListener();
        listener.setGoogleMap(googleMap);
        WebSocket ws = client.newWebSocket(request, listener);

        client.dispatcher().executorService().shutdown();
    }

    private void parseJsonObject(String jsonString){
        Log.d("onmessage",jsonString);

        String timeStampAsString;
        Double strikeLat = 0.0;
        Double strikeLon = 0.0;

        try {
            JSONObject jObj = new JSONObject(jsonString);
            timeStampAsString = jObj.getString("time");
            Date timeStamp = new Date(Long.parseLong(timeStampAsString));
            strikeLat = jObj.getDouble("lat");
            strikeLon = jObj.getDouble("lon");

            Log.d("strike location: ", "latitude: " + Double.toString(strikeLat) + ", longitude: " + Double.toString(strikeLon));
            LatLng lightning = new LatLng(strikeLat,strikeLon);
            Log.d("strike location: ", "latitude: " + Double.toString(lightning.latitude) + ", longitude: " + Double.toString(lightning.longitude));


            addLightning(lightning, timeStamp, strikeLat, strikeLon);

        } catch (JSONException e) {
            Log.e("Jsonparsing", "unexpected JSON exception", e);
        }

    }

    private void addLightning(final LatLng position, final Date time, final Double lat, final Double lon) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.lightning_purple);
                MarkerOptions markerOptions = new MarkerOptions().position(position).icon(icon);
                Marker lightningMarker = mMap.addMarker(markerOptions);

                Lightning lightning = new Lightning(time,lat,lon,lightningMarker);
                lightningMarkers.add(lightningMarker);
            }
        });
    }
}
