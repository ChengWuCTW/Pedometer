package android.pedometerapplication;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
	static float lowPassOut_Z;
	static float lowPassOut_Y;
	static float lowPassOut_X;
	static float magnitude;
	static Button refresh; // reference to the refresh button
	static Button pause_btn;
	static Button resume_btn;
	static boolean pauseboo = false;
	static float[] oldData = { 0.00f, 0.00f, 0.00f };
	static int state = 0; // stores current state of step
	static int steps = 0; // records number of steps
	static TextView stepstv; //textview displaying number of steps
	static TextView temp; //TEMPORARY VARIABLE
	static TextView temp1,temp2,temp3,temp4,temp5,temp6;
	static ArrayList<Float> dataPoints = new ArrayList<Float>(); // initializes arraylist for data points

	//static TextView stateTV;
	
	static ArrayList<Float> calibration = new ArrayList<Float>(); // initializes arraylist for data points
	static float threshold = 0.0f;
	static float crossover = 0.0f;
	

	//LOWPASS FILTER USED TO REDUCE NOISE FROM LINEAR ACCELEROMETER
	static public float[] lowpass(float[] oldIn, float[] in) {

		float[] out = new float[in.length];
		final float alpha = 0.15f;

		for (int i = 0; i < oldIn.length; i++) {
			out[i] = in[i] + alpha * (oldIn[i] - in[i]);
		}

		lowPassOut_Z = out[2];
		lowPassOut_Y = out[1];
		lowPassOut_X = out[0];

		return out;
	}
	
	
	
	//COMPUTES THE AVERAGE OF THE ARRAYLIST VALUES
	public static float averageData(ArrayList<Float> data) {
		float sum = 0;
		for (int i = 0; i < data.size(); i++) {
			sum += data.get(i);
		}
		return sum/data.size();
	}
	
	
	
	//CALCULATES THE MAGNITUDE OF 3 AXIS
	public static float magnitude_of_xyz(float a, float b, float c) {
		float temp = 0.0f;
		
		temp = a*a + b*b + c*c;
		
		magnitude = (float)Math.sqrt(temp);
		
		return magnitude;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static class PlaceholderFragment extends Fragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false); 
																						
			
			FrameLayout rl = (FrameLayout) rootView.findViewById(R.id.rl);

			
			temp = new TextView (rootView.getContext());
			temp1 = new TextView (rootView.getContext());
			temp2 = new TextView (rootView.getContext());
			temp3 = new TextView (rootView.getContext());
			temp4 = new TextView (rootView.getContext());
			temp5 = new TextView (rootView.getContext());
			temp6 = new TextView (rootView.getContext());
			
			
			stepstv = new TextView (rootView.getContext());
			stepstv.setGravity(Gravity.CENTER);
			stepstv.setTextSize(20);
			stepstv.setTextColor(Color.WHITE);


			stepstv.setText("OVERRIDE");

			rl.addView(temp);
			rl.addView(temp1);
			rl.addView(temp2);
			rl.addView(temp3);
			rl.addView(temp4);
			rl.addView(temp5);
			rl.addView(temp6);
			rl.addView(stepstv);
			
			
			refresh = (Button) rootView.findViewById(R.id.Refresh);
			refresh.setOnClickListener(new View.OnClickListener() {
				
				
				@Override
				public void onClick(View v) { // reset data on click
					calibration.clear();
					steps = 0;
				}
			});
			
			pause_btn = (Button) rootView.findViewById(R.id.pause);
			pause_btn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					pauseboo = true;
				}
			});
			
			resume_btn = (Button) rootView.findViewById(R.id.resume);
			resume_btn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					pauseboo = false;
					
				}
			});

			SensorManager sensorManager = (SensorManager) rootView.getContext().getSystemService(SENSOR_SERVICE); // request
																													// sensor
																													// manager
			Sensor accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION); // get
																								// reference
																								// to
																								// acceleration
																								// sensor


			SensorEventListener a = new AccelerationEventListener(stepstv);
			sensorManager.registerListener(a, accSensor, SensorManager.SENSOR_DELAY_FASTEST);

			return rootView;
		}
	}
	
	

	static public class AccelerationEventListener implements SensorEventListener {
		
		TextView output_steps;

		public AccelerationEventListener(TextView output) {
			
			output_steps = output;
			
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
				
				oldData = event.values;
				lowpass(oldData, event.values);

			}
			
			magnitude_of_xyz(lowPassOut_X, lowPassOut_Y, lowPassOut_Z);

			
			
			if (calibration.size() <= 100) {
				calibration.add(magnitude);
				threshold = averageData(calibration);
				if (threshold < 2) {
					calibration.clear();
				}
			}
			
			
			else{
				dataPoints.add(magnitude);

				//temp.setText("Magnitude is: "+ magnitude);
				temp.setText("");

				if (dataPoints.size() > 4) {
					
					crossover = averageData(dataPoints);

					refresh.setText("Refresh");
				
					if(!pauseboo){
					switch (state) {
					case 0:{
						if (crossover < threshold){
							state = 1;
						}
					}
					case 1: {
						if (crossover > threshold) {
							state = 2;
							//steps++;
							//output_steps.setText("Steps: " + String.valueOf(steps));
						}
						break;
					}
					case 2:{
						if (crossover < threshold){
							state = 3;
						}
					}
					case 3: {
						if (crossover > threshold) {
							state = 0;
							steps++;
							output_steps.setText("Steps: " + String.valueOf(steps));
						}
						break;
					}
					default:
						break;
					}
					}
					dataPoints.clear(); // clear data points
				}
			}
			}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

	}

}