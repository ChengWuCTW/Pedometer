/*
 * Simple Pedometer Version 1.0.0
 * 
 * Wednesday, March 2nd, 2016.
 * 
 * Developers: Samudraneel Bhattacharyya, Cheng-Tung Wu.
 * 
 * Contributors: Isaac Sy, Brian Le.
 * 
 * APPLICATION DESCRIPTION
 * 
 * A simple free, easy to use Pedometer application.
 * 
 * The pedometer keeps track of your steps and displays it to the user. It also calculates the total distance the user has walked
 * and the total calories the user has burned.
 * 
 * It is very easy to use! Simply start walking as you would with your phone in the pocket (or anywhere!) as soon as you turn the 
 * pedometer on.  * This must be done because the pedometer will begin calibration immediately upon activation. The calibration will 
 * last for approximately 3 seconds after which it will begin counting the users steps.
 * 
 * 
 */







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
	
	
	//----------------------------INITIALIZING ALL VARIABLES, BUTTON, TEXTVIEWS AND DATA STRUCTURES -------------------------------//
	
	//---------------------------VARIABLES--------------------------//
	
	static float lowPassOut_Z; // low-pass filter output from the z axis
	static float lowPassOut_Y; // low-pass filter output from the y axis
	static float lowPassOut_X; // low-pass filter output from the x axis
	static float magnitude; // magnitude of the xyz values
	static boolean pauseboo = false;
	static float[] oldData = { 0.00f, 0.00f, 0.00f };
	static int state = 0; // stores current state of step
	static int steps = 0; // records number of steps
	static float alpha = 0.762f; //average step length worldwide (lazy coding)
	static float beta = 0.05f; //average calories lost per step;

	//---------------------------BUTTONS---------------------------//
	
	static Button refresh; // reference to the refresh button
	static Button pause_btn;
	static Button resume_btn;

	//-------------------------TEXTVIEW----------------------------//
	
	static TextView stepstv; // textview displaying number of steps
	static TextView distancetv; // textview displaying distance traveled
	static TextView caloriesBurnedtv; // textview displaying calories burned
	static TextView temp, temp1, temp2, temp3, temp4, temp5, temp6; //TEMPORARY VARIABLES
	
	//-----------------------DATA STRUCTURES-----------------------//

	static ArrayList<Float> dataPoints = new ArrayList<Float>(); // initializes arraylist for data points
	static ArrayList<Float> calibration = new ArrayList<Float>(); // initializes arraylist for data points
	static float threshold = 0.0f; //the calibrated average of the individual
	static float crossover = 0.0f; //the calculated average after calibration
	
	//----------------------END OF VARIABLES------------------------------------------------------//

	

	//------------------------------------METHODS USED FOR APPLICATION---------------------------------//
	
	
	// CALCULATES DISTANCE TRAVELED
	static public float distance(int n) {
		float temp;
		temp = n*steps;
		return temp;
	}
	
	
	// CALCULATES CALORIES BURNED
	static public float calories (int k) {
		float temp;
		temp = k*beta;
		return temp;
	}
	

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
	
	//--------------------------END OF METHODS---------------------------------------------//
	
	
	
	//------------------------PROGRAMMING INSIDE OF MAINACTIVITY STARTS HERE------------------------------------------//

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

			// temp variables are used to move other text down because we weren't
			// too sure how to do it efficiently
			
			// this is very lazy programming
			
			//temp = new TextView (rootView.getContext());
			//temp1 = new TextView (rootView.getContext());
			//temp2 = new TextView (rootView.getContext());
			//temp3 = new TextView (rootView.getContext());
			//temp4 = new TextView (rootView.getContext());
			//temp5 = new TextView (rootView.getContext());
			//temp6 = new TextView (rootView.getContext());
			
			
			// Steps textview orientation, size, colour
			stepstv = new TextView (rootView.getContext());
			stepstv.setGravity(Gravity.CENTER);
			stepstv.setTextSize(20);
			stepstv.setTextColor(Color.BLACK);
			
			
			// Distance textview orientation, size, colour
			distancetv = new TextView (rootView.getContext());
			distancetv.setGravity(Gravity.TOP|Gravity.END);
			distancetv.setX(-42f);
			distancetv.setY(135f);
			distancetv.setTextSize(15);
			distancetv.setTextColor(Color.BLACK);
			
			
			// Calories textview orientation, size, colour
			caloriesBurnedtv = new TextView (rootView.getContext());
			caloriesBurnedtv.setGravity(Gravity.TOP|Gravity.START);
			caloriesBurnedtv.setTextSize(15);
			caloriesBurnedtv.setX(18f);
			caloriesBurnedtv.setY(135f);
			caloriesBurnedtv.setTextColor(Color.BLACK);
			

			// Setting initial text on screen
			stepstv.setText("Steps: 0");
			distancetv.setText("Distance walked: 0 m");
			caloriesBurnedtv.setText("Calories burned: 0.0 cal");

			
			// Adding text on screen in order specified below
			//rl.addView(temp);
			//rl.addView(temp1);
			//rl.addView(temp2);
			//rl.addView(temp3);
			//rl.addView(temp4);
			//rl.addView(temp5);
			//rl.addView(temp6);
			rl.addView(stepstv);
			rl.addView(distancetv);
			rl.addView(caloriesBurnedtv);
			
			
			// Buttons being initialized and their functions being defined
			
			
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
			
			
			// Sensor manager is being called for and Linear Acceleration sensor being selected

			SensorManager sensorManager = (SensorManager) rootView.getContext().getSystemService(SENSOR_SERVICE); 
			Sensor accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION); 


			SensorEventListener a = new AccelerationEventListener(stepstv, distancetv, caloriesBurnedtv);
			sensorManager.registerListener(a, accSensor, SensorManager.SENSOR_DELAY_FASTEST);

			return rootView;
		}
	}
	
	
	// Acceleration Event Listener (class that actually does all the tasks)

	static public class AccelerationEventListener implements SensorEventListener {
		
		
		// Initialize all the required TextView variables
		TextView output_steps;
		TextView output_distance;
		TextView output_caloriesBurned;

		public AccelerationEventListener(TextView output_count, TextView output_walked, TextView output_burned) {
			
			output_steps = output_count;
			output_distance = output_walked;
			output_caloriesBurned = output_burned;
			
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
				
				
				// Low-pass filter is used here to reduce environmental noise
				oldData = event.values;
				lowpass(oldData, event.values);

			}
			
			magnitude_of_xyz(lowPassOut_X, lowPassOut_Y, lowPassOut_Z);

			
			// Calibration occurs here
			// For future reference: IMPROVE THE CALIBRATION
			// Potential ideas: REMOVE THE OLDEST INPUT WITH THE NEWEST INPUT
			// Potential ideas: IMPLEMENT A NEURAL NETWORK
			// Potential ideas: USE A GPS TO CALCULATE DISTANCE RATHER THAN AN APPROXIMATION
			if (calibration.size() <= 100) {
				calibration.add(magnitude);
				threshold = averageData(calibration);
				if (threshold < 2) {
					calibration.clear();
				}
			}
			
			
			
			// Pedometer finite state machine calculations occur in this else statement
			else{
				dataPoints.add(magnitude);
				
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
							output_distance.setText("Distance walked: " + String.format("%.3g%n", distance(steps)) + " m");
							output_caloriesBurned.setText("Calories burned: " + String.format("%.1g%n", calories(steps)) + " cal");
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