
#include "LCDEvent.h"

#define ERROR_STRING "error"
#define INIT_STRING "init"
#define INIT_OPTIONS_STRING "A:Rm B:M"
#define REMOTE_CONTROL_STRING "rem cont"
#define MANUAL_CONTROL_STRING "man cont"

#define PAINT_BUFFER_LENGTH 9

 LCDEvent::LCDEvent(int row):
 TimeEvent(),
 m_old_internal_state(clear),
 m_new_internal_state(clear),
 m_drawing_row(row),
 m_drawed_chars(0),
 m_free_paint_string("aaaaaaaa")
{
}

void LCDEvent::onTimeEvent()
{	
	if(m_old_internal_state != m_new_internal_state || m_update)
	{//only if changes from extern occur
                m_update = false;
		m_old_internal_state = m_new_internal_state;

                #if USE_LCD
                m_lcd_display.gotoXY(0,m_drawing_row);
                #endif
		
		switch (m_old_internal_state)
		{
			case clear:
			{
				#if USE_LCD
                                m_lcd_display.clear();
                                #endif
				afterPaint(0);
				break;
			}
			case init:
			{
				afterPaint(paint(INIT_STRING));
				break;
			}
                        case init_options:
                        {
                          afterPaint(paint(INIT_OPTIONS_STRING));
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
                        case freePaint:
                        {
                          afterPaint(paint(m_free_paint_string));
                          break;
                        }
			default:
			{
				afterPaint(paint(ERROR_STRING));
                                delay(1000);
				break;
			}
                  }
          }
}

void LCDEvent::afterPaint(unsigned char drawed_chars)
{
//	m_lcd_display.gotoXY(0,m_drawing_row);
	m_drawed_chars = drawed_chars;
}

unsigned char LCDEvent::paint(const char* message)
{
	unsigned char message_length = String(message).length();
        unsigned char message_diff = 0;
        
        #if USE_LCD
        m_lcd_display.print(message);
        #endif
	if(message_length < m_drawed_chars)
	{//Whitespaces must be added to overdraw the old message
                message_diff = m_drawed_chars-message_length;
                char white_space[message_diff +1];
                white_space[message_diff] = '\0';
		for(int i = message_diff; i > 0; i--)
			white_space[message_diff-i] = ' ';
	
          #if USE_LCD		
	  m_lcd_display.print(white_space);
          #endif
	}
	return message_length;//should be only the real length of the message, else the message would ever grow longer!
}

void LCDEvent::setFreePaintString(String free_paint_string)
{
  free_paint_string.toCharArray(m_free_paint_string, PAINT_BUFFER_LENGTH);
  m_update = true;
}

unsigned char LCDEvent::getInternalState()
{
  return m_old_internal_state;
}

void LCDEvent::setInternalState(unsigned char state, bool update)
{
        m_update = update;
	m_new_internal_state = (State)state;
}

void LCDEvent::executeAction()
{
	//nothing to do here!
}
