package com.example.findmyway;

import java.util.List;

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

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ZoomControls;


public class MainMenu extends FragmentActivity implements OnMapReadyCallback {

	private static final String city_code = "7495";
	private static final int start_zoom = 17;
	
	private GoogleMap map;
	private boolean initialized = false;
	private LatLng LastLatLng;
	
	private ZoomControls zoom;
	private ImageButton go;
	private ImageButton home;
	private EditText addr;
	private ImageView aim_bounds;
	private ImageView aim_pic;
	private ImageView piece;
	
	private final float N = 8;
	private int circle_size;
	private float last_x, last_y;
	private boolean is_rotating;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.main_menu);
	    
	    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	    StrictMode.setThreadPolicy(policy);
	    
	    //getActionBar().hide();
	    
	    go = (ImageButton)findViewById(R.id.GoButton);
	    home = (ImageButton)findViewById(R.id.HomeButton);
	    aim_bounds = (ImageView)findViewById(R.id.aim_bounds);
	    aim_pic = (ImageView)findViewById(R.id.aim_pic);
	    piece = (ImageView)findViewById(R.id.piece);
	    zoom = (ZoomControls)findViewById(R.id.zoomControls1);
	    addr = (EditText)findViewById(R.id.Addr);

	    //—крываем клавиатуру при фокусе non-EditText полей
    	setupUI(findViewById(R.id.StartLayout));
    	
    	//«адаем размеры колеса
	    setUpBounds();
	    
	    MapFragment mapFragment = (MapFragment) getFragmentManager()
	    		.findFragmentById(R.id.map);
	    mapFragment.getMapAsync(this);
		
	};
	
	private String getAddr(String city, String navi){
		String str = NaviMapServices.getAddr(city, navi);
		if(str==null) return null;
		Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
	    str = str.replace("\t", "%20");
		str = str.replace(" ", "%20");
		return str;
	}
	
	@Override
	public void onMapReady(final GoogleMap _map) {
		
		map = _map;
		map.setMyLocationEnabled(true);
		map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		map.getUiSettings().setMyLocationButtonEnabled(false);
		initialized = true;		
		
		//«апоминаем свою последнюю позицию при смене местоположени€
		map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
			@Override
			public void onMyLocationChange(Location arg0) {
				if(LastLatLng == null){
					LastLatLng = new LatLng(arg0.getLatitude(), arg0.getLongitude());
					CameraUpdate start_pos = CameraUpdateFactory.newLatLngZoom(LastLatLng, start_zoom);
					map.animateCamera(start_pos);
				}
				else {
					LastLatLng = null;
					LastLatLng = new LatLng(arg0.getLatitude(), arg0.getLongitude());
				}
			}
		});
	
		go.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(addr.getText().toString().length()!=6) return;
				
				//«апрашиваем адрес по 6ти-значному коду Navi 
				String navi = getAddr(city_code, addr.getText().toString());
				if(navi==null){
					Toast.makeText(getApplicationContext(), "Connection error", Toast.LENGTH_LONG).show();
					return;
				}
				
				map.clear();
				buildWay(LastLatLng.latitude+","+LastLatLng.longitude, navi, map);
				aim_pic.setVisibility(View.GONE);
				piece.setVisibility(View.GONE);
				aim_bounds.setVisibility(View.GONE);
			}
		});
		
		home.setOnClickListener(new View.OnClickListener() {		
			@Override
			public void onClick(View v) {
				try{
					map.clear();
					aim_pic.setVisibility(View.VISIBLE);
			        piece.setVisibility(View.VISIBLE);
			        aim_bounds.setVisibility(View.VISIBLE);
			        
					if(LastLatLng == null){
						Toast.makeText(getApplicationContext(), "Current loc is undefined", Toast.LENGTH_LONG).show();
					} else {
						CameraUpdate pos = CameraUpdateFactory.newLatLngZoom(LastLatLng, map.getCameraPosition().zoom);
						map.animateCamera(pos);
					}
				} catch (Exception e){
					Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		});
		
		zoom.setOnZoomInClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CameraUpdate pos = CameraUpdateFactory.zoomIn();
				map.animateCamera(pos);
			}
		});
		 
		zoom.setOnZoomOutClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CameraUpdate pos = CameraUpdateFactory.zoomOut();
				map.animateCamera(pos);
			}
		});
	}
	
	private void buildWay(String startGeoPoint, String stopGeoPoint, GoogleMap map){
		List<LatLng> mPoints = NaviMapServices.getPoints(startGeoPoint, stopGeoPoint);
		PolylineOptions line = new PolylineOptions();
        line.width(4f).color(Color.BLUE);
        LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
        for (int i = 0; i < mPoints.size(); i++) {
            if (i == 0) {
                MarkerOptions startMarkerOptions = new MarkerOptions()
                        .position(mPoints.get(i))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.house_flag2a));
                map.addMarker(startMarkerOptions);
            } else if (i == mPoints.size() - 1) {
                MarkerOptions endMarkerOptions = new MarkerOptions()
                        .position(mPoints.get(i))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.house_flag2b));
                map.addMarker(endMarkerOptions);
            }
            line.add(mPoints.get(i));
            latLngBuilder.include(mPoints.get(i));
        }
        map.addPolyline(line);
        
        //ѕеремещение камеры таким образом, чтобы оба маркера было видно
        int size = getResources().getDisplayMetrics().widthPixels;
        LatLngBounds latLngBounds = latLngBuilder.build();
        CameraUpdate track = CameraUpdateFactory.newLatLngBounds(latLngBounds, size, size, 25);
        map.animateCamera(track);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.map_types, menu);
		return true;
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus){
		circle_size = aim_pic.getHeight();
		
		//–азмеры кнопок верхней панели
		changeLayoutParams(home.getHeight(), home.getHeight(), home);
		changeLayoutParams((int)(go.getHeight()*1.5), go.getHeight(), go);
		
		
		//Ќачальный поворот колеса на -22.5 градуса
		piece.setRotation(-360/N/2);
		aim_pic.setRotation(-360/N/2);
		
		//ќбработка событий касани€ колеса
		aim_bounds.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				//Toast.makeText(getApplicationContext(), "Circle:"+circle_size+", Action:"+event.getAction()+", X:"+event.getX()+", Y:"+event.getY(), Toast.LENGTH_LONG).show();
				float x = event.getX()-circle_size/2;
				float y = circle_size/2-event.getY();
				
				switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:
					float R = circle_size/2;
					float r = R*72/100;
					
					if((x*x+y*y)<=R*R && (x*x+y*y)>=r*r){
						is_rotating = true;
						last_x = x;
						last_y = y;
						return true;
					}
					break;
				case MotionEvent.ACTION_UP:
					if(is_rotating){
						/* ќтслеживание касани€
						if(d_sum<circle_size/100){
						}*/
						is_rotating = false;
						float rot = aim_pic.getRotation();
						float c = 360/N/2;
						rot = rot+c;
						if(rot<0) rot = rot+360;
						if(rot%(360/N)<(360/N/2)){
							aim_pic.setRotation(rot-rot%(360/N)-c);
						} else aim_pic.setRotation(rot+(360/N-rot%(360/N))-c);
						return true;
					}
					break;
				case MotionEvent.ACTION_MOVE:
					if(is_rotating){
						float sign_x=0, sign_y=0, dx = x-last_x, dy = y-last_y;
						if(x>0&&y>0){ //I четверть
							sign_x=1;
							sign_y=-1;
						} else if (x<0&&y>0){ //II четверть
							sign_x=1;
							sign_y=1;
						} else if (x<0&&y<0){ //III четверть
							sign_x=-1;
							sign_y=1;
						} else if (x>0&&y<0){ //IV четверть
							sign_x=-1;
							sign_y=-1;
						}
						float coeff=90;
						float current_r = (float) Math.sqrt((x+last_x)/2*(x+last_x)/2+(y+last_y)/2*(y+last_y)/2);
						R = circle_size/2;
						coeff = coeff*R/current_r;
						aim_pic.setRotation((aim_pic.getRotation()+sign_x*dx*coeff/circle_size+sign_y*dy*coeff/circle_size)%360);
						last_x = x;
						last_y = y;
						return true;
					}
					break;
				}
				
				return false;
			}
		});
	}
	
	private void setUpBounds(){
		DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int w = metrics.widthPixels;
        
        changeLayoutParams(w,w,piece);
    	changeLayoutParams(w,w,aim_bounds);
    	changeLayoutParams(w,w,aim_pic);
	}
	
	private void changeLayoutParams(int w, int h, View v){
		ViewGroup.LayoutParams params = v.getLayoutParams();
    	if(h>0)	params.height = h;
    	if(w>0) params.width = w;
    	v.setLayoutParams(params);
	}
	
	private void hideSoftKeyboard() {
	    InputMethodManager inputMethodManager = (InputMethodManager)  this.getSystemService(Context.INPUT_METHOD_SERVICE);
	    inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
	}
	
	private void setupUI(View view) {

	    //Set up touch listener for non-text box views to hide keyboard.
	    if(!(view instanceof EditText)) {

	        view.setOnTouchListener(new OnTouchListener() {

	            public boolean onTouch(View v, MotionEvent event) {
	                hideSoftKeyboard();
	                return false;
	            }

	        });
	    }

	    //If a layout container, iterate over children and seed recursion.
	    if (view instanceof ViewGroup) {

	        for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

	            View innerView = ((ViewGroup) view).getChildAt(i);

	            setupUI(innerView);
	        }
	    }
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if(!initialized) return super.onOptionsItemSelected(item);
		switch(item.getItemId()){
		case R.id.MAP_TYPE_NORMAL:
			map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			break;
		case R.id.MAP_TYPE_SATELLITE:
			map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			break;
		case R.id.MAP_TYPE_HYBRID:
			map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			break;
		case R.id.MAP_TYPE_TERRAIN:
			map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
			break;
		case R.id.MAP_TYPE_NONE:
			map.setMapType(GoogleMap.MAP_TYPE_NONE);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	    

}
