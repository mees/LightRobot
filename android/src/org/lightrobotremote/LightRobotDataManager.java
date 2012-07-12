package org.lightrobotremote;

import org.lightrobotremote.util.ColorHelper;

import android.graphics.Path.Direction;
import android.os.Handler;


/** Stores the current data values which will be sent to the robot.
 * 
 * @author Julian, Oier
 *
 */
public class LightRobotDataManager {
	
	
	/** Handles the communication with the Activiy
	 * 
	 */
	private Handler mHandler;
	
	/** sets the speed for the robot [-127, 127]
	 * positive values -> Forward
	 * negative values -> Backward
	 */
	private  byte mSpeed = 0;
	public static final byte SPEED_VALUE_MAX = 127;
	
	/** sets the direction (e.g. the turn rate) for the robot [-127, 127]
	 * positive values -> turns right, 128 turns on point
	 * negative values -> turns left, -127 turns on point
	 */
	private  byte mDirection = 0;//
	public static final byte DIRECTION_VALUE_MAX = 127;
	
	private ColorHelper mColor = new ColorHelper(0);
	
		
	/** sets the mode of the robot AND the mode of the light
	 * b0, b1, b2, b3 -> Color mode -> 	0000 -> remote (color set by remote)
	 * 									0001 -> blink (color set by remote, blinks)
	 * 									0010 -> random (fades random lights)
	 * 									0011 -> random blink
	 * b4, b5, b6, b7 -> Driving mode ->	0000 -> remote
	 * 										0001 -> random drive
	 * 										0010 -> forward
	 * 										0011 -> backward
	 */
	private  byte mMode = 0;
	
	private static final byte MASK_MODE = 0x0f; //masks the the last 4 bits, only the 4 bits are used
	
	private static final byte POSITION_DRIVE_MODE = 0;
	private static final byte MASK_DRIVE_MODE = 0x0f;
	
	public static final byte DRIVE_MODE_REMOTE = 0;
	public static final byte DRIVE_MODE_RANDOM = 1;
	public static final byte DRIVE_MODE_FORWARD = 2;
	public static final byte DRIVE_MODE_BACKWARD = 3;
	public static final byte DRIVE_MODE_LEFT = 4;
	public static final byte DRIVE_MODE_RIGHT = 5;
	
	private static final byte POSITION_COLOR_MODE = 4;
	private static final byte MASK_COLOR_MODE = -0x10;
	public static final byte COLOR_MODE_SHINE = 0;
	public static final byte COLOR_MODE_BLINK = 1;
	public static final byte COLOR_MODE_RANDOM = 2;
	public static final byte COLOR_MODE_RANDOM_BLINK = 3;
	
	/** Data packet with the four fields [speed][direction][color][mode]
	 * 
	 */
	private  byte mDataPacket[] = {0, 0, 0, 0};
	private static final byte POSITION_PACKET_SPEED = 0;
	private static final byte POSITION_PACKET_DIRECTION = 1;
	private static final byte POSITION_PACKET_COLOR = 2;
	private static final byte POSITION_PACKET_MODE = 3;
	
	private  boolean mSendNewPacket = false;
	
	/**Constructor gets the handler and stores it for updating the UI and sending messages.
	 * 
	 * @param handler
	 */
	public LightRobotDataManager(Handler handler)
	{
		mHandler = handler;
	}
	
	/** Stops the movement of the Robot.
	 * 
	 */
	public void resetMoveValues()
	{
		//mMode = 0;
		mSpeed = 0;
		mDirection = 0;
		updatePacket();
	}
	
	/** Resets all values, updates and sends the data package.
	 * 
	 */
	public void resetAllValues()
	{
		mSpeed = 0;
		mDirection = 0;
		mColor.setColor(0);
		mMode = 0;
		updatePacket();
	}
	
	
	public void setSpeedDirection(byte speed, byte direction)
	{
		mSpeed = speed;
		mDirection = direction;
		updatePacket();
	}
	/** Set the speed of the robot
	 * 
	 * @param speed the desired speed of the robot.
	 */
	public void setSpeed(byte speed)
	{
		mSpeed = speed;
		updatePacket();
	}
	
	/** Set the direction in which to drive
	 * 
	 * @param direction the direction in which the robot should move.
	 */
	public void setDirection(byte direction)
	{
		mDirection = direction;
		updatePacket();
	}
	
	/** Changes the value of mSpeed not absolut, but relative according to the amount.
	 * 
	 * @param amount [-127, 127] the amount of altering the value, Overflow is checked (and not possible).
	 */
	public void alterSpeed(byte amount)
	{
		short delta = (short)(SPEED_VALUE_MAX - (mSpeed*Math.signum(amount)));
		if(delta > SPEED_VALUE_MAX)
			delta = SPEED_VALUE_MAX;
		if(delta < Math.abs(amount))
			amount = (byte)(delta * Math.signum(amount));
		mSpeed += amount;
		updatePacket();
	}
	
