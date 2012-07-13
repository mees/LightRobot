
#include "MotorEvent.h"

#define SPEED_TRANS_FAKTOR 1
#define SPEED_MAX 200
#define DIRECTION_MAX 127
#define DIRECTION_OFFSET 20 //reduces the direction value linear MAX should be TURN_MIN*TURN_MULTIPLICATOR

#define SPEED_TRANS_MOTOR_PERCENT_FAKTOR 0.1921
#define SPEED_TRANS_MOTOR_PERCENT_ADD 50

#define DIRECTION_TRANS_FAKTOR 1.41732

#define WHEEL_DISTANCE 100 //about 100mm
#define SPEED_FACTOR 900/255 //255 means full speed, its about 900mm/s
#define TURN_MULTIPLICATOR 6 //Factor with which the received turn rate is multiplied (to cover a greater range). The greater this value is, the lesser sharp the robot is turning
#define TURN_VALUE 127*TURN_MULTIPLICATOR // This value minus the received defines the turn radius 
#define TURN_MIN (5) //Radius smaller than this lets the wheel start to turn backwards

#define RANDOM_MOVE_DURATION 7
#define RANDOM_MOVE_SPEED 65
#define RANDOM_DISTANCE_VALUE 30

 MotorEvent::MotorEvent():
 TimeEvent(),
 m_state(normal),
 m_speed(0),
 m_speed_external(0),
 m_direction(0),
 m_speed_motor_left(0),
 m_speed_motor_right(0),
 m_update_motors(false),
 m_random_counter(0)
{
//  int random_number;
//  randomSeed(analogRead(1));
}

void MotorEvent::onTimeEvent()
{
  if(m_state != m_state_old)
  {//on state change
    m_state_old = m_state;
    m_speed_motor_right = 0;
    m_speed_motor_left = 0;
    m_motors.setSpeeds(m_speed_motor_right, m_speed_motor_left);
    
  }
  switch(m_state)
  {
    case normal:
    {
      m_motors.setSpeeds(m_speed_motor_right, m_speed_motor_left);
      break;
    }
    case random_m:
    {
      if(m_update)
      {//at first run
        setSpeed(RANDOM_MOVE_SPEED);
        m_update = false;
      }
      
      if(m_distance_value <= RANDOM_DISTANCE_VALUE)
      {
        setSpeed(-RANDOM_MOVE_SPEED);
        setDirection(0);
        m_motors.setSpeeds(m_speed_motor_right, m_speed_motor_left);
      }
      else
      {
      
        if(m_random_counter >= RANDOM_MOVE_DURATION)
        {
         m_random_counter = 0;
         setSpeed(RANDOM_MOVE_SPEED);
         long random_number = random((-DIRECTION_MAX+2), (DIRECTION_MAX-2));
         setDirection((byte)random_number);
         m_motors.setSpeeds(m_speed_motor_right, m_speed_motor_left);
        }
        else
          m_random_counter++;
      }
      break;
    }
    default:
    {
      break;
    }
  };
}

void MotorEvent::stopMotors()
{
  setDirection(0);
  setSpeed(0);
}

char MotorEvent::getSpeed()
{
  return m_speed_external;
}

short MotorEvent::getSpeedMotorLeft()
{
  return  m_speed_motor_left;
}
short MotorEvent::getSpeedMotorRight()
{
  return m_speed_motor_right;
}

void MotorEvent::setSpeed(short speed)
{
  short temp_speed = 0;
  
  m_speed_external = speed;
  temp_speed = SPEED_TRANS_FAKTOR*speed;
  m_speed = restrictSpeed(temp_speed);
  calcMotorSpeed();
}

short MotorEvent::restrictSpeed(short speed)
{
  if(speed > SPEED_MAX)
    return SPEED_MAX;
  else if(speed < (-SPEED_MAX))
    return (-SPEED_MAX);
  else
    return speed;
}

void MotorEvent::alterSpeed(char value)
{
  if(m_update_motors)
  {
    m_update_motors = false;
    setSpeed(m_speed_external + value);
  }
}

void MotorEvent::calcMotorSpeed()
{
  short left_wheel = 0;
  short right_wheel = 0;
  short direction = m_direction;
  short radius = m_direction; //interpretet as an radius to turn
  short speed = m_speed;
  
  if(direction == 0)
  {//Move straight forward without turning
    left_wheel = speed;
    right_wheel = left_wheel;
  }
  else if(direction == TURN_VALUE)
  {//turn on spot
    left_wheel = speed;
    right_wheel = -speed;
  }
    else if(direction == -TURN_VALUE)
  {//turn on spot
    left_wheel = -speed;
    right_wheel = speed;
  }
  else
  {//if direction is negative (radius is then also negative) the left wheel will be slowed, else the right wheel
    left_wheel = speed + (WHEEL_DISTANCE*speed)/(2*radius);
    right_wheel = speed - (WHEEL_DISTANCE*speed)/(2*radius);
  }
  
  m_speed_motor_left = restrictSpeed(left_wheel);
  m_speed_motor_right = restrictSpeed(right_wheel);
  m_update_motors = true;
}
  

short MotorEvent::getDirection()
{
  return m_direction;
}
void MotorEvent::setDirection(short direction)
{//comes as [-127, 127], goes as [-1260, 1260]
  //switch steering direction
  //direction = (-1)*direction;
  if(direction >= -TURN_MIN && direction <= TURN_MIN)
  {//this means do not turn at all
    m_direction = 0;
  }
  else if (direction == -DIRECTION_MAX || direction == DIRECTION_MAX)
  {
    if(direction > 0)
     m_direction = -TURN_VALUE;
    else
     m_direction = TURN_VALUE;
  }
  else
  {
    if(direction >= 0)
     m_direction = TURN_VALUE - direction * TURN_MULTIPLICATOR - DIRECTION_OFFSET;
    else
     m_direction = (-TURN_VALUE) - (direction * TURN_MULTIPLICATOR + DIRECTION_OFFSET);
  }
  calcMotorSpeed();
}

unsigned char MotorEvent::getInternalState()
{
  return m_state;
}

void MotorEvent::setInternalState(unsigned char state, bool update)
{
  m_state = (State)state;
  m_update = update;
}

void MotorEvent::executeAction()
{
	//nothing to do here!
}
