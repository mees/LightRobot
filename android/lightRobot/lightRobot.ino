/*Main file for the LightRobot project on the arduino part*/

#include <TimedAction.h>
#include <Wire.h>
#include <OrangutanPushbuttons.h>
#include <OrangutanLCD.h>
#include <OrangutanMotors.h>
#include "ButtonEvent.h"
#include "LCDEvent.h"
#include "BlueToothEvent.h"
#include "MotorEvent.h"
#include "StateManager.h"
//#include <Wire.h>
#include "LightEvent.h"
#include "RangeEvent.h"
//#include "SoftI2Master.h"

#define SLOW_ACTION 100
#define MIDDLE_ACTION 75
#define MOTOR_ACTION 150
#define FAST_ACTION 40
#define FASTER_ACTION 5


#define LCD_ATTACHED 0
#define MOTOR_ACTIVATED 1

#define LIGHT_ACTIVATED 1

#define RANGE_ACTIVATED 1

SoftI2CMaster m_wire(2,4);


ButtonEvent button_event;
TimedAction button_action = TimedAction(MIDDLE_ACTION, buttonEvent);
LCDEvent lcd_event_0;
TimedAction lcd_action_0 = TimedAction(SLOW_ACTION, lcdEvent0);
LCDEvent lcd_event_1(1);
TimedAction lcd_action_1 = TimedAction(SLOW_ACTION, lcdEvent1);
BlueToothEvent bt_event;
TimedAction bt_action = TimedAction(FAST_ACTION, btEvent);
MotorEvent motor_event;
TimedAction motor_action = TimedAction(MOTOR_ACTION, motorEvent);
LightEvent light_event(&m_wire);
//LightEvent light_event;
TimedAction light_action = TimedAction(SLOW_ACTION, lightEvent);
RangeEvent range_event(&m_wire);
TimedAction range_action = TimedAction(SLOW_ACTION, rangeEvent);


void buttonEvent(){
  button_event.onTimeEvent();
}

void lcdEvent0(){
  lcd_event_0.onTimeEvent();
}

void lcdEvent1(){
  lcd_event_1.onTimeEvent();
}

void btEvent(){
  bt_event.onTimeEvent();
}

void motorEvent(){
  motor_event.onTimeEvent();
}

void lightEvent(){
  light_event.onTimeEvent();
}

void rangeEvent(){
  range_event.onTimeEvent();
}

StateManager state_manager(&range_event, &light_event, &motor_event, &bt_event, &lcd_event_0, &lcd_event_1, &button_event);

void setup()
{  
  lcd_event_0.setInternalState(LCDEvent::clear, true);
  lcd_event_0.onTimeEvent();
  lcd_event_1.setInternalState(LCDEvent::clear, true);
  lcd_event_1.onTimeEvent();
  
  randomSeed(analogRead(6));
  
  Serial.begin(9600);

}

void loop()
{
  button_action.check();
  #if LCD_ATTACHED
  lcd_action_0.check();
  lcd_action_1.check();
  #endif
  bt_action.check();
  #if MOTOR_ACTIVATED
  motor_action.check();
  #endif
  #if LIGHT_ACTIVATED
  light_action.check();
  #endif
  #if RANGE_ACTIVATED
  range_action.check();
  #endif
  
  state_manager.manageState();
}

