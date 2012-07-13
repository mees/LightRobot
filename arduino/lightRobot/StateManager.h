/*! \file StateManager.h Implementation of the global statemanager.*/

#include "RangeEvent.h"
#include "LightEvent.h"
#include "MotorEvent.h"
#include "BlueToothEvent.h"
#include "LCDEvent.h"
#include "ButtonEvent.h"

#ifndef STATE_MANAGER_H
#define STATE_MANAGER_H

/*! \class StateManager
Implements a statemanager for the LightRobot. All information will be centralized and processed here.
*/
class StateManager
{
  public:
  
  StateManager();
  StateManager(RangeEvent *range_event, LightEvent *light_event, MotorEvent *motor_event, BlueToothEvent *bt_event,  LCDEvent * lcd_event_0, LCDEvent * lcd_event_1, ButtonEvent *button_event);
  ~StateManager(){}
  
  /*! The central part of the software. This method is executed over and over again. In this place the data is managed and distributed for the several processes like the motors, the light, the buttons and the bt-connection
  */
  void manageState();
  
  
  private:
  
  byte convertColorMode(byte bt_color_mode);
  
  private:
  
  enum State{
    init=0,
    remoteControl,
    manualControl
  };
  
  private:
  
  State m_state;
  State m_old_state;
  
  bool m_update_lcd;
  
  RangeEvent *m_range_event;
  LightEvent *m_light_event;
  MotorEvent *m_motor_event;
  BlueToothEvent *m_bt_event;
  LCDEvent *m_lcd_event_0;
  LCDEvent *m_lcd_event_1;
  ButtonEvent *m_button_event;
  
  BlueToothEvent::DataPacket m_data_packet;
};

#endif
