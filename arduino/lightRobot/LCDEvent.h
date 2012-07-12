/*! \file LCDEvent.h updates the LCD Periodically*/

#include <Arduino.h>
#include <OrangutanLCD.h>
#include "TimeEvent.h"

#ifndef LCD_EVENT_H
#define LCD_EVENT_H

/*! \class LCDEvent
Updates the LCD-Display periodically, accordingly to the state which is set from Extern.
*/
class LCDEvent : public TimeEvent
{
  public:
  
  LCDEvent(int row = 0);
  ~LCDEvent(){};
  
  /*! Callback which is executed periodically*/
  virtual void onTimeEvent();
  /*! Returns an internal state.*/
  virtual int getInternalState();
  /*! Sets an internal state.*/
  virtual void setInternalState(int state, bool update=false);
  /*! Executes a more complex (and time consuming) action.*/
  virtual void executeAction();
  
  public:
  
  /*! Defines which information should be displayed on the LCD*/
  enum  State{
	clear,
	init,
	manualControl,
	remoteControl,
	but_A,
	but_B,
	but_C
	};
	
  private:
  
  /*! Resets the LCD cursor and saves the number of printed chars.
	\param drawed_chars number of chars drawn.
  */
  void afterPaint(int drawed_chars);
  
  /*! Prints a message and adds white spaces to overdraw the old message.
	\param message the message to be drawn.
	\return number of drawn messages.
  */
  int paint(const char* message);
  
  private:
  
  OrangutanLCD m_lcd_display;
  
  /*! Saves the old internal state*/
  State m_old_internal_state;
  
  /*! Saves the new internal state (set from external)*/
  State m_new_internal_state;
  
  /*! Saves the row in which the data should be displayed*/
  
  int m_drawing_row;
  
  /*! Saves the number of characters*/
  
  int m_drawed_chars;
  
};

#endif
