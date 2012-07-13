/*! \file BlueToothEvent.h checks periodically if new data over BT has been received.
*/

#include <Arduino.h>
#include <OrangutanLCD.h>
#include "TimeEvent.h"

#ifndef BLUETOOTH_EVENT_H
#define BLUETOOTH_EVENT_H

#define DATA_WORD_LENGTH 4

/*! \class BlueToothEvent
retrieves and stores the received BT data. The data should come over the serialport, it must be started in "setup".
The struct DataPacket publishes the parsed data for the Statemanager.
*/
class BlueToothEvent : public TimeEvent
{
  public:
  
  struct DataPacket {
  char speed;
  char direction;
  int color[4]; // [3]==blue [2]==green [1]==red [0]==brightness
  int mode[2]; // [1]==color mode (0000->remote, 0001->blink, 0010->random, 0011->random&blink)   [0]==drive mode (0000->remote, 0001->random) 
};
  
  BlueToothEvent();
  ~BlueToothEvent(){};
  
  /*! Callback which is executed periodically*/
  virtual void onTimeEvent();
  /*! Returns an internal state.*/
  virtual unsigned char getInternalState();
  /*! Sets an internal state.*/
  virtual void setInternalState(unsigned char state, bool update=false);
  /*! Executes a more complex (and time consuming) action.*/
  virtual void executeAction();
  
  /*! To get the received data in the DataPacket struct
  \return The most recent data received via Serial connection
  */
  DataPacket getDataPacket();
  
  bool m_new_data_present;
  
  

private:

/*! Processes the array with a 4 byte data word and saves the information in the DataPacket field.
\param data the array (must have the length of 4 bytes)
*/
void processData(unsigned char* data);

  private:
  
  enum Data{
    velocity=0,//desired velocity
    direction,//direction to drive
    color,//desired color of the LEDs
    mode,//the different modes: remote, random, blink
  };
  
  unsigned char m_data[DATA_WORD_LENGTH];
  
  struct DataPacket m_data_packet;
  
};

#endif
