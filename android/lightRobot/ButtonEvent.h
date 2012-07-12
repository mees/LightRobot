/*! \file ButtonEvent.h checks the Buttons periodically*/

#include <Arduino.h>
#include <OrangutanPushbuttons.h>
#include "TimeEvent.h"

#ifndef BUTTON_EVENT_H
#define BUTTON_EVENT_H

/*! \class ButtonEvent
Checks the hardware buttons periodically, and makes the status public. Two sorts of status are available -> hold true after pressed and during hold, -> pressed true only after pressed, during hold false.
TODO: - Rename pressed to hold (currently only hold is implemented)
      - Implement pressed
*/
class ButtonEvent : public TimeEvent
{
  public:
  
  ButtonEvent();
  ~ButtonEvent(){};
  
  /*! Callback which is executed periodically*/
  virtual void onTimeEvent();
  /*! Returns an internal state.*/
  virtual unsigned char getInternalState();
  /*! Sets an internal state.*/
  virtual void setInternalState(unsigned char state, bool update=false);
  /*! Executes a more complex (and time consuming) action.*/
  virtual void executeAction();
  
    /*! True if the Button is hold down
  */
  bool isButtonAHold();
  bool isButtonBHold();
  bool isButtonCHold();
  
  /*! True after click, but not during hold*/
  bool isButtonAClicked();//Top
  bool isButtonBClicked();//Middle
  bool isButtonCClicked();//Bottom
  
  private:
  
  OrangutanPushbuttons buttons;
  
  /*!Saves if the buttons have been read.*/
  
  bool m_button_status;
  
  /*! saves witch buttons were clicked/are hold*/
  unsigned char m_buttons_state;
  
  /*!Indicator if ANY button was clicked*/
  bool m_button_clicked;
  
  /*!Saves the status of the buttons*/
  
  bool m_button_A_clicked;
  bool m_button_B_clicked;
  bool m_button_C_clicked;
  
  bool m_button_A_hold;
  bool m_button_B_hold;
  bool m_button_C_hold;
};

#endif
