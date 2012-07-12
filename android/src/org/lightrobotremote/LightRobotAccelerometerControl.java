package org.lightrobotremote;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;


public class LightRobotAccelerometerControl implements SensorEventListener {

	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	/**needed to get the orientation of the screen*/
	private Display mDisplay;
	
	private Handler mHandler;
	
	private LightRobotDataManager mDataManager;
	
	//Fields for the acc data
	private float mData_acc_x = 0.f;
	private float mData_acc_y = 0.f;
	
	private byte mSpeed_acc = 0;
	public static final float UPPER_BORDER_SPEED = 9.79f;
	public static final float LOWER_BORDER_SPEED = 5.0f;
	public static final float THRESHOLD_SPEED = 0.3f;
	
	public static final float PARAMETER_SPEED = -(LightRobotDataManager.SPEED_VALUE_MAX/LOWER_BORDER_SPEED+0.2f);
	
	private byte mDirection_acc = 0;
	
	public static final float UPPER_BORDER_DIRECTION = 7.5f;
	public static final float LOWER_BORDER_DIRECTION = 0.f;
	public static final float THRESHOLD_DIRECTION = 0.5f;
	/*! Determines the parameter to transform from [LOWER_BORDER, UPPER_BORDER] to [0,DIRECTION_VALUE_MAX]
	 * 
	 */
	public static final float PARAMETER_DIRECTION = LightRobotDataManager.DIRECTION_VALUE_MAX/UPPER_BORDER_DIRECTION;

	public LightRobotAccelerometerControl(SensorManager sensorManager, WindowManager windowManager, Handler handler)
	{//TODO:: Add data manager and dont communicate with the activity directly!
		mSensorManager = sensorManager;
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mDisplay = windowManager.getDefaultDisplay();
		
		mHandler = handler;
	}
	
	/** Register the callback function to receive new data from the acc-sensor.
	 * 
	 */
	public void startControlAcc()
	{
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
	}
	
	/** Unregister the callback function to stop receiving data from the acc-sensor.
	 * 
	 */
	
	public void stopControlAcc()
	{
		mSensorManager.unregisterListener(this);
		mData_acc_x = 0.f;
		mData_acc_y = 0.f;
		mHandler.obtainMessage(LightRobotRemoteInterface.MESSAGE_UPDATE_DISPLAY,0,0).sendToTarget();
	}


	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
			return;
		/*
		 * Record the accelerometer data. In this application, we need to
		 * take into account how the screen is rotated with respect to the
		 * sensors (which always return data in a coordinate space aligned
		 * to with the screen in its native orientation).
		 */
		
		float sensorX = 0.f;
		float sensorY = 0.f;

		switch (mDisplay.getRotation()) {
		case Surface.ROTATION_0:
			sensorX = event.values[0];
			sensorY = event.values[1];
			break;
		case Surface.ROTATION_90:
			sensorX = -event.values[1];
			sensorY = event.values[0];
			break;
		case Surface.ROTATION_180:
			sensorX = -event.values[0];
			sensorY = -event.values[1];
			break;
		case Surface.ROTATION_270:
			sensorX = event.values[1];
			sensorY = -event.values[0];
			break;	
		}
		
		mData_acc_x = sensorX;
		mData_acc_y = sensorY;
		
		mSpeed_acc = convertSensorYToSpeed(sensorY);
		mDirection_acc = convertSensorXToDirection(sensorX);
		
		mHandler.obtainMessage(LightRobotRemoteInterface.MESSAGE_UPDATE_DATA,0,0).sendToTarget();
		mHandler.obtainMessage(LightRobotRemoteInterface.MESSAGE_UPDATE_DISPLAY,0,0).sendToTarget();

	}
	
	public float getSensorX()
	{
		return mData_acc_x;
	}
	
	public float getSensorY()
	{
		return mData_acc_y;
	}
	
	public byte getSpeedAcc()
	{
		return mSpeed_acc;
	}
	
	public byte getDirectionAcc()
	{
		return mDirection_acc;
	}
	
	
	/** First simple idea -> Sensor Values from 0 (parallel to ground) to 9.5 (orthogonal to ground) are linear transformed 
	 * to a value between 127 and -127.
	 * @param sensorY the value from the y-axis of the acc-sensor.
	 * @return a value between 127 and -127.
	 */
	private byte convertSensorYToSpeed(float sensorY)
	{
		if(sensorY < 0.f && sensorY > -LOWER_BORDER_SPEED)
			return LightRobotDataManager.SPEED_VALUE_MAX;
		else if(sensorY > UPPER_BORDER_SPEED)
			return -LightRobotDataManager.SPEED_VALUE_MAX;
		else if (( (sensorY <= (LOWER_BORDER_SPEED + THRESHOLD_SPEED)) && (sensorY >= (LOWER_BORDER_SPEED - THRESHOLD_SPEED)))
				|| (sensorY <= -LOWER_BORDER_SPEED))
			return 0;
		else
			return(byte)(PARAMETER_SPEED * sensorY + ((float)LightRobotDataManager.SPEED_VALUE_MAX)-1);
	}
	
	
	/** First simple idea -> Sensor Values from 7.0 (tilted to the left) to -7.0 (tilted to the right) are linear transformed 
	 * to a value between 127 and -127.
	 * @param sensorX the value from the x-axis of the acc-sensor.
	 * @return a value between 127 and -127.
	 */
	
	private byte convertSensorXToDirection(float sensorX)
	{
		if(sensorX >= UPPER_BORDER_DIRECTION)
			return -127;
		else if(sensorX <= -UPPER_BORDER_DIRECTION)
			return 127;
		else if((sensorX <= THRESHOLD_DIRECTION) && (sensorX >= -THRESHOLD_DIRECTION))
			return 0;
		else
			return (byte)(PARAMETER_DIRECTION * sensorX);
	}


	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// nothing to do here
	}

}
