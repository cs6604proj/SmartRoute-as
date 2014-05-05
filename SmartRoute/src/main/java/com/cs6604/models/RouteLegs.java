package com.cs6604.models;
import com.cs6604.smartroute.RouteUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class RouteLegs implements Serializable {
    public List<RouteSteps> _steps;
    public String _startAddress;
    public String _endAddress;
    public LatLng _startPoint;
    public LatLng _endPoint;

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

    public void addToMap(GoogleMap map) {
        // iterate over steps to add polylines
        Iterator<RouteSteps> stepsIterator = this._steps.iterator();
        while (stepsIterator.hasNext())
            stepsIterator.next().addToMap(map);

        // add markers for start and stop
//            if (this._endAddress.contains("@"))
        map.addMarker(new MarkerOptions().position(this._endPoint).title(this._endAddress).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
//            else
//                map.addMarker(new MarkerOptions().position(this._startPoint).title(this._startAddress).alpha(0.7f));
    }

}