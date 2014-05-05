package com.cs6604.smartroute;

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
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cs6604.adapter.PlacesAutoCompleteAdapter;
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
    private ArrayAdapter getPlaces;
    private AutoCompleteTextView sourceAutoComplete;
    private AutoCompleteTextView destAutoComplete;
    private EditText poiTextView;
    private GoogleMap map;
    private MapFragment mapFragment;
    private Button routeButton;
    private String _firstEndAddress;

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
            }

        });

        // Enable MyLocation Button in the Map
        this.map.setMyLocationEnabled(true);

        PlacesAutoCompleteAdapter adpter = new PlacesAutoCompleteAdapter(this, R.layout.list_item);
        sourceAutoComplete.setAdapter(adpter);
        destAutoComplete.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.list_item));
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
            RouteLegs leg = results.get(0)._legs.get(0);
            leg._endAddress = poiTextView.getText().toString() + " @ " + leg._endAddress;
            ;
            for (int i = 0; i < results.size(); i++) {
                RouteDetails route = results.get(i);
                route.addToMap();
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

    private class RouteTransitDetails {
        private int _numStops;
        private String _lineColor;
        private String _shortName;
        private String _icon;
        private String _type;

        public RouteTransitDetails(JSONObject object) throws JSONException {
            this._numStops = object.getInt("num_stops");
            if(object.getJSONObject("line").has("color"))
                this._lineColor = object.getJSONObject("line").getString("color");
            else
                this._lineColor = "#0000FF";
            this._shortName = object.getJSONObject("line").getString("name");
            this._type = object.getJSONObject("line").getJSONObject("vehicle").getString("type");
//            this._icon = object.getJSONObject("line").getString("icon");
        }
    }

    private class RouteSteps {
        private List<RouteSteps> _steps;
        private PolylineOptions _polylineOptions;
        private List<LatLng> _points;
        private String _travelMode;
        private String _htmlInstructions;
        private RouteTransitDetails _transitDetails = null;
        private LatLng _startPoint;

        public RouteSteps(JSONObject object) throws JSONException {
            // setup
            this._steps = new LinkedList<RouteSteps>();

            // check for nested steps
            if (object.has("steps")) {
                JSONArray jSteps = object.getJSONArray("steps");
                // get stps
                for (int i = 0; i < jSteps.length(); i++)
                    this._steps.add(new RouteSteps(jSteps.getJSONObject(i)));
            }

            // get html instructions
            this._htmlInstructions = object.getString("html_instructions");
            // get travel mode
            this._travelMode = object.getString("travel_mode");
            if (this._travelMode.equals("TRANSIT")) {
                this._transitDetails = new RouteTransitDetails(object.getJSONObject("transit_details"));
            }
            // get polyline
            _points = new LinkedList<LatLng>();
            List<LatLng> points = RouteUtils.decodePoly(object.getJSONObject("polyline").getString("points"));
            _points.clear();
            _points.addAll(points);

            // get start loation
            this._startPoint = RouteUtils.generatePoint(object.getJSONObject("start_location"));
        }

        public void addToMap() {
            _polylineOptions = new PolylineOptions();
            _polylineOptions.addAll(this._points);
            _polylineOptions.width(2);
            if (this._transitDetails != null) {
                _polylineOptions.color(RouteUtils.colorFromHex(this._transitDetails._lineColor));
            } else {
                _polylineOptions.color(RouteUtils.randomColor());
                if(this._transitDetails._type == "BUS")
                {
                    _polylineOptions.width(4);

                }else if(this._transitDetails._type == "SUBWAY")
                {
                    _polylineOptions.width(6);
                }
            }
            map.addPolyline(_polylineOptions);

            // add marker for start of steps
//            map.addMarker(new MarkerOptions().position(this._startPoint).title(this._htmlInstructions));
        }

    }

    private class RouteLegs {
        private List<RouteSteps> _steps;
        private String _startAddress;
        private String _endAddress;
        private LatLng _startPoint;
        private LatLng _endPoint;

        public RouteLegs(JSONObject object) throws JSONException {
            // setup
            this._steps = new LinkedList<RouteSteps>();
            JSONArray jSteps = object.getJSONArray("steps");
            // get steps
            for (int i = 0; i < jSteps.length(); i++)
                this._steps.add(new RouteSteps(jSteps.getJSONObject(i)));

            // get start and stop locations
            this._startPoint = RouteUtils.generatePoint(object.getJSONObject("start_location"));
            this._startAddress = object.getString("start_address");
            this._endPoint = RouteUtils.generatePoint(object.getJSONObject("end_location"));
            this._endAddress = object.getString("end_address");
        }

        public void addToMap() {
            // iterate over steps to add polylines
            Iterator<RouteSteps> stepsIterator = this._steps.iterator();
            while (stepsIterator.hasNext())
                stepsIterator.next().addToMap();

            // add markers for start and stop
//            if (this._endAddress.contains("@"))
                map.addMarker(new MarkerOptions().position(this._endPoint).title(this._endAddress).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
//            else
//                map.addMarker(new MarkerOptions().position(this._startPoint).title(this._startAddress).alpha(0.7f));
        }

    }

    private class RouteDetails {
        private LatLngBounds _boundary;
        private String _distance;
        private String _duration;
        private List<LatLng> _points;
        private List<RouteLegs> _legs;
        private JSONObject _originalData;

        public RouteDetails(JSONObject object) throws JSONException {
            // setup
            this._legs = new LinkedList<RouteLegs>();
            // high level info
            this._originalData = object;
            JSONArray jLegs = object.getJSONArray("legs");

            // get legs
            for (int i = 0; i < jLegs.length(); i++)
                this._legs.add(new RouteLegs(jLegs.getJSONObject(i)));

            // get boundary
            JSONObject northeast = object.getJSONObject("bounds").getJSONObject("northeast");
            LatLng northeast_point = RouteUtils.generatePoint(northeast);
            JSONObject southwest = object.getJSONObject("bounds").getJSONObject("southwest");
            LatLng southwest_point = RouteUtils.generatePoint(southwest);
            this.setBoundary(northeast_point, southwest_point);

            // get distance
            this.setDistance(jLegs.getJSONObject(0).getJSONObject("distance").getString("text"));
            // get duration
            this.setDuration(jLegs.getJSONObject(0).getJSONObject("duration").getString("text"));
            // get polyline and build
            _points = new LinkedList<LatLng>();
            this.decodePolyline(object.getJSONObject("overview_polyline").getString("points"));
        }

        public void addToMap() {
            // move camera
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(this.getBoundary(), 0));
            // iterate over legs
            Iterator<RouteLegs> legsIterator = this._legs.iterator();
            while (legsIterator.hasNext())
                legsIterator.next().addToMap();

            // add start of leg
//            map.addMarker(new MarkerOptions().position(this._legs.get(0)._startPoint).title(this._legs.get(0)._startAddress).alpha(0.7f));
        }

        public LatLngBounds getBoundary() {
            return _boundary;
        }

        public String getDistance() {
            return _distance;
        }

        public String getDuration() {
            return _duration;
        }

        public List<LatLng> getPoints() {
            return _points;
        }

        public void setBoundary(LatLng northeast, LatLng southwest) {
            _boundary = new LatLngBounds(southwest, northeast);
        }

        public void setDistance(String distance) {
            _distance = distance;
        }

        public void setDuration(String duration) {
            _duration = duration;
        }

        public void decodePolyline(String lineX) {
            List<LatLng> points = RouteUtils.decodePoly(lineX);
            _points.clear();
            _points.addAll(points);
        }

    }

}