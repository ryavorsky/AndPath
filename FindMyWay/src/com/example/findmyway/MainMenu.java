package com.example.findmyway;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainMenu extends Activity{

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.main_menu);
	    
	    View v = findViewById(R.id.MainMenuImg);
	    v.setOnClickListener(new View.OnClickListener(){
	    	public void onClick(View view){
	    		Intent intent = new Intent(MainMenu.this, PathMenu.class);	 
	    		startActivity(intent);
	    	}
	    });
	    
	    };

}
