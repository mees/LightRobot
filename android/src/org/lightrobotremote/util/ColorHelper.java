package org.lightrobotremote.util;

import android.R.color;
import android.graphics.Color;

/**@class ColorHelper
 * @brief Handles the management of the color data for the data packet which is sent to the light robot.
 * The color field is 8 bits long and consists of 4 individual values with a length of 2 bits each
 * b0-b1: Brightness of the light
 * b2-b3: Red value of the light
 * b4-b5: Green value of the light
 * b6-b7: Blue value of the light
 * 
 * @author Julian, Oier
 *
 */

public class ColorHelper {
	
	
	/** sets the color of the lamps [0, 255]
	 * b0, b1 -> brightness -> 00 min, 11 max
	 * b2, b3 -> red value
	 * b4, b5 -> green value
	 * b6, b7 -> blue value
	 */
	private  byte mColor = 0;
	private static final short MASK_SHORT_BYTE = 0xff;
	private static final byte MASK_RGB = 0x3f;//0b00111111
	private static final byte POSITION_RGB = 0x02;
	private static final byte MASK_RGB_FROM_BYTE = -0x04; //0b11111100
	private static final byte MASK_BRIGHTNESS = 0x03;//0b00000011;
	private static final byte POSITION_BRIGHTNESS = 0;
	private static final byte MASK_RED = 0x0C;
	private static final byte POSITION_RED = 2;
	private static final byte MASK_GREEN = 0x30;
	private static final byte POSITION_GREEN = 4;
	private static final byte MASK_BLUE = -0x40;//0xc0 11000000
	private static final byte POSITION_BLUE = 6;
	private static final byte mColorLookup[] = {0,0,0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0,0,0, 0,0,0,
												1,1,1,1,1,1,1,1,1,1, 1,1,1,1,1,1,1,1,1,1, 1,1,1,1,1,1,1,1,1,1, 1,1,1,1,1,1,1,1,1,1, 1,1,1,1,1,1,1,1,1,1, 1,1,1,1,1,1,1,1,1,1, 1,1,1,1,
												2,2,2,2,2,2,2,2,2,2, 2,2,2,2,2,2,2,2,2,2, 2,2,2,2,2,2,2,2,2,2, 2,2,2,2,2,2,2,2,2,2, 2,2,2,2,2,2,2,2,2,2, 2,2,2,2,2,2,2,2,2,2, 2,2,2,2,
												3,3,3,3,3,3,3,3,3,3, 3,3,3,3,3,3,3,3,3,3, 3,3,3,3,3,3,3,3,3,3, 3,3,3,3,3,3,3,3,3,3, 3,3,3,3,3,3,3,3,3,3, 3,3,3,3,3,3,3,3,3,3, 3,3,3,3, 3};
	/*!Color definitions ins HSB Space*/
	public static final int HSB_RED = 0xff000000;
	public static final int HSB_GREEN = 0xffff4242;//is about 80 and equals green in the HSB space.
	public static final int HSB_BLUE = 0xff0000f0;

	/** Standard contr. initialises the values (color is 0)
	 * 
	 */
	public ColorHelper()
	{
		setColor(0);
	}
	
	/** The color is set with a long value
	 * @param color 4 byte value to encode the color
	 */
	public ColorHelper(int color_value)
	{
		setColor(color_value);
	}
	
	/** Sets the RGB part of the color at once
	 * 
	 * @param color_value a value between [0 63] which determines the color
	 */
	public void setColor(byte color_value)
	{
		color_value = (byte) (color_value & MASK_RGB);//only the lowest 6 bits remain
		byte temp_color = (byte) (mColor & (~MASK_RGB_FROM_BYTE));//cut the brightness out
		temp_color = (byte) (temp_color | color_value<<POSITION_RGB);
		mColor = temp_color;
	}
	
	
	/** Sets the color value
	 * example: alpha max and red: 0xffff0000
	 * 			alpha max and white: 0xffffffff
	 * 			alpha max and black: 0xff000000
	 * @param color_value color as 4 bytes
	 */
	public void setColor(int color_value)
	{
		setColor((short)Color.alpha(color_value),(short) Color.red(color_value),(short) Color.green(color_value),(short) Color.blue(color_value));
	}
	
	/** Sets all color values
	 * 
	 * @param brightness the brightness of the light.
	 * @param red red value
	 * @param green green value
	 * @param blue blue value
	 */
	public void setColor(short brightness, short red, short green, short blue)
	{
		setColorBrightness(brightness);
		setColorRed(red);
		setColorGreen(green);
		setColorBlue(blue);
	}
	
	/** Set individually the brightness of the light.
	 * 
	 * @param brightness how bright should the light shine
	 */
	public void setColorBrightness(short brightness)
	{
		setColorPart(brightness, POSITION_BRIGHTNESS, MASK_BRIGHTNESS);
	}
	
	/** Set the red part of the color.
	 * 
	 * @param red red part of the color
	 */
	public void setColorRed(short red)
	{
		setColorPart(red, POSITION_RED, MASK_RED);
	}
	
	/** Set the green part of the color.
	 * 
	 * @param green green part of the color
	 */
	public void setColorGreen(short green)
	{
		setColorPart(green, POSITION_GREEN, MASK_GREEN);
	}
	
	/** Set the blue part of the color.
	 * 
	 * @param blue blue part of the color
	 */
	public void setColorBlue(short blue)
	{
		setColorPart(blue, POSITION_BLUE, MASK_BLUE);
	}
	
	/** Returns the encoded color value
	 * 
	 *@return encoded color value
	 */
	public  byte getColor() {
		return mColor;
	}
	
	/** Set an individual part of the color value
	 * 
	 * @param value the value to be set
	 * @param position on which position should the value be altered
	 * @param mask which mask should be applied
	 */
	
	private void setColorPart(short value, byte position, byte mask)
	{
		byte tempValue = shortToColor(value, position);
		mColor = (byte)(tempValue | ((byte)(mColor & ~mask)));
	}
	
	/** Converts the color to a 2 digit value on a position
	 * 
	 * @param value the value which should be applied
	 * @param position position in an 8 bit type
	 * @return the byte with the value on the correct position 
	 */
	private byte shortToColor(short value, byte position)
	{
		short tempValue = (short)(value & MASK_SHORT_BYTE);//maximum value is now 254
		tempValue = mColorLookup[tempValue];//lookup which value should be used
		tempValue = (byte)(tempValue << position);//shift to correct position
		return (byte)tempValue;
	}
}
