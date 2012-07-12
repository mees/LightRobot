/*! \file LCDEvent.h updates the LCD Periodically*/

#include <Arduino.h>
#include <OrangutanLCD.h>
#include "TimeEvent.h"

#ifndef LCD_EVENT_H
#define LCD_EVENT_H

#define USE_LCD 0

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
  virtual unsigned char getInternalState();
  /*! Sets an internal state.*/
  virtual void setInternalState(unsigned char state, bool update=false);
  /*! Executes a more complex (and time consuming) action.*/
  virtual void executeAction();
  /*! Sets a string which is then painted on the LCD (if correct mode is set -> freePaint)*/
  void setFreePaintString(String free_paint_string);
  
  public:
  
  /*! Defines which information should be displayed on the LCD*/
  enum  State{
	clear,
	init,
        init_options,
	manualControl,
	remoteControl,
	but_A,
	but_B,
	but_C,
        freePaint
	};
	
  private:
  
  /*! Resets the LCD cursor and saves the number of printed chars.
	\param drawed_chars number of chars drawn.
  */
  void afterPaint(unsigned char drawed_chars);
  
  /*! Prints a message and adds white spaces to overdraw the old message.
	\param message the message to be drawn.
	\return number of drawn messages.
  */
  unsigned char paint(const char* message);
  
  private:
  
  #if USE_LCD
  OrangutanLCD m_lcd_display;
  #endif
  
  /*! Saves the old internal state*/
  State m_old_internal_state;
  
  /*! Saves the new internal state (set from external)*/
  State m_new_internal_state;
  
  /*! Saves the row in which the data should be displayed*/
  
  unsigned char m_drawing_row;
  
  /*! Saves the number of characters*/
  
  unsigned char m_drawed_chars;
  
  char* m_free_paint_string;
  
};

#endif
