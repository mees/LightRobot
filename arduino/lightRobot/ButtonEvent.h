/*! \file ButtonEvent.h checks the Buttons periodically*/

#include <Arduino.h>
#include <OrangutanPushbuttons.h>
#include "TimeEvent.h"

#ifndef BUTTON_EVENT_H
#define BUTTON_EVENT_H

/*! \class ButtonEvent
Checks the hardware buttons periodically, and makes the status public. The status is automatically cleared after reading.
*/
class ButtonEvent : public TimeEvent
{
  public:
  
  ButtonEvent();
  ~ButtonEvent(){};
  
  /*! Callback which is executed periodically*/
  virtual void onTimeEvent();
  /*! Returns an internal state.*/
  virtual int getInternalState();
  /*! Sets an internal state.*/
  virtual void setInternalState(int state, bool update=false);
  /*! Executes a more complex (and time consuming) action.*/
  virtual void executeAction();
  
  bool isButtonTopPressed();
  bool isButtonMiddlePressed();
  bool isButtonBottomPressed();
  
  private:
  
  OrangutanPushbuttons buttons;
  
  /*!Saves if the buttons have been read.*/
  
  bool m_button_status;
  
  /*! saves witch buttons were pressed*/
  unsigned char m_buttons_state;
  
  /*!Indicator if ANY button was pressed*/
  bool m_button_pressed;
  
  /*!Saves the status of the buttons*/
  
  bool m_button_top;
  bool m_button_middle;
  bool m_button_bottom;
  
};

#endif
