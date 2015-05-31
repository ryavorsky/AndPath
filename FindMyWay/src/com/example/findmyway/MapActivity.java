package com.example.findmyway;


import java.util.List;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Query;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    	
	    	
	    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	    StrictMode.setThreadPolicy(policy);
	        
	    setContentView(R.layout.map_layout);
	    getActionBar().hide();
	    	
	    MapFragment mapFragment = (MapFragment) getFragmentManager()
	    		.findFragmentById(R.id.map);
	    mapFragment.getMapAsync(this);
	    
	    /*Bundle b = getIntent().getExtras();
		String startGeoPoint = b.getString("startGeoPoint");
		String stopGeoPoint = b.getString("stopGeoPoint");
		
		getWay2(startGeoPoint, stopGeoPoint);
		
		Log.d("TEST_TAG", startGeoPoint);
		Log.d("TEST_TAG", stopGeoPoint);
		Log.d("TEST_TAG", "%20Москва,%20Большая%20Академическая%20улица,%20д5с4%20");
		Log.d("TEST_TAG", "%20Москва,%20Авиационный%20переулок,%20д4с4%20");
		getWay2("%20Москва,%20Большая%20Академическая%20улица,%20д5с4%20", "%20Москва,%20Авиационный%20переулок,%20д4с4%20");
		getWay2(startGeoPoint, stopGeoPoint);
	    
		
	    getWay("1415", "140199");
	    String str_test = getAddrFromServer("1415", "140199");
	    str_test = str_test.replace("\t", "%20");
	    str_test = str_test.replace(" ", "%20");
	    getWay2("Montreal", str_test);*/

	}
	 
	@Override
	public void onMapReady(final GoogleMap map) {	
		Bundle b = getIntent().getExtras();
		String startGeoPoint = b.getString("startGeoPoint");
		String stopGeoPoint = b.getString("stopGeoPoint");
		getWay(startGeoPoint, stopGeoPoint, map);
	}
	
	/*
	public String getAddrFromServer(String city, String addr){
		//Запрос к сервису
		RestAdapter restAdapter = new RestAdapter.Builder()
		    .setEndpoint("http://geo.viz-labs.ru")
		    .setLogLevel(RestAdapter.LogLevel.FULL)
		    .build();
		ServerPoints routeService = restAdapter.create(ServerPoints.class);
		Response routeResponse = routeService.getPoint(city, addr);		 
		String str = new String(((TypedByteArray) routeResponse.getBody()).getBytes());
		str = str.substring(str.indexOf(":")+1, str.indexOf("<br/>"));
		return str;
	 }
	
	public void getWay2(String startGeoPoint, String stopGeoPoint){
		//Запрос к сервису
		RestAdapter restAdapter = new RestAdapter.Builder()
	    	.setEndpoint("https://maps.googleapis.com")
	    	.setLogLevel(RestAdapter.LogLevel.FULL)
	    	.build();
		RouteApi routeService = restAdapter.create(RouteApi.class);
		RouteResponse routeResponse = routeService.getRoute(startGeoPoint, stopGeoPoint, true, "ru");
	 }
	 */
	public void getWay(String startGeoPoint, String stopGeoPoint, GoogleMap map){
		//Запрос к сервису
		RestAdapter restAdapter = new RestAdapter.Builder()
		    .setEndpoint("https://maps.googleapis.com")
		    .setLogLevel(RestAdapter.LogLevel.FULL)
		    .build();
		RouteApi routeService = restAdapter.create(RouteApi.class);
		RouteResponse routeResponse = routeService.getRoute(startGeoPoint, stopGeoPoint, true, "ru");
		
		//Обработка ответа. Отрисовка маршрута и маркеров
		List<LatLng> mPoints = PolyUtil.decode(routeResponse.getPoints());
		PolylineOptions line = new PolylineOptions();
        line.width(4f).color(Color.BLUE);
        LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
        for (int i = 0; i < mPoints.size(); i++) {
            if (i == 0) {
                MarkerOptions startMarkerOptions = new MarkerOptions()
                        .position(mPoints.get(i))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.house_flag2));
                map.addMarker(startMarkerOptions);
            } else if (i == mPoints.size() - 1) {
                MarkerOptions endMarkerOptions = new MarkerOptions()
                        .position(mPoints.get(i))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.house_flag2));
                map.addMarker(endMarkerOptions);
            }
            line.add(mPoints.get(i));
            latLngBuilder.include(mPoints.get(i));
        }
        map.addPolyline(line);
        
        //Перемещение камеры таким образом, чтобы оба маркера было видно
        int size = getResources().getDisplayMetrics().widthPixels;
        LatLngBounds latLngBounds = latLngBuilder.build();
        CameraUpdate track = CameraUpdateFactory.newLatLngBounds(latLngBounds, size, size, 25);
        map.moveCamera(track);
		 
	 }
	 
	 public interface RouteApi {
		    @GET("/maps/api/directions/json")
		    RouteResponse getRoute(
		            @Query(value = "origin", encodeValue = false) String position,
		            @Query(value = "destination", encodeValue = false) String destination,
		            @Query("sensor") boolean sensor,
		            @Query("language") String language);
		}
	 /*
	 public interface ServerPoints {
		    @GET("/navidb/search.php")
		    Response getPoint(
		            @Query("city") String city,
		            @Query("addr") String addr);
		}
	 */
	 public class RouteResponse {
		 	//Класс построен в соответствие со структурой ответа сервиса:
		 	//{routes}:
		 	// ...
		 	//	{overview_polyline}:
		 	//		{points}:*Нужная нам строка*	

		    public List<Route> routes;

		    public String getPoints() {
		        return this.routes.get(0).overview_polyline.points;
		    }

		    class Route {
		        OverviewPolyline overview_polyline;
		    }

		    class OverviewPolyline {
		        String points;
		    }
		}
	
}


