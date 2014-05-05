package com.cs6604.models;
import com.cs6604.smartroute.RouteUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class RouteDetails implements Serializable {
    public LatLngBounds _boundary;
    public String _distance;
    public String _duration;
    public List<LatLng> _points;
    public List<RouteLegs> _legs;
    public JSONObject _originalData;

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

    public void addToMap(GoogleMap map) {
        // move camera
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(this.getBoundary(), 0));
        // iterate over legs
        Iterator<RouteLegs> legsIterator = this._legs.iterator();
        while (legsIterator.hasNext())
            legsIterator.next().addToMap(map);

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