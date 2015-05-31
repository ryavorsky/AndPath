package com.example.findmyway;

import java.io.IOException;
import java.net.HttpURLConnection;

import retrofit.RestAdapter;
import retrofit.client.Request;
import retrofit.client.Response;
import retrofit.client.UrlConnectionClient;
import retrofit.http.GET;
import retrofit.http.Query;
import retrofit.mime.TypedByteArray;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class PathMenu extends Activity{
	
	ProgressDialog pd;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    	   	
	    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	    StrictMode.setThreadPolicy(policy);
	        
	    setContentView(R.layout.path_menu);
	    //getActionBar().hide();
	    
	    Button mButton = (Button)findViewById(R.id.Go);
	    mButton.setOnClickListener(new View.OnClickListener(){
	    	public void onClick(View view){
	    		
	    		//Считываем значения полей
	    		int FromCity, FromAddr, ToCity, ToAddr;
            	try{
            		FromCity = Integer.parseInt(((EditText)findViewById(R.id.FromCity)).getText().toString());
            		FromAddr = Integer.parseInt(((EditText)findViewById(R.id.FromAddr)).getText().toString());
            		ToCity = Integer.parseInt(((EditText)findViewById(R.id.ToCity)).getText().toString());
            		ToAddr = Integer.parseInt(((EditText)findViewById(R.id.ToAddr)).getText().toString());
            		
            		if(((EditText)findViewById(R.id.FromCity)).getText().toString().length()!=4
            				||((EditText)findViewById(R.id.FromAddr)).getText().toString().length()!=6
            				||((EditText)findViewById(R.id.ToCity)).getText().toString().length()!=4
            				||((EditText)findViewById(R.id.ToAddr)).getText().toString().length()!=6){
            			//return;
            		}
            		
            	} catch (Exception e) { return; } 
            	
	    		showDialog(FromCity, FromAddr, ToCity, ToAddr);
	    	}
	    });
	}
	
	public void showDialog(int FromCity, int FromAddr, int ToCity, int ToAddr){
		/*
		 pd = new ProgressDialog(this);
		 
	    pd.setTitle("Подождите");
	    pd.setMessage("Прокладываю маршрут");
	    pd.setCancelable(false);
	    pd.show();
*/
	    Intent intent = new Intent(PathMenu.this, MapActivity.class);
	    Bundle b = new Bundle();
	    b.putString("startGeoPoint", getAddrFromServer(FromCity+"", FromAddr+""));
	    b.putString("stopGeoPoint", getAddrFromServer(ToCity+"", ToAddr+""));
	    intent.putExtras(b);
	    startActivity(intent);
	    
	//    pd.dismiss();
	}
	
	public String getAddrFromServer(String city, String addr){
		RestAdapter restAdapter = new RestAdapter.Builder()
		    .setEndpoint("http://geo.viz-labs.ru")
		    .setLogLevel(RestAdapter.LogLevel.FULL)
		    .setClient(new MyUrlConnectionClient())
		    .build();
		ServerPoints routeService = restAdapter.create(ServerPoints.class);
		Response routeResponse = routeService.getPoint(city, addr);		 
		String str = new String(((TypedByteArray) routeResponse.getBody()).getBytes());
		str = str.substring(str.indexOf(":")+1, str.indexOf("<br/>"));
		str = str.replace("\t", "%20");
		str = str.replace(" ", "%20");
		//str = str.replace("\r\n", "").replace("\n", "");
		str = str.replace("%20%20", "%20");
		str = str.replaceAll("\\r", "").replaceAll("\\n", "");
		return str;
	 }
	
	public interface ServerPoints {
	    @GET("/navidb/search.php")
	    Response getPoint(
	            @Query("city") String city,
	            @Query("addr") String addr);
	}
	
	public final class MyUrlConnectionClient extends UrlConnectionClient {
		  @Override protected HttpURLConnection openConnection(Request request) {
			  HttpURLConnection connection;
			try {
				connection = super.openConnection(request);
				connection.setConnectTimeout(120 * 1000);
			    connection.setReadTimeout(120 * 1000);
			    return connection;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    return null;
		  }
		}

}
