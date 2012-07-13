/*! \file LightEvent.h sets the light and color mode periodically*/

#include <Arduino.h>
#include "TimeEvent.h"
#include "SoftI2CMaster.h"

#ifndef LIGHT_EVENT_H
#define LIGHT_EVENT_H

#define SEND 1

/*! \class LightEvent
Sets the Light (Color, Brightnes, etc..) periodically.

*/
class LightEvent : public TimeEvent
{
  public:
  
  LightEvent();
  LightEvent(SoftI2CMaster * wire);
  ~LightEvent(){};
  
  /*! Callback which is executed periodically*/
  virtual void onTimeEvent();
  /*! Returns an internal state.*/
  virtual unsigned char getInternalState();
  /*! Sets an internal state.*/
  virtual void setInternalState(unsigned char state, bool update=false);
  /*! Executes a more complex (and time consuming) action.*/
  virtual void executeAction();
  
  void setHSB(byte red, byte green, byte blue, byte brightness);
  void setHSB(byte hue, byte brightness);
  
  void setHSBColor(byte red, byte green, byte blue);
  void setHSBBrightness(byte brightness);
  
  
  void setBrightness(byte value);
  void setRed(byte value);
  void setGreen(byte value);
  void setBlue(byte value);
  void turnOn(bool value);
  
  byte getHue(){return m_hue;}
  
  enum State {
    init,
    shine,
    blink,
    random,
    random_blink
  };
  
  private:
  
  /*! Sets the color accordin to the internal value of red, green and blue
  */
  void  setColor();
  
  /*! Fades to a rgb color*/
  
  void fadeColor();
  
  /*! Sets the color in hsb mode*/
  void fadeHSB();
  
  /*! Sets the blikm to random hsb mode*/
  void fadeHSBRandom(byte hue_randomness=0x40);
  
  /*! Maps a value between [0,63] to [0,255]*/
  byte mapHue(byte value);

  /*! Maps a value between [0,3] to [0,255]*/  
  byte mapBrightness(byte value);
  
  /*! Maps the value from 0-3 to a value between 0-255
  */
  byte mapLightValue(byte value);
  
  /*! Turn the LED on or of*/
  
  void turnOn();
  
  public:
  /*! Initialises the LEDs (stops the standard script, etc.)*/
  void initLight();
  
  private:
  
  /*! Sends the data over I2C.
  */
  #if SEND
  SoftI2CMaster *m_wire;
  #endif
  
  /*! Determines if the LED should blink, shine, or change their color randomly
  */
  State m_state;
  State m_old_state;
  
  /*! basically on or of
  */
  bool m_light_state;
  byte m_hue;
  byte m_brightness;
  byte m_red;
  byte m_green;
  byte m_blue;
  
  int m_blink_counter;
};

#endif
