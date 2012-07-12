
#include "ButtonEvent.h"

 ButtonEvent::ButtonEvent():
 TimeEvent(),
m_buttons_state(0),
m_button_pressed(false),
m_button_top(false),
m_button_middle(false),
m_button_bottom(false)
{
}

void ButtonEvent::onTimeEvent()
{
        unsigned char temp_pressed = m_buttons_state | buttons.getSingleDebouncedPress(TOP_BUTTON | MIDDLE_BUTTON | BOTTOM_BUTTON);
        unsigned char temp_released_neg = ~(buttons.getSingleDebouncedRelease(TOP_BUTTON | MIDDLE_BUTTON | BOTTOM_BUTTON));
        m_buttons_state = temp_pressed & temp_released_neg;// for example: button is pressed (and holded): Press -> 0001 Release -> 0000 -> Neg ->1111 -> & -> 0001, Release -> 0001 -> 1110 -> & -> 0000
        
	
	if(m_buttons_state != 0)
	  m_button_pressed = true;
        else
          m_button_pressed = false;      
	

	if((m_buttons_state & TOP_BUTTON) == TOP_BUTTON)
	  m_button_top = true;
        else
          m_button_top = false;
          
	if((m_buttons_state & MIDDLE_BUTTON) == MIDDLE_BUTTON)
	  m_button_middle = true;
        else
          m_button_middle = false;
          
       	if((m_buttons_state & BOTTOM_BUTTON) == BOTTOM_BUTTON)
	  m_button_bottom = true;
        else
          m_button_bottom = false;        
}

int ButtonEvent::getInternalState()
{
  return m_buttons_state;
}

bool ButtonEvent::isButtonTopPressed()
{
  //m_button_state = true;
  return m_button_top;
}

bool ButtonEvent::isButtonMiddlePressed()
{
  return m_button_middle;
}

bool ButtonEvent::isButtonBottomPressed()
{
  return m_button_bottom;
}

void ButtonEvent::setInternalState(int state, bool update)
{
	//nothing to do here!
}

void ButtonEvent::executeAction()
{
	//nothing to do here!
}
