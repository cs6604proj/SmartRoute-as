package com.cs6604.models;
import com.cs6604.smartroute.MainActivity;
import com.cs6604.smartroute.RouteUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;


public class RouteSteps implements Serializable{
    public List<RouteSteps> _steps;
    public PolylineOptions _polylineOptions;
    public List<LatLng> _points;
    public String _travelMode;
    public String _htmlInstructions;
    public RouteTransitDetails _transitDetails = null;
    public LatLng _startPoint;

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

    public void addToMap(GoogleMap map) {
        _polylineOptions = new PolylineOptions();
        _polylineOptions.addAll(this._points);
        _polylineOptions.width(5);
        if (this._transitDetails != null) {
            _polylineOptions.color(RouteUtils.colorFromHex(this._transitDetails._lineColor));
        } else {
//            _polylineOptions.color(RouteUtils.randomColor());
            _polylineOptions.color(RouteUtils.colorFromHex("#66FF33"));
        }
        map.addPolyline(_polylineOptions);

        // add marker for start of steps
//            map.addMarker(new MarkerOptions().position(this._startPoint).title(this._htmlInstructions));
    }

}