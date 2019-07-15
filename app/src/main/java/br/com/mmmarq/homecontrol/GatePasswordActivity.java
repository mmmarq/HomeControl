package br.com.mmmarq.homecontrol;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class GatePasswordActivity extends Activity {

	private EditText t;
	private ProgressDialog progress;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gate_password);
		// Show the Up button in the action bar.
		setupActionBar();
		t = (EditText) findViewById(R.id.passwordTextView);
		progress = new ProgressDialog(this);
	}

	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.gate_password, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onSendButtonClick(View view){
   		/*Check network availability*/
		if ( ServerCommands.checkConnectivity(getApplicationContext()) ){
			AsyncGateOpener runner = new AsyncGateOpener();
   		    runner.execute("gate.open|"+t.getText().toString());
   		}else{
   			Toast.makeText(getApplicationContext(), getString(R.string.connection_error), Toast.LENGTH_LONG).show();
   			finish();
   		}
	}
	
	public void setComponentStatus(boolean status){
		t.setEnabled(status);
	}
	
    private class AsyncGateOpener extends AsyncTask<String, String, String> {
    	
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
			progress.dismiss();
   			if (commandResult.equals("ok")){
   				setComponentStatus(true);
   				Toast.makeText(getApplicationContext(), R.string.gate_opening_ok, Toast.LENGTH_LONG).show();
   			}else{
   				Toast.makeText(getApplicationContext(), R.string.gate_opening_fail, Toast.LENGTH_LONG).show();			
   			}
	   		finish();
    	}
    }
}
