/**
 * @brief This File contains the necessary implementations to control the LightRobot with speech commands.
 * @author Julian
 * @date 25.06.2012
 * 
 * 
 */

package org.lightrobotremote;

import java.util.ArrayList;

import org.lightrobotremote.util.ColorHelper;

import android.app.Activity;
import android.os.Handler;


/**
 * 
 * @author Julian, Oier
 *
 */
public class LightRobotVoiceControl {

	private static final int NUMBER_OF_WORDS = 2;

	//Each command consists of two words part1 and part2 respectively.
	private static final String PART_1_0 = "move";
	private static final String PART_1_1 = "turn";
	private static final String PART_1_2 = "shine";
	private static final String PART_1_3 = "blink";

	private static final String PART_2_MOVE_0 = "remote";
	private static final String PART_2_MOVE_1 = "backward";//not used
	private static final String PART_2_MOVE_2 = "random";

	private static final String PART_2_TURN_0 = "left";
	private static final String PART_2_TURN_1 = "right";


	private static final String PART_2_SHINE_0 = "white";
	private static final String PART_2_SHINE_1 = "black";
	private static final String PART_2_SHINE_2 = "red";
	private static final String PART_2_SHINE_3 = "green";
	private static final String PART_2_SHINE_4 = "blue";
	private static final String PART_2_SHINE_5 = "random";

	private static final String PART_2_BLINK_0 = "white";
	private static final String PART_2_BLINK_1 = "black";
	private static final String PART_2_BLINK_2 = "red";
	private static final String PART_2_BLINK_3 = "green";
	private static final String PART_2_BLINK_4 = "blue";
	private static final String PART_2_BLINK_5 = "random";

	private byte driveMode;

	private byte colorMode = 0;

	private  ColorHelper mColor = new ColorHelper(0);

	private ArrayList<String> mWordsList;

	private String mPart_1;
	public String getPart_1() {
		return mPart_1;
	}

	public String getPart_2() {
		return mPart_2;
	}

	private String mPart_2;

	private Handler mHandler;

	private boolean mIsFirstPartCorrect = false;
	private boolean mIsSecondPartCorrect = false;
	private boolean mIsCommandCorrect = false;


	public boolean isFirstPartCorrect() {
		return mIsFirstPartCorrect;
	}

	public boolean isSecondPartCorrect() {
		return mIsSecondPartCorrect;
	}

	public boolean isCommandCorrect() {
		return mIsCommandCorrect;
	}

	public LightRobotVoiceControl(Handler uiHandler)
	{
		mHandler = uiHandler;			
	}


	public void setWordList(ArrayList<String> new_words)
	{
		mWordsList = new_words;
		mIsFirstPartCorrect = false;
		mIsSecondPartCorrect = false;
		mIsCommandCorrect = false;
		parseWords();
	}

	public ArrayList<String> recognizedWords()
	{
		return mWordsList;
	}

