/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lightrobotremote;

import java.util.ArrayList;

import org.lightrobotremote.R;
import org.lightrobotremote.util.ColorHelper;

import android.R.color;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

/**@class LightRobotRemoteInterface
 * This is the main Activity that displays the remote control interface.
 * TODO Add timer for sending
 */
public class LightRobotRemoteInterface extends Activity {
	// Debugging
	private static final String TAG = "LightRobotRemoteInterface";
	private static final boolean D = true;

	// Message types sent from the BluetoothService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;


	//Message types sent from the LightRobotDataManager and the Control parts
	public static final int MESSAGE_UPDATE_DISPLAY = 0;
	public static final int MESSAGE_UPDATE_DATA = 1;
	public static final int MESSAGE_SEND_DATA = 2;

	// Key names received from the BluetoothService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
	private static final int REQUEST_ENABLE_BT = 3;

	//Control checks
	private ListView mControlList;

	//Status which control is active
	public static final int STATUS_NO_CONTROL = 0;
	public static final int STATUS_BUTTON_CONTROL = 1;
	public static final int STATUS_ACC_CONTROL = 2;
	public static final int STATUS_VOICE_CONTROL = 3;


	private final String [] mControlItems = new String[]{"No Control", "Button Control", "Acc Control", "Voice Control"};

	public int mControlStatus = STATUS_NO_CONTROL;
	public int mControlStatusOld = STATUS_NO_CONTROL;

	// Layout Views
	private TextView mTitle;
	private TextView mData_speed;
	private TextView mData_direction;
	//	private TextView mData_acc_x;
	//	private TextView mData_acc_y;
	private TextView mData_voice;
	//private ListView mConversationView;
	//	private EditText mOutEditText;
	//	private Button mSendButton;
	private Button mMoveLeftButton;
	private Button mMoveForwardButton;
	private Button mMoveStopButton;
	private Button mMoveBackwardButton;
	private Button mMoveRightButton;

	private RadioGroup mChoiceMode;
	private RadioGroup mChoiceColorMode;

	private SeekBar mSeekColor;
	private SeekBar mSeekBrightness;
	private ColorHelper mColor = new ColorHelper(0);

	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Array adapter for the conversation thread
	//private ArrayAdapter<String> mConversationArrayAdapter;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothService mBTService = null;

	private LightRobotDataManager mDataManager = null;

	private LightRobotAccelerometerControl mControlAcc = null;
	private SensorManager mSensorManager;
	private WindowManager mWindowManager;

	private LightRobotVoiceControl mControlVoice = null;
	private boolean mIsVoiceControlActive = false;

	private static final int VOICE_ACTIVITY = 1111;

	private long initial_time=0;




	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(D) Log.e(TAG, "+++ ON CREATE +++");

