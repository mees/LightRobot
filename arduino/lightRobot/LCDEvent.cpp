
#include "LCDEvent.h"

#define ERROR_STRING "error"
#define INIT_STRING "init"
#define REMOTE_CONTROL_STRING "rem cont"
#define MANUAL_CONTROL_STRING "man cont"

 LCDEvent::LCDEvent(int row):
 TimeEvent(),
 m_old_internal_state(clear),
 m_new_internal_state(clear),
 m_drawing_row(row),
 m_drawed_chars(0)
{
}

void LCDEvent::onTimeEvent()
{	
	if(m_old_internal_state != m_new_internal_state || m_update)
	{//only if changes from extern occur
                m_update = false;
		m_old_internal_state = m_new_internal_state;

                m_lcd_display.gotoXY(0,m_drawing_row);
		
		switch (m_old_internal_state)
		{
			case clear:
			{
				m_lcd_display.clear();
				afterPaint(0);
				break;
			}
			case init:
			{
				afterPaint(paint(INIT_STRING));
				break;
			}
			case manualControl:
			{
				afterPaint(paint(MANUAL_CONTROL_STRING));
				break;
			}
			case remoteControl:
			{
				afterPaint(paint(REMOTE_CONTROL_STRING));
				break;
			}
			default:
			{
				afterPaint(paint(ERROR_STRING));
				break;
			}
                  }
          }
}

void LCDEvent::afterPaint(int drawed_chars)
{
//	m_lcd_display.gotoXY(0,m_drawing_row);
	m_drawed_chars = drawed_chars;
}

int LCDEvent::paint(const char* message)
{
	int message_length = String(message).length();
        int message_diff = 0;
        
        m_lcd_display.print(message);
	if(message_length < m_drawed_chars)
	{//Whitespaces must be added to overdraw the old message
                message_diff = m_drawed_chars-message_length;
                char white_space[message_diff +1];
                white_space[message_diff] = '\0';
		for(int i = message_diff; i > 0; i--)
			white_space[message_diff-i] = ' ';
			
	  m_lcd_display.print(white_space);
	}
	return message_length;//should be only the real length of the message, else the message would ever grow longer!
}

int LCDEvent::getInternalState()
{
  return m_old_internal_state;
}

void LCDEvent::setInternalState(int state, bool update)
{
        m_update = update;
	m_new_internal_state = (State)state;
}

void LCDEvent::executeAction()
{
	//nothing to do here!
}
