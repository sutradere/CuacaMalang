package com.example.cuacamalang;

import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class MainActivity extends Activity implements OnClickListener {
	
	private Button buttonKota;
	private EditText EditText1;
	private Handler handler = new Handler();
	private ImageView iconCuaca;
	private String kota = "malang";
	private TextView kotaView, cuacaView, keterangan;
	private JSONObject json;
	private static final String OPEN_WEATHER_MAP_API = 
            "http://api.openweathermap.org/data/2.5/weather?q=%s,id&units=metric";
	
	Thread thread;
	
	private Runnable runnable = new Runnable(){
		@Override
		public void run(){
			json = getJSON(getApplicationContext(), kota);
			if(json == null){
                handler.post(new Runnable(){
                    public void run(){
                        Toast.makeText(getApplicationContext(), 
                                getApplicationContext().getString(R.string.place_not_found),
                                Toast.LENGTH_LONG).show(); 
                    }
                });
            } else {
                handler.post(new Runnable(){
                    public void run(){
                        renderWeather(json);
                    }
                });
            }
		}
	};
	
	public JSONObject getJSON(Context context, String city) {
        try {
            URL url = new URL(String.format(OPEN_WEATHER_MAP_API, city));           
            HttpURLConnection connection = 
                    (HttpURLConnection)url.openConnection();
             
            connection.addRequestProperty("x-api-key", 
                    context.getString(R.string.open_weather_maps_app_id));
             
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
             
            StringBuffer json = new StringBuffer(1024);
            String tmp="";
            while((tmp=reader.readLine())!=null)
                json.append(tmp).append("\n");
            reader.close();
             
            JSONObject data = new JSONObject(json.toString());
             
            // This value will be 404 if the request was not
            // successful
            if(data.getInt("cod") != 200){
                return null;
            }
             
            return data;
        }catch(Exception e){
            return null;
        }
    }
	
	private void renderWeather(JSONObject json){
		try {
			
			JSONObject cuaca = json.getJSONArray("weather").getJSONObject(0);
			JSONObject main = json.getJSONObject("main");
			
			kotaView.setText(json.getString("name").toUpperCase());
			setWeatherIcon(cuaca.getInt("id"),
	                json.getJSONObject("sys").getLong("sunrise") * 1000,
	                json.getJSONObject("sys").getLong("sunset") * 1000);
			cuacaView.setText(cuaca.getString("description").toUpperCase());
			keterangan.setText("temperature \t: "+main.getString("temp_min").toUpperCase() + " ℃" +  
					"\nhumidity \t\t\t\t: " + main.getString("humidity").toUpperCase() + " %");
			
		} catch (Exception e) {
			Log.e("ERROR","error");
		}
	}
	
	private void setWeatherIcon(int actualId, long sunrise, long sunset){
	    int id = actualId / 100;
	    if(actualId == 800){
	        long currentTime = new Date().getTime();
	        if(currentTime>=sunrise && currentTime<sunset) {
	        	iconCuaca.setImageResource(R.drawable.weather1);
	        } else {
	        	iconCuaca.setImageResource(R.drawable.weather2);
	        }
	    } else {
	        switch(id) {
	        case 2 : iconCuaca.setImageResource(R.drawable.weather3);
	                 break;         
	        case 3 : iconCuaca.setImageResource(R.drawable.weather4);
	                 break;     
	        case 7 : iconCuaca.setImageResource(R.drawable.weather5);
	                 break;
	        case 8 : iconCuaca.setImageResource(R.drawable.weather6);
	                 break;
	        case 6 : iconCuaca.setImageResource(R.drawable.weather7);
	                 break;
	        case 5 : iconCuaca.setImageResource(R.drawable.weather8);
	                 break;
	        }
	    }
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.buttonKota = (Button) this.findViewById(R.id.buttonKota);
		this.buttonKota.setOnClickListener(this);
		this.EditText1 = (EditText) this.findViewById(R.id.editText1);
		this.kotaView = (TextView) this.findViewById(R.id.textView1);
		this.cuacaView = (TextView) this.findViewById(R.id.textView2);
		this.iconCuaca= (ImageView) this.findViewById(R.id.imageView1);
		this.keterangan = (TextView) this.findViewById(R.id.textView3);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
	
	@Override
	public void onClick(View view) {
			if(view.getId()==R.id.buttonKota){
				this.kota = (String) EditText1.getText().toString();
				thread = new Thread(runnable);
				thread.start();
		}
	}
}
