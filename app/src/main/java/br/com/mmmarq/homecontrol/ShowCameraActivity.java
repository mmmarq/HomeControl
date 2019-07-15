package br.com.mmmarq.homecontrol;

import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TabHost;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.os.Handler;
import java.util.ArrayList;

public class ShowCameraActivity extends Activity {

	private ArrayList<ImageView> iv = new ArrayList<ImageView> ();
	private Handler mHandler = new Handler();
	private Runnable mRunnable;
	private TextView imageCounter;
	private int sCount = 0;
	private int rCount = 0;
	private int selectedCamera = 0;
    private TabHost tabHost;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_camera);
		// Show the Up button in the action bar.
		setupActionBar();
        imageCounter = (TextView) findViewById(R.id.frameCount);
        imageCounter.setBackgroundColor(Color.rgb(0, 0, 0));
        imageCounter.setTextColor(Color.rgb(255, 255, 255));
        imageCounter.setText("# " + String.valueOf(sCount) + " / " + String.valueOf(rCount));

		iv.add((ImageView) findViewById(R.id.cam1ImageView));
		iv.get(0).setBackgroundColor(Color.rgb(0, 0, 0));
		iv.add((ImageView) findViewById(R.id.cam2ImageView));
		iv.get(1).setBackgroundColor(Color.rgb(0, 0, 0));
		iv.add((ImageView) findViewById(R.id.cam3ImageView));
		iv.get(2).setBackgroundColor(Color.rgb(0, 0, 0));
		iv.add((ImageView) findViewById(R.id.cam4ImageView));
		iv.get(3).setBackgroundColor(Color.rgb(0, 0, 0));

		tabHost = (TabHost)findViewById(R.id.tabHost);
		tabHost.setup();

		//Camera 1 Tab
		TabHost.TabSpec spec = tabHost.newTabSpec(getString(R.string.cam1_text));
		spec.setContent(R.id.cam1Tab);
		spec.setIndicator(getString(R.string.cam1_text));
		tabHost.addTab(spec);

		//Camera 2 Tab
		spec = tabHost.newTabSpec(getString(R.string.cam2_text));
		spec.setContent(R.id.cam2Tab);
		spec.setIndicator(getString(R.string.cam2_text));
		tabHost.addTab(spec);

		//Camera 3 Tab
		spec = tabHost.newTabSpec(getString(R.string.cam3_text));
		spec.setContent(R.id.cam3Tab);
		spec.setIndicator(getString(R.string.cam3_text));
		tabHost.addTab(spec);

		//Camera 4 Tab
		spec = tabHost.newTabSpec(getString(R.string.cam4_text));
		spec.setContent(R.id.cam4Tab);
		spec.setIndicator(getString(R.string.cam4_text));
		tabHost.addTab(spec);


		/* Save selected camera */
		tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
			@Override
			public void onTabChanged(String arg0) {
				selectedCamera = tabHost.getCurrentTab();
			}
		});

		StrictMode.ThreadPolicy policy = new StrictMode.
                ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

        mRunnable = new Runnable() {
            @Override
            public void run() {                
                {
            		rCount++;
            		if ( ServerCommands.checkConnectivity(getApplicationContext()) ){

            			byte[] commandResult = ServerCommands.sendImageRequest(String.valueOf(selectedCamera+1),getApplicationContext());
                		if (commandResult.length != 0){
                			Bitmap bitmap = BitmapFactory.decodeByteArray(commandResult, 0, commandResult.length); 
                			iv.get(selectedCamera).setImageBitmap(bitmap);
                			sCount++;
                		}else{
                    		Toast.makeText(getApplicationContext(), getString(R.string.camera_image_error_text), Toast.LENGTH_SHORT).show();
                    		mHandler.postDelayed(this, 500);
                		}
            		}else{
                		Toast.makeText(getApplicationContext(), getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
                		mHandler.postDelayed(this, 500);
            		}
        	        imageCounter.setText("# " + String.valueOf(sCount) + " / " + String.valueOf(rCount));
            		// Wait 0.5 seconds
            		mHandler.postDelayed(this, 500);
                }
            }
        };        
        mHandler.post(mRunnable);		
	}
	
	public void onBackPressed() {
		mHandler.removeCallbacks(mRunnable);
	    finish();
	    return;
	} 
	
	public void onPause() {
		mHandler.removeCallbacks(mRunnable);
		finish();
	    super.onPause();
	}
	
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

/*	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.camera_reload, menu);
		return true;
	}*/

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			mHandler.removeCallbacks(mRunnable);
		    finish();
		}
		return super.onOptionsItemSelected(item);
	}
}
