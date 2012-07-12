
#include "LightEvent.h"

//TODO: change to pin PD2 (4 on the LCD Port) and PD4 (6 ont the LCD Port) when used together with bt.
#define BT_AVAILABLE 1


#if BT_AVAILABLE
  #define SDA_I2C 2
  #define SCL_I2C 4
#else
  #define SDA_I2C 1
  #define SCL_I2C 0
#endif

#define BLINK_DURATION 5
#define RANDOM_DURATION 4
#define BLINK_ADRESS 0x09
#define STOP_SCRIPTS 'o'
#define SET_COLOR 'n'
#define FADE_COLOR 'c'
#define FADE_HSB 'h'
#define RANDOM_HSB 'H'
#define RANDOM_HUE_RANGE 0x30
#define RANDOM_BRIGHTNESS_RANGE 0x0f
#define SATURATION 0xff

#define FAKTOR_HUE 2.5 //map a value between [0,63] to [0-255] (hue space)
#define FAKTOR_BRIGHTNESS 85 //map [0,3] to [0,255]


 LightEvent::LightEvent():
 TimeEvent(),
 #if SEND
  m_wire(new SoftI2CMaster(SDA_I2C, SCL_I2C)),
#endif
 m_state(init),
 m_light_state(false),
 m_hue(0),
 m_brightness(0),
 m_red(0),
 m_green(0xff),
 m_blue(0),
 m_blink_counter(0)
{
  m_update = true;
}

LightEvent::LightEvent(SoftI2CMaster *wire):
TimeEvent(),
 #if SEND
  m_wire(wire),
#endif
 m_state(init),
 m_light_state(false),
 m_hue(0),
 m_brightness(0),
 m_red(0),
 m_green(0xff),
 m_blue(0),
 m_blink_counter(0)
{
  m_update = true;
}



void LightEvent::onTimeEvent()
{
  if(m_update)
  {//only if new data is available
    if(m_old_state != m_state)
    {//if state changed
      m_light_state = true;
      turnOn();
      m_old_state = m_state;
    }
    switch(m_state)
    {
      case init:
      {
        m_update = false;
        initLight();
        m_state = shine;
        break;
      }
      case shine:
      {
        fadeHSB();
        m_update = false;//NOT before the switch
       break; 
      }
      case blink:
      {//Update is never cleared here, because it counts the calls and works as timer
      if(m_blink_counter >= BLINK_DURATION)
      {//only if time is up NEVER clear update!
        m_blink_counter = 0;
        if(m_light_state)
         m_light_state = false;
        else
         m_light_state = true;
        //fadeHSB();
        turnOn();
      }
      else
       m_blink_counter++;
       
        break;
      }
      case random:
      {//Update is never cleared here, because it counts the calls and works as timer
      if(m_blink_counter >= RANDOM_DURATION)
      {//only if time is up NEVER clear update!
        m_blink_counter = 0;
        //fadeHSB();
        fadeHSBRandom();
      }
      else
       m_blink_counter++;
       
        break;
      }
      case random_blink:
      {//Update is never cleared here, because it counts the calls and works as timer
      if(m_blink_counter >= BLINK_DURATION)
      {//only if time is up NEVER clear update!
        m_blink_counter = 0;
        if(m_light_state)
        {
          turnOn();
         m_light_state = false;
         fadeHSBRandom(0xf0);
        }
        else
        {
          turnOn();
          m_light_state = true;
        }
      }
      else
       m_blink_counter++;
       
        break;
      }
      default:
      {
        //what to do?
      }
    }
  };
        
}

unsigned char LightEvent::getInternalState()
{
  return m_state;
}


void LightEvent::setInternalState(unsigned char state, bool update)
{
  m_state = (State)state;
  m_update = true;
}

void LightEvent::executeAction()
{
	//nothing to do here!
}

void LightEvent::initLight()
{
 #if SEND
  m_wire->beginTransmission(BLINK_ADRESS);
  m_wire->send(STOP_SCRIPTS);
  m_wire->endTransmission();
  delay(100);
  m_wire->beginTransmission(BLINK_ADRESS);
  m_wire->send(SET_COLOR);
  m_wire->send(0x01);
  m_wire->send(0x01);
  m_wire->send(0xff);
  m_wire->endTransmission();
 #endif
}

