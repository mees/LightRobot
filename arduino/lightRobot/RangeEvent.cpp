
#include "RangeEvent.h"

//TODO: change to pin PD2 (4 on the LCD Port) and PD4 (6 ont the LCD Port) when used together with bt.
#define BT_AVAILABLE 1


#if BT_AVAILABLE
  #define SDA_I2C 2
  #define SCL_I2C 4
#else
  #define SDA_I2C 1
  #define SCL_I2C 0
#endif

#define RANGE_DURATION 5
#define RANDOM_DURATION 4
#define RANGE_ADRESS 0x70
#define START_RANGING 0x51 //gets distance in cm
#define CMD_BYTE 0x00
#define RANGE_BYTE 0x02 


 RangeEvent::RangeEvent():
 TimeEvent(),
 #if SEND_RANGING
  m_wire(new SoftI2CMaster(SDA_I2C, SCL_I2C)),
#endif
  m_present_distance_value(0),
  m_new_data_present(false)
{
  m_update = true;
}

RangeEvent::RangeEvent(SoftI2CMaster *wire):
TimeEvent(),
 #if SEND_RANGING
  m_wire(wire),
#endif
  m_present_distance_value(0),
  m_new_data_present(false)
{
  m_update = true;
}



void RangeEvent::onTimeEvent()
{
  
/*    if(m_old_state != m_state)
    {//if state changed
    }*/
  
  #if SEND_RANGING
  /*Acquire ranging data*/
  m_wire->beginTransmission(RANGE_ADRESS);             // start communicating with SRFmodule
  m_wire->send(RANGE_BYTE);                           // Call the register for start of ranging data
  m_wire->endTransmission();
   
  m_wire->requestFrom(RANGE_ADRESS);                // Request 2 bytes from SRF module
  //while(m_wire->available() < 2);                    // Wait for data to arrive
  byte high_byte = m_wire->receive();                      // Get high byte
  byte low_byte = m_wire->receiveLast();                       // Get low byte
  m_new_data_present = true;
  
  m_present_distance_value = (((unsigned int)(high_byte)) << 8)| (unsigned int)low_byte;         
  
  /*Start new Ranging*/
  m_wire->beginTransmission(RANGE_ADRESS);             // Start communticating with SRF02
  m_wire->send((byte)CMD_BYTE);                      // Send Command Byte
  m_wire->send(START_RANGING);                               // Send 0x51 to start a ranging in cm
  m_wire->endTransmission();
  #endif
    
        
}

unsigned char RangeEvent::getInternalState()
{
  return m_state;
}


void RangeEvent::setInternalState(unsigned char state, bool update)
{
  m_state = (State)state;
  m_update = true;
}

void RangeEvent::executeAction()
{
	//nothing to do here!
}



/*void RangeEvent::startRanging()
{
  #if SEND
  m_wire->beginTransmission(BLINK_ADRESS);
  m_wire->send(FADE_COLOR); // c is fade to color
  m_wire->send(m_red); // value for red channel
  m_wire->send(m_green); // value for green channel
  m_wire->send(m_blue); // value for blue channel
  m_wire->endTransmission();
  #endif
}

void RangeEvent::acquireRangeData()
{
  #if SEND
  m_wire->beginTransmission(BLINK_ADRESS);
  m_wire->send(FADE_HSB); // c is fade to color
  m_wire->send(m_hue); // hue
  m_wire->send(SATURATION); // saturation
  m_wire->send(m_brightness); // brightness
  m_wire->endTransmission();
  #endif
}*/



