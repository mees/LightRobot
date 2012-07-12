
#include "StateManager.h"

 StateManager::StateManager():
 m_state(init),
 m_old_state(remoteControl),
 m_update_lcd(true),
 m_range_event(NULL),
 m_light_event(NULL),
 m_motor_event(NULL),
 m_bt_event(NULL),
 m_lcd_event_0(NULL),
 m_lcd_event_1(NULL),
 m_button_event(NULL)
{
}

StateManager::StateManager(RangeEvent *range_event, LightEvent *light_event, MotorEvent *motor_event, BlueToothEvent *bt_event, LCDEvent * lcd_event_0, LCDEvent * lcd_event_1, ButtonEvent *button_event):
 m_state(init),
 m_old_state(remoteControl),
 m_update_lcd(true),
 m_range_event(range_event),
 m_light_event(light_event),
 m_motor_event(motor_event),
m_bt_event(bt_event),
m_lcd_event_0(lcd_event_0),
m_lcd_event_1(lcd_event_1),
m_button_event(button_event)
{
}

void StateManager::manageState()
{
  if(m_state != m_old_state)
     {//on state change
       m_update_lcd = true;//On state change -> update lcd
       m_motor_event->stopMotors();
       m_old_state = m_state;
     }
   switch (m_state)
   {
     case init:
     {
       if(m_update_lcd)
       {
         m_lcd_event_0->setInternalState(LCDEvent::init);
         m_lcd_event_1->setInternalState(LCDEvent::init_options);
         m_update_lcd = false;
       }
       
       if(m_button_event->isButtonAClicked())
         m_state = remoteControl;
       if(m_button_event->isButtonBClicked())
         m_state = manualControl;
       
       break;
     }
     case remoteControl:
     {
       if(m_update_lcd)
       {//only once
         m_lcd_event_0->setInternalState(LCDEvent::remoteControl);
         m_lcd_event_1->setInternalState(LCDEvent::freePaint);
         
         /*m_light_event->setRed((byte)0xff);
         m_light_event->setGreen((byte)0x00);
         m_light_event->setBlue((byte)0x00);*/
         m_light_event->setHSB((byte)0, (byte)0xff);//sets color to red
         m_update_lcd = false;
       }
       
       //acquire new data from bt
       if(m_bt_event->m_new_data_present || m_range_event->m_new_data_present)
       {
        m_data_packet = m_bt_event->getDataPacket();
       
        //set values for the motors
        m_motor_event->setSpeed(m_data_packet.speed);
        m_motor_event->setDirection(m_data_packet.direction);
        m_motor_event->setDistanceValue(m_range_event->getDistanceValue());
        m_motor_event->setInternalState((byte)m_data_packet.mode[0], true);
       
        //set values for the light
       /* m_light_event->setRed((byte)m_data_packet.color[1]);
        m_light_event->setGreen((byte)m_data_packet.color[2]);
        m_light_event->setBlue((byte)m_data_packet.color[3]);*/
        m_light_event->setHSB((byte)m_data_packet.color[1], (byte)m_data_packet.color[2], (byte)m_data_packet.color[3], (byte)m_data_packet.color[0]);
        m_light_event->setInternalState(convertColorMode((byte)m_data_packet.mode[1]), true);
       
      }
       
       /*m_lcd_event_1->setFreePaintString(String("L") + 
                                         String( m_motor_event->getSpeedMotorLeft())+ 
                                         String("R") + 
                                         String(m_motor_event->getSpeedMotorRight()));       */
       //m_lcd_event_1->setFreePaintString(String("L") + String(m_motor_event->getSpeedMotorLeft() + String("R") + String(m_motor_event->getSpeedMotorRight())));
       
       if(m_button_event->isButtonAClicked())
         m_state = manualControl;
       break;
     }
     case manualControl:
     {
      if(m_update_lcd)
       {
         m_lcd_event_0->setInternalState(LCDEvent::manualControl);
         m_lcd_event_1->setInternalState(LCDEvent::freePaint);
         //m_lcd_event_1->setFreePaintString("L  R  ");
         m_light_event->setHSB((byte)70, (byte)0xff);//sets color to green
         m_update_lcd = false;
       }
       
       //Read Data from BT
       // m_data_packet = m_bt_event->getDataPacket();
        //m_lcd_event_1->setFreePaintString(String(m_data_packet.mode[1]));
        
       
       if(m_button_event->isButtonBClicked())
       {//middle Button increases speed
         m_motor_event->alterSpeed(20);
         m_lcd_event_1->setFreePaintString(String("ML") + String(m_motor_event->getSpeedMotorLeft() + String("MR") + String(m_motor_event->getSpeedMotorRight())));
       }
       if(m_button_event->isButtonCClicked())
       {
         //m_light_event->setHSB(189, 0xff);
         if(m_range_event->getDistanceValue() > 100)
         m_motor_event->setSpeed(50);
         else
         m_motor_event->setSpeed(0);
         
       }
       
       if(m_button_event->isButtonAClicked())
         m_state = remoteControl;
       break;
     }
     default:
     {
       break;
     }
   };
}

byte StateManager::convertColorMode(byte bt_color_mode)
{
    return bt_color_mode + 1;
}