void LightEvent::setRed(byte value)
{
  m_red = mapLightValue(value);
  m_update = true;
}

void LightEvent::setGreen(byte value)
{
  m_green = mapLightValue(value);
  m_update = true;
}

void LightEvent::setBlue(byte value)
{
  m_blue = mapLightValue(value);
  m_update = true;
}


void LightEvent::setHSBColor(byte red, byte green, byte blue)
{
  setHSB(red, green, blue, m_brightness);
}

void LightEvent::setHSBBrightness(byte brightness)
{
  setHSB(m_hue, brightness);
}

void LightEvent::setHSB(byte red, byte green, byte blue, byte brightness)
{
  setHSB(mapHue(red|green<<2|blue<<4), mapBrightness(brightness));
}

void LightEvent::setHSB(byte hue, byte brightness)
{
  m_hue = hue;
  m_brightness = brightness;
  m_update = true;
}

byte LightEvent::mapHue(byte value)
{
  return (byte)(FAKTOR_HUE * (float)value);
}

byte LightEvent::mapBrightness(byte value)
{
  if(value == 0)
      return 0;
    if(value == 1)
      return 60;//100
    if(value == 2)
      return 110;//200
    if(value == 3)
      return 0xff;
    else
      return 0x00;
}

void LightEvent::turnOn()
{
   #if SEND
  if(m_light_state)
  {
    m_wire->beginTransmission(BLINK_ADRESS);
    m_wire->send(FADE_HSB); // c is fade to color
    m_wire->send(m_hue); // hue
    m_wire->send(SATURATION); // saturation
    m_wire->send(m_brightness); // brightness
    m_wire->endTransmission();
  }
  else
  {
    m_wire->beginTransmission(BLINK_ADRESS);
    m_wire->send(FADE_HSB); // c is fade to color
    m_wire->send(m_hue); // hue
    m_wire->send(0); // saturation
    m_wire->send(0); // brightness
    m_wire->endTransmission();
  }
  #endif
}

byte LightEvent::mapLightValue(byte value)
{
    if(value == 0)
      return 0;
    if(value == 1)
      return 0x64;//100
    if(value == 2)
      return 0xC8;//200
    if(value == 3)
      return 0xff;
  }

void LightEvent::setColor()
{
  #if SEND
  m_wire->beginTransmission(BLINK_ADRESS);
  m_wire->send(SET_COLOR); // c is fade to color
  m_wire->send(m_red); // value for red channel
  m_wire->send(m_green); // value for green channel
  m_wire->send(m_blue); // value for blue channel*/
  m_wire->endTransmission();
  #endif
}

void LightEvent::fadeColor()
{
  #if SEND
  m_wire->beginTransmission(BLINK_ADRESS);
  m_wire->send(FADE_COLOR); // c is fade to color
  m_wire->send(m_red); // value for red channel
  m_wire->send(m_green); // value for green channel
  m_wire->send(m_blue); // value for blue channel*/
  m_wire->endTransmission();
  #endif
}

void LightEvent::fadeHSB()
{
  #if SEND
  m_wire->beginTransmission(BLINK_ADRESS);
  m_wire->send(FADE_HSB); // c is fade to color
  m_wire->send(m_hue); // hue
  m_wire->send(SATURATION); // saturation
  m_wire->send(m_brightness); // brightness*/
  m_wire->endTransmission();
  #endif
}

/*void LightEvent::fadeHSBRandom()
{
  #if SEND
  m_wire->beginTransmission(BLINK_ADRESS);
  m_wire->send(RANDOM_HSB); 
  m_wire->send(RANDOM_HUE_RANGE); // value randomness of hue
  m_wire->send(0x00); // randomness of saturation
  m_wire->send(0x00); // randomness of brightness
  m_wire->endTransmission();
  #endif
}*/

void LightEvent::fadeHSBRandom(byte hue_randomness)
{
  #if SEND
  m_wire->beginTransmission(BLINK_ADRESS);
  m_wire->send(RANDOM_HSB); 
  m_wire->send(hue_randomness); // value randomness of hue
  m_wire->send(0x00); // randomness of saturation
  m_wire->send(0x00); // randomness of brightness*/
  m_wire->endTransmission();
  #endif
}


