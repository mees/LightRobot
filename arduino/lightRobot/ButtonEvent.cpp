
#include "ButtonEvent.h"

 ButtonEvent::ButtonEvent():
 TimeEvent(),
m_buttons_state(0),
m_button_clicked(false),
m_button_A_clicked(false),
m_button_B_clicked(false),
m_button_C_clicked(false),
m_button_A_hold(false),
m_button_B_hold(false),
m_button_C_hold(false)
{
}

void ButtonEvent::onTimeEvent()
{
        unsigned char temp_clicked = buttons.getSingleDebouncedPress(TOP_BUTTON | MIDDLE_BUTTON | BOTTOM_BUTTON);
        unsigned char temp_pressed = m_buttons_state | temp_clicked;
        unsigned char temp_released_neg = ~(buttons.getSingleDebouncedRelease(TOP_BUTTON | MIDDLE_BUTTON | BOTTOM_BUTTON));
        m_buttons_state = temp_pressed & temp_released_neg;//  for example: button is pressed (and holded): 
                                                           //  Press -> 0001 Release -> 0000 -> Neg ->1111 -> & -> 0001, Release -> 0001 -> 1110 -> & -> 0000
        
	if(m_buttons_state != 0)
	  m_button_clicked = true;
        else
          m_button_clicked = false;      
          
        if((temp_clicked & TOP_BUTTON) == TOP_BUTTON)
          m_button_C_clicked = true;
        if((temp_clicked & MIDDLE_BUTTON) == MIDDLE_BUTTON)
          m_button_B_clicked = true;	
        if((temp_clicked & BOTTOM_BUTTON) == BOTTOM_BUTTON)
          m_button_A_clicked = true;	

	if((m_buttons_state & TOP_BUTTON) == TOP_BUTTON)
	  m_button_C_hold = true;
        else
          m_button_C_hold = false;
          
	if((m_buttons_state & MIDDLE_BUTTON) == MIDDLE_BUTTON)
	  m_button_B_hold = true;
        else
          m_button_B_hold = false;
          
       	if((m_buttons_state & BOTTOM_BUTTON) == BOTTOM_BUTTON)
	  m_button_A_hold = true;
        else
          m_button_A_hold = false;        
}

unsigned char ButtonEvent::getInternalState()
{
  return m_buttons_state;
}

bool ButtonEvent::isButtonAHold()
{
  //m_button_state = true;
  return m_button_A_hold;
}

bool ButtonEvent::isButtonBHold()
{
  return m_button_B_hold;
}

bool ButtonEvent::isButtonCHold()
{
  return m_button_C_hold;
}

bool ButtonEvent::isButtonAClicked()
{
  if(m_button_A_clicked)
  {
    m_button_A_clicked = false;
    return true;
  }
  else
    return false;
}

bool ButtonEvent::isButtonBClicked()
{
    if(m_button_B_clicked)
  {
    m_button_B_clicked = false;
    return true;
  }
  else
    return false;
}

bool ButtonEvent::isButtonCClicked()
{
    if(m_button_C_clicked)
  {
    m_button_C_clicked = false;
    return true;
  }
  else
    return false;
}

void ButtonEvent::setInternalState(unsigned char state, bool update)
{
	//nothing to do here!
}

void ButtonEvent::executeAction()
{
	//nothing to do here!
}
