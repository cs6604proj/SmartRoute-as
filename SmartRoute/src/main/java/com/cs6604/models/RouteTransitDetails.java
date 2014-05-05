package com.cs6604.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class RouteTransitDetails implements Serializable {
    public int _numStops;
    public String _lineColor;
    public String _shortName;
    public String _icon;
    public String _type;

    public RouteTransitDetails(JSONObject object) throws JSONException {
        this._numStops = object.getInt("num_stops");
        if(object.getJSONObject("line").has("color"))
            this._lineColor = object.getJSONObject("line").getString("color");
        else
            this._lineColor = "#0000FF";
        if(object.getJSONObject("line").has("name"))
            this._shortName = object.getJSONObject("line").getString("name");
        this._type = object.getJSONObject("line").getJSONObject("vehicle").getString("type");
//            this._icon = object.getJSONObject("line").getString("icon");
    }
}