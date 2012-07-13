
#include "BlueToothEvent.h"

 BlueToothEvent::BlueToothEvent():
 TimeEvent(),
 m_new_data_present(false)
{
  //init data array
  
  m_data[velocity] = 0;
  m_data[direction] = 0;
  m_data[color] = 0;
  m_data[mode] = 0;
  
  processData(m_data);
  //Serial connection
}

void BlueToothEvent::onTimeEvent()
{	
	//Code to receive a single data word and store it in m_data field
        //Word consists of 4 chars (see the docu for further explanations): 
        
        //[0] -> direction to drive [0-254]
        //[1] -> velocity to drive [0-254]
        //[2] -> desired color for the Light [0-254] in 2 bit packets -> b0[0,3]->Brightnes | [0,3]->Red| [0,3]->Green | [0,3]->Blue
        //[3] -> internal mode (see responsible class)[0-254]
        
        if(Serial.available() >= DATA_WORD_LENGTH)
        {//minimum number of bytes must be available in the buffer
          while(Serial.available() > DATA_WORD_LENGTH)
            Serial.read();//clear buffer except the last 4 bits
          
          m_data[velocity] = (char)Serial.read();
          m_data[direction] = (char)Serial.read();
          m_data[color] = (char)Serial.read();
          m_data[mode] = (char)Serial.read();
          
          processData(m_data); 
          m_new_data_present = true;
        }
}

void BlueToothEvent::processData(unsigned char* data)
{
  m_data_packet.speed = data[velocity];
  m_data_packet.direction = data[direction];
  m_data_packet.color[0] = data[color] & 0b00000011;
  m_data_packet.color[1] = (data[color] & 0b00001100)>>2;
  m_data_packet.color[2] = (data[color] & 0b00110000)>>4;
  m_data_packet.color[3] = (data[color] & 0b11000000)>>6;
  m_data_packet.mode[0] = data[mode] & B00001111;
  m_data_packet.mode[1] = (data[mode] & B11110000)>>4;
}


BlueToothEvent::DataPacket BlueToothEvent::getDataPacket()
{
  m_new_data_present = false;
  return m_data_packet;
}

//unsigned char BlueToothEvent::getData(unsigned char field)
//{
//  if(field <= mode)
//    return m_data[field];
//  else
//    return 0;
//}

unsigned char BlueToothEvent::getInternalState()
{
  return m_data[mode];
}

void BlueToothEvent::setInternalState(unsigned char state, bool update)
{
  //nothing to do here!
}

void BlueToothEvent::executeAction()
{
	//nothing to do here!
}
