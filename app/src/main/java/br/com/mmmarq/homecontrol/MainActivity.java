package br.com.mmmarq.homecontrol;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.*;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;
import android.widget.ImageView;

public class MainActivity extends Activity {
	private boolean lightStatus = false;
	private boolean operationMode;
	private static boolean AUTOMATIC = true;
	private static boolean MANUAL = false;
	
	private ImageButton s;
	private ImageButton b;
	private TextView t;
	private TextView h;
    private ImageView tempImage;
    private ImageView humidImage;
	private RadioButton r1;
	private RadioButton r2;
	private ProgressDialog progress;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
   		s = (ImageButton) findViewById(R.id.lightButton);
   		b = (ImageButton) findViewById(R.id.gateButton);
   		t = (TextView) findViewById(R.id.TemperatureTextView);
   		h = (TextView) findViewById(R.id.HumidityTextView);
   		r1 = (RadioButton) findViewById(R.id.automaticRadio);
   		r2 = (RadioButton) findViewById(R.id.manualRadio);
        tempImage = (ImageView) findViewById(R.id.temperatureImage);
        humidImage = (ImageView) findViewById(R.id.humidityImage);
   		progress = new ProgressDialog(this);

   		if ( ServerCommands.checkConnectivity(getApplicationContext()) ){
   			AsyncUpdateStatus runner = new AsyncUpdateStatus();
   		    runner.execute("status.all");
   		}else{
			s.setEnabled(false);
			b.setEnabled(false);
			r1.setEnabled(false);
			r2.setEnabled(false);
			Toast.makeText(getApplicationContext(), getString(R.string.connection_error), Toast.LENGTH_LONG).show();
   		}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
        
    public void onLightStateChange(View view){
        lightStatus = !lightStatus;
    	
   		if ( ServerCommands.checkConnectivity(getApplicationContext()) ){
   			AsyncUpdateStatus runner = new AsyncUpdateStatus();
   			if (lightStatus){
   	   		    runner.execute("light.on");
                s.setImageResource(R.drawable.light_on);
            }else{
   	   		    runner.execute("light.off");
                s.setImageResource(R.drawable.light_off);
            }
   		}else{
   			Toast.makeText(getApplicationContext(), getString(R.string.connection_error), Toast.LENGTH_LONG).show();
   		}
 	}

    public void onOperationModeChange(View view){
    	RadioGroup operationModeGroup = (RadioGroup) findViewById(R.id.operationModeGroup);
		AsyncUpdateStatus runner = new AsyncUpdateStatus();

		if ( findViewById(R.id.automaticRadio).equals(findViewById(operationModeGroup.getCheckedRadioButtonId()))){
			if (operationMode != AUTOMATIC){
				if ( ServerCommands.checkConnectivity(getApplicationContext()) ){
					runner.execute("set.automatic");
				}else{
					r1.setChecked(false);
					r2.setChecked(true);
				}
			}
		}else{
			if (operationMode != MANUAL){
				if ( ServerCommands.checkConnectivity(getApplicationContext()) ){
					runner.execute("set.manual");
				}else{
					r1.setChecked(true);
					r2.setChecked(false);
				}
			}
		}
    }
    
    public void onGateButtonClick(View view){
    	Intent myIntent = new Intent(this, GatePasswordActivity.class);
    	this.startActivity(myIntent);
    }
    
    /**
     * On selecting action bar icons
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
        case R.id.action_refresh:
            if ( ServerCommands.checkConnectivity(getApplicationContext()) ){
       			AsyncUpdateStatus runner = new AsyncUpdateStatus();
       		    runner.execute("status.all");
            }else{
    			s.setEnabled(false);
    			b.setEnabled(false);
    			r1.setEnabled(false);
    			r2.setEnabled(false);
    			Toast.makeText(getApplicationContext(), getString(R.string.connection_error), Toast.LENGTH_LONG).show();
            }
            return true;
        case R.id.action_camera:
        	Intent myIntent = new Intent(this, ShowCameraActivity.class);
        	this.startActivity(myIntent);
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    public void setComponentStatus(boolean status){
		s.setEnabled(status);
		b.setEnabled(status);
		r1.setEnabled(status);
		r2.setEnabled(status);
    }
    
    private class AsyncUpdateStatus extends AsyncTask<String, String, String> {
    	
    	protected String doInBackground(String... params) {
        	return ServerCommands.sendServerCommand(params[0],getApplicationContext());
    	}

    	protected void onPreExecute() {
			setComponentStatus(false);
    		
   			progress.setMessage(getString(R.string.check_status));
   			progress.setCancelable(false);
   			progress.setCanceledOnTouchOutside(false);
   			progress.show();
    	}

    	protected void onPostExecute(String commandResult) {
    		if (! commandResult.equals("")){
    			setComponentStatus(true);
    			if ( commandResult.split(" ")[0].equals("on") ){
    				lightStatus = true;
					s.setImageResource(R.drawable.light_on);
    			}else{
    				lightStatus = false;
                    s.setImageResource(R.drawable.light_off);
    			}
    			if ( commandResult.split(" ")[1].equals("automatic") ){
    				r1.setChecked(true);
    				r2.setChecked(false);
    				operationMode = AUTOMATIC;
    			}else{
    				r1.setChecked(false);
    				r2.setChecked(true);
    				operationMode = MANUAL;
    			}
    	
    			t.setText( commandResult.split(" ")[2] + (char) 0x00B0 + "C");
                float f = Float.parseFloat(commandResult.split(" ")[2]);
				if ( f < 15){
					tempImage.setImageResource(R.drawable.temp_low);
				}else{
					if (f < 26){
                        tempImage.setImageResource(R.drawable.temp_mid);
					}else{
                        if (f < 33) {
                            tempImage.setImageResource(R.drawable.temp_mhigh);
                        }else{
                            tempImage.setImageResource(R.drawable.temp_high);
                        }
					}
				}
    			h.setText( commandResult.split(" ")[3] + "%");
                f = Float.parseFloat(commandResult.split(" ")[3]);
                if (f < 30){
                    humidImage.setImageResource(R.drawable.humidity_low);
                }else{
                    if (f < 35){
                        humidImage.setImageResource(R.drawable.humidity_mid);
                    }else{
                        humidImage.setImageResource(R.drawable.humidity_good);
                    }
                }

    			Toast.makeText(getApplicationContext(), getString(R.string.update_completed), Toast.LENGTH_SHORT).show();
    		}else{
    			Toast.makeText(getApplicationContext(), getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
       		}
			progress.dismiss();
    	}
    }
}

