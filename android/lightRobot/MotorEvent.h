/*! \file MotorEvent.h sets periodically new velocities for the motors.*/

#include <Arduino.h>
#include <OrangutanMotors.h>

#include "TimeEvent.h"

#ifndef MOTOR_EVENT_H
#define MOTOR_EVENT_H

/*! \class MotorEvent
controls the motors, uses the 3pi api.
*/
class MotorEvent : public TimeEvent
{
  public:
  
  MotorEvent();
  ~MotorEvent(){};
  
  /*! Callback which is executed periodically*/
  virtual void onTimeEvent();
  /*! Returns an internal state.*/
  virtual unsigned char getInternalState();
  /*! Sets an internal state.*/
  virtual void setInternalState(unsigned char state, bool update=false);
  /*! Executes a more complex (and time consuming) action.*/
  virtual void executeAction();
  
  
  void stopMotors();
  
  /*! Set the speed with a value range from [-127, 127]. This will be converted to a range from [-255, 255]*/
  void setSpeed(short speed);
  
  /*! Adds the value (can be negative) to the current speed value*/
  void alterSpeed(char value);
  
  void setDirection(short direction);
  
  char getSpeed();
  
  /*In a Range between [0,99] 0 -> max backward...*/
  short getSpeedMotorLeft();
  
  short getSpeedMotorRight();
  
  short getDirection();
  
  void setDistanceValue(unsigned int distance_value){m_distance_value = distance_value;};
  
  public:
  
  enum State{
    normal=0,
    random_m
  };
  
  private:
  
  void calcMotorSpeed();
  short restrictSpeed(short speed);
  
  private:
  
  OrangutanMotors m_motors;
  
  /*! The internal state of this module
  */
  State m_state;
  State m_state_old;
  
  /*! The speed of the robot [-255, 255]
  */
  short m_speed;
  
  /*! The corresponding external value [-127, 127]
  */
  short m_speed_external;
  /*! The driving direction (or turn rate) in radius
  */
  short m_direction;
  
  /*! The speed for the left motor [-255, 255]*/
  short m_speed_motor_left;
  /*! The speed for the right motor [-255, 255]*/
  short m_speed_motor_right;
  
  bool m_update_motors;
  
  /*! used as a timer for the random movement part*/
  int m_random_counter;
  
  /*! Distance value to decide if driving in current direction makes sense*/
  
  unsigned int m_distance_value;
};

#endif