		// Set up the window layout
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);


		mControlList = (ListView) findViewById(R.id.listView1);
		ArrayAdapter<String> ad = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, mControlItems);
		mControlList.setAdapter(ad);
		mControlList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mControlList.setSelection(0);
		mControlList.setItemChecked(0, true);

		mControlList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView arg0, View arg1, int arg2, long arg3)
			{
				mControlStatus = arg2;
				mControlList.setSelection(0);
				updateControlStatus();
			}
		});

		//Set up data display
		mData_speed = (TextView) findViewById(R.id.data_speed);
		mData_speed.setText(String.valueOf(0));

		mData_direction = (TextView) findViewById(R.id.data_direction);
		mData_direction.setText(String.valueOf(0));

		//		mData_acc_x = (TextView) findViewById(R.id.data_acc_x);
		//		mData_acc_x.setText(String.valueOf(0));
		//
		//		mData_acc_y = (TextView) findViewById(R.id.data_acc_y);
		//		mData_acc_y.setText(String.valueOf(0));

		mData_voice = (TextView) findViewById(R.id.data_voice_rec);
		mData_voice.setText("-");

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		//Sensor
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

		mChoiceMode = (RadioGroup) findViewById(R.id.choice_mode);
		mChoiceMode.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId)
				{
				case R.id.choice_mode_0 : mDataManager.setMode(LightRobotDataManager.DRIVE_MODE_REMOTE);
				break;
				case R.id.choice_mode_1 : mDataManager.setMode(LightRobotDataManager.DRIVE_MODE_RANDOM);
				break;
				case R.id.choice_mode_2 : mDataManager.setMode(LightRobotDataManager.DRIVE_MODE_FORWARD);
				break;
				case R.id.choice_mode_3 : mDataManager.setMode(LightRobotDataManager.DRIVE_MODE_BACKWARD);
				break;
				}
			}
		});

		mChoiceColorMode = (RadioGroup) findViewById(R.id.choice_color);
		mChoiceColorMode.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId)
				{
				case R.id.choice_color_0 : mDataManager.setColorMode(LightRobotDataManager.COLOR_MODE_SHINE);
				break;
				case R.id.choice_color_1 : mDataManager.setColorMode(LightRobotDataManager.COLOR_MODE_BLINK);
				break;
				case R.id.choice_color_2 : mDataManager.setColorMode(LightRobotDataManager.COLOR_MODE_RANDOM);
				break;
				case R.id.choice_color_3 : mDataManager.setColorMode(LightRobotDataManager.COLOR_MODE_RANDOM_BLINK);
				break;
				}

			}
		});

		mSeekColor = (SeekBar) findViewById(R.id.seek_color);
		mSeekColor.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				//send only if bar is released TODO: test if its useful to do it after every change
				mDataManager.setColor(mColor);	

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				//nothing to do here
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				//TODO: Alter the color of the seekbar according to progress
				mColor.setColor((byte)progress);
				mDataManager.setColor(mColor);

			}
		});

		mSeekBrightness = (SeekBar) findViewById(R.id.seek_brightness);
		mSeekBrightness.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				//send only if bar is released TODO: test if its useful to do it after every change
				mDataManager.setColor(mColor);	
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				//nothing to do here
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				//TODO: Alter the brightness of the seekbar
				mColor.setColorBrightness((byte)progress);
				mDataManager.setColor(mColor);

			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
		if(D) Log.e(TAG, "++ ON START ++");

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mBTService == null) setupBTService();
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if(D) Log.e(TAG, "+ ON RESUME +");

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
		if (mBTService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't started already
			if (mBTService.getState() == BluetoothService.STATE_NONE) {
				// Start the Bluetooth chat services
				mBTService.start();
			}
			if(!mIsVoiceControlActive)
			{
				mDataManager.resetAllValues();
				mControlStatus = STATUS_NO_CONTROL;
				updateControlStatus();
			}
			else
			{
				mIsVoiceControlActive = false;
				mControlStatus = STATUS_NO_CONTROL;
				updateControlStatus();
			}
		}

	}

	private void setupBTService() {
		Log.d(TAG, "setupService()");

		// Initialize the compose field with a listener for the return key
		//mOutEditText = (EditText) findViewById(R.id.edit_text_out);
		//mOutEditText.setOnEditorActionListener(mWriteListener);

		// Initialize the send button with a listener that for click events
		//		mSendButton = (Button) findViewById(R.id.button_send);
		//		mSendButton.setOnClickListener(new OnClickListener() {
		//			public void onClick(View v) {
		//				// Send a message using content of the edit text widget
		//				TextView view = (TextView) findViewById(R.id.edit_text_out);
		//				String message = view.getText().toString();
		//				sendMessage(message);
		//			}
		//		});

		// Initialize the move_left Button with a listener for events
		mMoveLeftButton = (Button) findViewById(R.id.button_move_left);
		mMoveLeftButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Decrease the direction in which to drive
				mDataManager.alterDirection((byte)-10);
			}
		});

		// Initialize the move_forward Button with a listener for events
		mMoveForwardButton = (Button) findViewById(R.id.button_move_forward);
		mMoveForwardButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Increase the speed of the robot.
				mDataManager.alterSpeed((byte)10);
			}
		});


		//Initialize the move_stop Button with a listener for events
		mMoveStopButton = (Button) findViewById(R.id.button_move_stop);
		mMoveStopButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// stops the robot
				mDataManager.resetMoveValues();
			}
		});
		// Initialize the move_backward Button with a listener for events
		mMoveBackwardButton = (Button) findViewById(R.id.button_move_backward);
		mMoveBackwardButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Decrease the speed of the robot
				mDataManager.alterSpeed((byte)-10);
			}
		});

		// Initialize the move_right Button with a listener for events
		mMoveRightButton = (Button) findViewById(R.id.button_move_right);
		mMoveRightButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Increase the direction in which to drive
				mDataManager.alterDirection((byte)10);
			}
		});

		// Initialize the BluetoothService to perform bluetooth connections
		mBTService = new BluetoothService(this, mBTServiceHandler);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");

		mDataManager = new LightRobotDataManager(mDataHandler);
		mControlAcc = new LightRobotAccelerometerControl(mSensorManager, mWindowManager, mControlAccHandler);
		mControlVoice = new LightRobotVoiceControl(mControlVoiceHandler);
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		if(D) Log.e(TAG, "- ON PAUSE -");

		if(!mIsVoiceControlActive)
		{
			mControlStatus = STATUS_NO_CONTROL;
			mDataManager.setMode((byte)0);
			updateControlStatus();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if(D) Log.e(TAG, "-- ON STOP --");
		mControlStatus = STATUS_NO_CONTROL;
		updateControlStatus();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mControlStatus = STATUS_NO_CONTROL;
		updateControlStatus();
		if (mBTService != null) mBTService.stop();
		if(D) Log.e(TAG, "--- ON DESTROY ---");
	}

	private void ensureDiscoverable() {
		if(D) Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() !=
			BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}


	/** Updates the internal status defining which sort of control is used 
	 * TODO: Parameter new_status: the desired status -> mControlStatus is then obsolete
	 */

	private void updateControlStatus()
	{
		if(mControlStatus != mControlStatusOld)
		{//on status change
			//stop robot
			mDataManager.resetMoveValues();
			//TODO: Update for other controls
			switch(mControlStatusOld)
			{
			case STATUS_ACC_CONTROL:
				mControlAcc.stopControlAcc();
				break;
			}

			mControlStatusOld = mControlStatus;

			switch(mControlStatus)
			{
			case STATUS_NO_CONTROL:
				//uncheck all
				mControlList.setItemChecked(STATUS_NO_CONTROL, true);
				mControlList.setSelection(STATUS_NO_CONTROL);

				break;
			case STATUS_BUTTON_CONTROL:

				break;
			case STATUS_ACC_CONTROL:

				mControlAcc.startControlAcc();
				break;
			case STATUS_VOICE_CONTROL:
				startVoiceRecogService();

				break;
			}
		}
	}

	/**
	 * Sends a message.
	 * @param message  A string of text to send.
	 */
	private void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (mBTService.getState() != BluetoothService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothService to write
			byte[] send = message.getBytes();
			mBTService.write(send);

			// Reset out string buffer to zero and clear the edit text field
			mOutStringBuffer.setLength(0);
			//mOutEditText.setText(mOutStringBuffer);
		}
	}

	private void sendMessage(byte[] message)
	{
		// Check that we're actually connected before trying anything
		if (mBTService.getState() != BluetoothService.STATE_CONNECTED) {
			//Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
			return;
		}
		// Get the message bytes and tell the BluetoothService to write
		mBTService.write(message);
	}

	// The action listener for the EditText widget, to listen for the return key
	//	private TextView.OnEditorActionListener mWriteListener =
	//			new TextView.OnEditorActionListener() {
	//		public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
	//		// If the action is a key-up event on the return key, send the message
	//			if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
	//				String message = view.getText().toString();
	//				sendMessage(message);
	//			}
	//			if(D) Log.i(TAG, "END onEditorAction");
	//			return true;
	//		}
	//	};

	// The Handler that gets information back from the BluetoothService
	private final Handler mBTServiceHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothService.STATE_CONNECTED:
					mTitle.setText(R.string.title_connected_to);
					mTitle.append(mConnectedDeviceName);
					//mConversationArrayAdapter.clear();
					break;
				case BluetoothService.STATE_CONNECTING:
					mTitle.setText(R.string.title_connecting);
					break;
				case BluetoothService.STATE_LISTEN:
				case BluetoothService.STATE_NONE:
					mTitle.setText(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_WRITE:
				//byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				//String writeMessage = new String(writeBuf);
				//mConversationArrayAdapter.add("Me:  " + writeMessage);
				break;
			case MESSAGE_READ:
				//byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				//String readMessage = new String(readBuf, 0, msg.arg1);
				//mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(), "Connected to "
						+ mConnectedDeviceName, Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	private final Handler mDataHandler = new Handler() {
		@Override
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
			case MESSAGE_UPDATE_DISPLAY:

				mData_speed.setText(String.valueOf(mDataManager.getSpeed()));
				mData_direction.setText(String.valueOf(mDataManager.getDirection()));
				break;
			case MESSAGE_SEND_DATA:
				long time_now = System.currentTimeMillis();
				if(time_now - initial_time > 10 || initial_time==0)
				{
					LightRobotRemoteInterface.this.sendMessage(mDataManager.getDataPacket());
					initial_time = System.currentTimeMillis();
				}
				break;

			}
		}
	};

	private final Handler mControlAccHandler = new Handler() {
		@Override
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
			case MESSAGE_UPDATE_DISPLAY:

				//				mData_acc_x.setText(String.valueOf(mControlAcc.getSensorX()));
				//				mData_acc_y.setText(String.valueOf(mControlAcc.getSensorY()));
				break;
			case MESSAGE_UPDATE_DATA:

				//mDataManager.setSpeed(mControlAcc.getSpeedAcc());				
				//mDataManager.setDirection(mControlAcc.getDirectionAcc());
				mDataManager.setSpeedDirection(mControlAcc.getSpeedAcc(),mControlAcc.getDirectionAcc());
				break;
			}
		}
	};

	private final Handler mControlVoiceHandler = new Handler() {
		@Override
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
			case MESSAGE_UPDATE_DATA:
				if(mControlVoice.isCommandCorrect())
				{
					//mDataManager.setMode(mControlVoice.getDriveMode());
					//mDataManager.setColorMode(mControlVoice.getColorMode());
					mDataManager.setModeColor(mControlVoice.getDriveMode(), mControlVoice.getColorMode(), mControlVoice.getColor());
				}
				break;

			case MESSAGE_UPDATE_DISPLAY:
				//mData_voice.setText("Bla");
				if(mControlVoice.isCommandCorrect())
					//mData_voice.setBackgroundColor(R.color.solid_green);
					mData_voice.setBackgroundColor(0xff00ff00);
				else
					//mData_voice.setBackgroundColor(R.color.solid_red);
					mData_voice.setBackgroundColor(0xffff0000);

				mData_voice.setText(" " + mControlVoice.getPart_1() + " " + mControlVoice.getPart_2());

				break;
			}
		}
	};

	private void startVoiceRecogService()
	{
		mIsVoiceControlActive = true;
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "LightRobot Voice Command");
		startActivityForResult(intent, VOICE_ACTIVITY);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(D) Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case VOICE_ACTIVITY:
			if (resultCode == Activity.RESULT_OK)
				mControlVoice.setWordList(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS));
			break;
		case REQUEST_CONNECT_DEVICE_SECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data, true);
			}
			break;
		case REQUEST_CONNECT_DEVICE_INSECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data, false);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupBTService();
			} else {
				// User did not enable Bluetooth or an error occured
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	private void connectDevice(Intent data, boolean secure) {
		// Get the device MAC address
		String address = data.getExtras()
		.getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		// Get the BLuetoothDevice object
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		// Attempt to connect to the device
		mBTService.connect(device, secure);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent serverIntent = null;
		switch (item.getItemId()) {
		case R.id.secure_connect_scan:
			// Launch the DeviceListActivity to see devices and do scan
			serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
			return true;
		case R.id.insecure_connect_scan:
			// Launch the DeviceListActivity to see devices and do scan
			serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
			return true;
		case R.id.discoverable:
			// Ensure this device is discoverable by others
			ensureDiscoverable();
			return true;
		}
		return false;
	}

}
