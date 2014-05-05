package com.cs6604.smartroute;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.cs6604.models.RouteDetails;
import com.cs6604.models.RouteLegs;
import com.cs6604.models.RouteSteps;
import com.google.android.gms.plus.model.people.Person;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RouteListActivity extends ActionBarActivity {

    List<RouteDetails> results = new ArrayList<RouteDetails>();
    List<Map<String, String>> instructions = new ArrayList<Map<String,String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_list);
        String routeDetailsString = (String) getIntent().getSerializableExtra("RouteDetails");
        if(routeDetailsString != null && !routeDetailsString.equals("")) {
            try {

                JSONObject jObject = new JSONObject(routeDetailsString);
                JSONArray allRoutes = jObject.getJSONArray("routes");
                for (int i = 0; i < allRoutes.length(); i++) {
                    JSONObject jRoute = null;
                    jRoute = (JSONObject) allRoutes.get(i);
                    // first route
                    RouteDetails route = new RouteDetails(jRoute);
                    results.add(route);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // flatten the route details
            for (RouteDetails r : results) {
                for (RouteLegs leg : r._legs) {
                    for (RouteSteps step : leg._steps) {
                        HashMap<String, String> instruction = new HashMap<String, String>();
                        instruction.put("instruction", step._htmlInstructions);
                        instructions.add(instruction);
                    }
                }
            }

            // construct list view
            ListView lv = (ListView) findViewById(R.id.list);
            SimpleAdapter simpleAdapter = new SimpleAdapter(this,
                    instructions,
                    android.R.layout.simple_list_item_1,
                    new String[]{"instruction"},
                    new int[]{android.R.id.text1});
            lv.setAdapter(simpleAdapter);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.route_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