	/** Changes the value of mDirection not absolut, but relative according to the amount
	 * 
	 * @param amount [-127, 127] the amount of how much will the value be altered, Overflow is checked (and not possible).
	 */
	public void alterDirection(byte amount)
	{
		short delta = (short)(SPEED_VALUE_MAX - (mDirection*Math.signum(amount)));
		if(delta > SPEED_VALUE_MAX)
			delta = SPEED_VALUE_MAX;
		if(delta < Math.abs(amount))
			amount = (byte)(delta * Math.signum(amount));
		mDirection += amount;
		updatePacket();
	}
	
	/** Gets the current speed value.
	 * 
	 * @return the current speed
	 */
	public byte getSpeed()
	{
		return mSpeed;
	}
	
	/** Gets the current direction value.
	 * 
	 * @return the current direction
	 */
	public  byte getDirection() {
		return mDirection;
	}
	
	/** Sets the current color
	 * @param color the desired color to be set
	 */
	
	public void setColor(ColorHelper color)
	{
		mColor = color;
		updatePacket();
	}
	
	/** Gets the current color
	 * @return the current color
	 */
	public ColorHelper getColor()
	{
		return mColor;
	}
	
	
	/** Sets the color mode:
	 * 0 -> shine 
	 * 1 -> blink 
	 * 2 -> random shine
	 * 3 -> random blink
	 * @param color_mode the color mode
	 */
	public void setColorMode(byte color_mode)
	{
		byte tempMode = (byte)(mMode & ~MASK_COLOR_MODE);//clear color mode field.
		tempMode |= (byte)(((byte)(color_mode & MASK_MODE)) << POSITION_COLOR_MODE);
		mMode = tempMode;
		updatePacket();
	}
	
	/** Set the internal mode of the robot:
	 * 0 -> remotecontrolled
	 * 1 -> random movement
	 * 2 -> move forward
	 * 3 -> move backward
	 * 4 -> turn left
	 * 5 -> turn right
	 * 
	 * @param drive_mode mode for the robot
	 */
	public void setMode(byte drive_mode)
	{
		byte tempMode = (byte)(mMode & ~MASK_DRIVE_MODE);//clear color mode field.
		tempMode |= (byte)(((byte)(drive_mode & MASK_MODE)) << POSITION_DRIVE_MODE);
		mMode = tempMode;
		updatePacket();
	}
	
	/** Returns the encoded mode value.
	 * 
	 * @return the encoded code value
	 */
	public byte getMode() {
		return mMode;
	}
	
	
	public void setModeColor(byte drive_mode, byte color_mode, ColorHelper color)
	{
		byte tempMode = (byte)(mMode & ~MASK_DRIVE_MODE);//clear color mode field.
		tempMode |= (byte)(((byte)(drive_mode & MASK_MODE)) << POSITION_DRIVE_MODE);
		mMode = tempMode;
		
		tempMode = (byte)(mMode & ~MASK_COLOR_MODE);//clear color mode field.
		tempMode |= (byte)(((byte)(color_mode & MASK_MODE)) << POSITION_COLOR_MODE);
		mMode = tempMode;
		
		mColor = color;
		updatePacket();
		
		
	}
	
	/** Updates the fields of the data packet, sends a message for display and one to send the data
	 * 
	 */	
	private void updatePacket()
	{
		mDataPacket[POSITION_PACKET_SPEED] = mSpeed;
		mDataPacket[POSITION_PACKET_DIRECTION] = mDirection;
		mDataPacket[POSITION_PACKET_COLOR] = mColor.getColor();
		mDataPacket[POSITION_PACKET_MODE] = mMode;
		
		mHandler.obtainMessage(LightRobotRemoteInterface.MESSAGE_SEND_DATA, 0, 0).sendToTarget();
		mHandler.obtainMessage(LightRobotRemoteInterface.MESSAGE_UPDATE_DISPLAY, 0, 0).sendToTarget();
	}
	
	/** Returns the current data packet
	 * 
	 * @return array with the 4 data fields
	 */
	public byte[] getDataPacket()
	{
		mDataPacket[POSITION_PACKET_SPEED] = mSpeed;
		mDataPacket[POSITION_PACKET_DIRECTION] = mDirection;
		mDataPacket[POSITION_PACKET_COLOR] = mColor.getColor();
		mDataPacket[POSITION_PACKET_MODE] = mMode;
		return mDataPacket;
	}
	

}
