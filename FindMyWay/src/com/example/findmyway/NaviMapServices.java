package com.example.findmyway;

import java.util.List;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Query;

public class NaviMapServices {
	
	public static List<LatLng> getPoints(String startGeoPoint, String stopGeoPoint){
		try {
			RestAdapter restAdapter = new RestAdapter.Builder()
					.setEndpoint("https://maps.googleapis.com")
					.setLogLevel(RestAdapter.LogLevel.FULL)
					.setClient(new MyUrlConnectionClient()).build();
			RouteApi routeService = restAdapter.create(RouteApi.class);
			RouteResponseMaps routeResponse = routeService.getRoute(
					startGeoPoint, stopGeoPoint);

			return PolyUtil.decode(routeResponse.getPoints());
		} catch (Exception e) {
			return null;
		}
	 }
	
	public static String getAddr(String city, String navi) {
		try {
			RestAdapter restAdapter = new RestAdapter.Builder()
					.setEndpoint("http://geo.viz-labs.ru")
					.setLogLevel(RestAdapter.LogLevel.FULL)
					.setClient(new MyUrlConnectionClient()).build();
			RouteApi routeService = restAdapter.create(RouteApi.class);
			RouteResponseNavi routeResponse = routeService.getAddr(city, navi);

			return routeResponse.getAddr();
		} catch (Exception e) {
			return null;
		}
	}
	
	private interface RouteApi {
	    @GET("/maps/api/directions/json")
	    RouteResponseMaps getRoute(
	            @Query(value = "origin", encodeValue = false) String position,
	            @Query(value = "destination", encodeValue = false) String destination);
	    
	    @GET("/navidb/search.php")
	    RouteResponseNavi getAddr(
	            @Query("city") String city,
	            @Query("addr") String addr);
	}
	
	private class RouteResponseMaps {
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
	
	private class RouteResponseNavi {
	    private String city_en;
	    private String street_en;
	    private String number_en;
	    
	    public String getAddr(){
	    	return city_en+","+street_en+","+number_en;
	    }
	    
	}

}
