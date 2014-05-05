package com.cs6604.smartroute;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cs6604.adapter.PlacesAutoCompleteAdapter;
import com.cs6604.models.RouteDetails;
import com.cs6604.models.RouteLegs;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;


public class MainActivity extends Activity implements OnItemClickListener {

    private ArrayList<String> results = new ArrayList<String>();
    private List<RouteDetails> routeResults;
    private ArrayAdapter getPlaces;
    private AutoCompleteTextView sourceAutoComplete;
    private AutoCompleteTextView destAutoComplete;
    private EditText poiTextView;
    private GoogleMap map;
    private MapFragment mapFragment;
    private Button routeButton;
    private String _firstEndAddress;
    private String _originalJson;

    HttpRetriever httpRetriever = new HttpRetriever();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);
        sourceAutoComplete = (AutoCompleteTextView) findViewById(R.id.source_autocomplete_textview);
        destAutoComplete = (AutoCompleteTextView) findViewById(R.id.dest_autocomplete_textview);
        poiTextView = (EditText) findViewById(R.id.poi_text_view);
        routeButton = (Button) findViewById(R.id.route);

        // This is unnecessary since the MapFragment already exists in the view ... see main_page.xml
//        mapFragment = MapFragment.newInstance();
//        FragmentTransaction fragmentTransaction = getFragmentManager()
//                .beginTransaction();
//        fragmentTransaction.add(R.id.map, mapFragment);
//        fragmentTransaction.commit();

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();

        // setting some defaults
        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(38.90157749999999, -77.20872829999999), 13));

        routeButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                RouteRequestTask rrTask = new RouteRequestTask(sourceAutoComplete.getText().toString(),
                        destAutoComplete.getText().toString(),
                        poiTextView.getText().toString());
                rrTask.execute();
                hideSoftKeyboard(MainActivity.this);
            }

        });

        // Enable MyLocation Button in the Map
        this.map.setMyLocationEnabled(true);

        PlacesAutoCompleteAdapter adpter = new PlacesAutoCompleteAdapter(this, R.layout.list_item);
        sourceAutoComplete.setAdapter(adpter);
        destAutoComplete.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.list_item));
    }
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
    public void showRoute(View view) {
        Intent intent = new Intent(this, RouteListActivity.class);
        if (_originalJson != null)
            intent.putExtra("RouteDetails", _originalJson);

        intent.putExtra("PointOfInterest", poiTextView.getText().toString());
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        String str = (String) adapterView.getItemAtPosition(position);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    private class RouteRequestTask extends AsyncTask<String, Void, JSONObject> {
        private String origin;
        private String dest;
        private String pois;
        private String serverURL = "http://107.170.136.66:9999/route";

        public RouteRequestTask(String origin, String dest, String pois) {
            this.origin = origin;
            this.dest = dest;
            this.pois = pois;
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            // TODO Auto-generated method stub
            StringBuilder sb = new StringBuilder();
            try {
                sb.append(serverURL + "?origin=" + URLEncoder.encode(origin, "utf8"));
                sb.append("&dest=" + URLEncoder.encode(dest, "utf8"));
                sb.append("&pois=" + URLEncoder.encode(pois, "utf8"));
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Log.w("Tskatom", "URL: " + sb.toString());
            String response = httpRetriever.retrieve(sb.toString());
            try {
                return new JSONObject(response);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                if (!result.getString("status").equals("OK")) {
                    return;
                }
                _originalJson = result.toString();
                JSONArray routes = result.getJSONArray("routes");
                ParseDirectionJsonTask pdjTask = new ParseDirectionJsonTask();
                pdjTask.execute(routes);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private class ParseDirectionJsonTask extends AsyncTask<JSONArray, Void, List<RouteDetails>> {

        @Override
        protected List<RouteDetails> doInBackground(
                JSONArray... params) {
            // redoing this because I need more information
            List<RouteDetails> results = new ArrayList<RouteDetails>();
            try {
                JSONArray allRoutes = params[0];
                for (int i = 0; i < allRoutes.length(); i++) {
                    JSONObject jRoute = (JSONObject) allRoutes.get(i);
                    // first route
                    RouteDetails route = new RouteDetails(jRoute);
                    results.add(route);
                }

//                // TODO Auto-generated method stub
//                JSONArray mulRoutes = params[0];
//                DirectionsJSONParser djParser = new DirectionsJSONParser();
//                for (int i = 0; i < mulRoutes.length(); i++) {
//                    JSONObject route = (JSONObject) mulRoutes.get(i);
//                    List<List<HashMap<String, String>>> r_paths = djParser.parse(route);
//                    results.add(r_paths);
//                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return results;
        }

        @Override
        protected void onPostExecute(List<RouteDetails> results) {
            map.clear();
            routeResults = results;
            RouteLegs leg = results.get(0)._legs.get(0);
            leg._endAddress = poiTextView.getText().toString() + " @ " + leg._endAddress;
            ;
            for (int i = 0; i < results.size(); i++) {
                RouteDetails route = results.get(i);
                route.addToMap(map);
            }

//            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(38.88254, -77.11608), 13));
//            Log.w("Tskatom", "Finish Parse");
//            List<List<HashMap<String, String>>> result = results.get(0);
//            ArrayList<LatLng> points = null;
//            PolylineOptions lineOptions = null;
//            MarkerOptions markerOptions = new MarkerOptions();
//            String distance = "";
//            String duration = "";
//
//            // Traversing through all the routes
//            for (int i = 0; i < result.size(); i++) {
//                points = new ArrayList<LatLng>();
//                lineOptions = new PolylineOptions();
//
//                // Fetching i-th route
//                List<HashMap<String, String>> path = result.get(i);
//
//                // Fetching all the points in i-th route
//                for (int j = 0; j < path.size(); j++) {
//                    HashMap<String, String> point = path.get(j);
//
//                    if (j == 0) { // Get distance from the list
//                        distance = point.get("distance");
//                        continue;
//                    } else if (j == 1) { // Get duration from the list
//                        duration = point.get("duration");
//                        continue;
//                    }
//                    double lat = Double.parseDouble(point.get("lat"));
//                    double lng = Double.parseDouble(point.get("lng"));
//                    LatLng position = new LatLng(lat, lng);
//                    points.add(position);
//                }
//
//                // Adding all the points in the route to LineOptions
//                lineOptions.addAll(points);
//                lineOptions.width(2);
//                lineOptions.color(Color.RED);
//            }
////            lineOptions.geodesic(true);
//            Toast.makeText(getApplicationContext(), "Start to draw Line: " + lineOptions.getPoints().size(), Toast.LENGTH_LONG).show();
//            map.addPolyline(lineOptions);
        }

    }


}