	private void parseWords()
	{
		ArrayList<String> present_words = mWordsList;
		ArrayList<ArrayList<String> > possible_commands = new ArrayList<ArrayList<String> >();

		for (int i = 0; i < present_words.size(); i++) {

			String[] words = present_words.get(i).split("\\s+");
			if(words.length == NUMBER_OF_WORDS)
			{
				ArrayList<String> temp_command = new ArrayList<String>();
				temp_command.add(words[0]);
				temp_command.add(words[1]);

				possible_commands.add(temp_command);
			}
		}

		int number_of_possible_commands = possible_commands.size();
		if(number_of_possible_commands > 0)
		{
			String[] temp_command = new String[]{"", ""};
			boolean isFirstPartCorrect = false;
			boolean isSecondPartCorrect = false;

			for (int i = 0; i < number_of_possible_commands && !(isFirstPartCorrect && isSecondPartCorrect); i++) {

				isFirstPartCorrect = false;
				isSecondPartCorrect = false;
				temp_command[0] = possible_commands.get(i).get(0);
				temp_command[1] = possible_commands.get(i).get(1);

				if(temp_command[0].equalsIgnoreCase(PART_1_0))//move
				{
					isFirstPartCorrect = true;
					if(temp_command[1].equalsIgnoreCase(PART_2_MOVE_0))//remote
					{
						isSecondPartCorrect = true;
						driveMode = LightRobotDataManager.DRIVE_MODE_REMOTE;
					}
					else if(temp_command[1].equalsIgnoreCase(PART_2_MOVE_1))//backward
					{
						isSecondPartCorrect = true;
						driveMode = LightRobotDataManager.DRIVE_MODE_BACKWARD;
					}
					else if(temp_command[1].equalsIgnoreCase(PART_2_MOVE_2))//random
					{
						isSecondPartCorrect = true;
						driveMode = LightRobotDataManager.DRIVE_MODE_RANDOM;
					}
				}
				else if(temp_command[0].equalsIgnoreCase(PART_1_1))//turn
				{
					isFirstPartCorrect = true;
					if(temp_command[1].equalsIgnoreCase(PART_2_TURN_0))//left
					{
						isSecondPartCorrect = true;
						driveMode = LightRobotDataManager.DRIVE_MODE_LEFT;
					}
					else if(temp_command[1].equalsIgnoreCase(PART_2_TURN_1))//right
					{
						isSecondPartCorrect = true;
						driveMode = LightRobotDataManager.DRIVE_MODE_RIGHT;
					}
				}
				else if(temp_command[0].equalsIgnoreCase(PART_1_2))//shine
				{
					isFirstPartCorrect = true;
					colorMode = LightRobotDataManager.COLOR_MODE_SHINE;

					if(temp_command[1].equalsIgnoreCase(PART_2_SHINE_0))//white
					{
						isSecondPartCorrect = true;
						mColor.setColor(0xffffff00);
					}
					else if(temp_command[1].equalsIgnoreCase(PART_2_SHINE_1))//black
					{
						isSecondPartCorrect = true;
						mColor.setColor(0x00000000);
					}
					else if(temp_command[1].equalsIgnoreCase(PART_2_SHINE_2))//red
					{
						isSecondPartCorrect = true;
						mColor.setColor(ColorHelper.HSB_RED);//a bit different because it is interpreted as hsb
					}
					else if(temp_command[1].equalsIgnoreCase(PART_2_SHINE_3))//green
					{
						isSecondPartCorrect = true;
						mColor.setColor(ColorHelper.HSB_GREEN);
					}
					else if(temp_command[1].equalsIgnoreCase(PART_2_SHINE_4))//blue
					{
						isSecondPartCorrect = true;
						mColor.setColor(ColorHelper.HSB_BLUE);
					}
					else if(temp_command[1].equalsIgnoreCase(PART_2_SHINE_5))//random
					{
						isSecondPartCorrect = true;
						colorMode = LightRobotDataManager.COLOR_MODE_RANDOM;
					}
				}
				else if(temp_command[0].equalsIgnoreCase(PART_1_3))
				{
					isFirstPartCorrect = true;
					colorMode = LightRobotDataManager.COLOR_MODE_BLINK;

					if(temp_command[1].equalsIgnoreCase(PART_2_BLINK_0))//white
					{
						isSecondPartCorrect = true;
						mColor.setColor(0xffffffff);
					}
					else if(temp_command[1].equalsIgnoreCase(PART_2_BLINK_1))//black
					{
						isSecondPartCorrect = true;
						mColor.setColor(0x00000000);
					}
					else if(temp_command[1].equalsIgnoreCase(PART_2_BLINK_2))//red
					{
						isSecondPartCorrect = true;
						mColor.setColor(ColorHelper.HSB_RED);
					}
					else if(temp_command[1].equalsIgnoreCase(PART_2_BLINK_3))//green
					{
						isSecondPartCorrect = true;
						mColor.setColor(ColorHelper.HSB_GREEN);
					}
					else if(temp_command[1].equalsIgnoreCase(PART_2_BLINK_4))//blue
					{
						isSecondPartCorrect = true;
						mColor.setColor(ColorHelper.HSB_BLUE);
					}
					else if(temp_command[1].equalsIgnoreCase(PART_2_BLINK_5))//random
					{
						isSecondPartCorrect = true;
						colorMode = LightRobotDataManager.COLOR_MODE_RANDOM_BLINK;
					}
				}
			}

			mIsFirstPartCorrect = isFirstPartCorrect;
			mIsSecondPartCorrect = isSecondPartCorrect;
			mIsCommandCorrect = isFirstPartCorrect && isSecondPartCorrect;

			if(mIsCommandCorrect)
			{//save the correct recognized words
				mPart_1 = temp_command[0];
				mPart_2 = temp_command[1];	
			}
			else
			{//save the best guess (the first two words in the array)
				mPart_1 = possible_commands.get(0).get(0);
				mPart_2 = possible_commands.get(0).get(1);
			}
		}
		else
		{
			mPart_1 = "-";
			mPart_2 = "-";

		}

		mHandler.obtainMessage(LightRobotRemoteInterface.MESSAGE_UPDATE_DISPLAY, 0, 0).sendToTarget();
		mHandler.obtainMessage(LightRobotRemoteInterface.MESSAGE_UPDATE_DATA, 0, 0).sendToTarget();
	}

	public  ColorHelper getColor() {
		return mColor;
	}

	public byte getDriveMode() {
		return driveMode;
	}

	public byte getColorMode() {
		return colorMode;
	}
}
