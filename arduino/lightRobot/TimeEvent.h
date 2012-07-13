/*! \file TimeEvent.h Interface for timed Events*/

#ifndef TIME_EVENT_H
#define TIME_EVENT_H

/*! \class TimeEvent
Stub for an interface that works as a simple thread. A callback function is defined which is called in defined time intervalls. Other methods for getting and setting ther internal state, this could be the state of some switches, a bluetooth module, or something else. Setter for setting leds or anything.
*/
class TimeEvent
{
  public:
  
  TimeEvent():m_update(false){}
  ~TimeEvent(){}
  
  /*! Callback which is executed periodically*/
  virtual void onTimeEvent()=0;
  /*! Returns an internal state.*/
  virtual unsigned char getInternalState()=0;
  /*! Sets an internal state.*/
  virtual void setInternalState(unsigned char state, bool update = true)=0;
  /*! Executes a more complex (and time consuming) action.*/
  virtual void executeAction()=0;
  
  protected:
  
  bool m_update;
};

#endif
