/*! \file RangeEvent.h sets the light and color mode periodically*/

#include <Arduino.h>
#include "TimeEvent.h"
#include "SoftI2CMaster.h"

#ifndef RANGE_EVENT_H
#define RANGE_EVENT_H

#define SEND_RANGING 1

/*! \class RangeEvent
Sets the Light (Color, Brightnes, etc..) periodically.

*/
class RangeEvent : public TimeEvent
{
  public:
  
  RangeEvent();
  RangeEvent(SoftI2CMaster * wire);
  ~RangeEvent(){};
  
  /*! Callback which is executed periodically*/
  virtual void onTimeEvent();
  /*! Returns an internal state.*/
  virtual unsigned char getInternalState();
  /*! Sets an internal state.*/
  virtual void setInternalState(unsigned char state, bool update=false);
  /*! Executes a more complex (and time consuming) action.*/
  virtual void executeAction();
  
  unsigned int getDistanceValue()
  {
    m_new_data_present = false;
    return m_present_distance_value;
  }
  
  enum State {
    init
  };
  
  public:
  
  bool  m_new_data_present;
  
  private:
  
  /*! Sends the data over I2C.
  */
  #if SEND_RANGING
  SoftI2CMaster *m_wire;
  #endif
  
  State m_state;
  
  unsigned int m_present_distance_value;
  
};

#endif
