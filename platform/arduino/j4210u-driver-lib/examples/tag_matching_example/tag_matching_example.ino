/**
 * J4210U UHF Reader/Writer demo.
 *
 * @author: Ejaz Jamil
 */

#include <Arduino.h>
#include <J4210U.h>

#define DEFAULT_READER_BAUD 57600
#define COM_PORT_BAUD 9600
// #define NANO_ESP32

#if defined(__AVR_ATmega2560__)
// for arduino mega 17(RX2), 16(TX2)
#define UART2 Serial2
#elif defined(NANO_ESP32)
#include "HardwareSerial.h"
HardwareSerial UART2(2);
#elif defined(ESP32)
// for ESP32 Wroom -(16) RX2  (17) TX2
#define UART2 Serial2
#elif defined(__TM4C1294NCPDT__)
#define UART2 Serial7
#elif defined(__AVR_ATmega32U4__) || defined(__AVR_ATmega16U4__)
#define UART2 Serial1 // for arduino leonardo : 0(RX), 1(TX)
#elif defined(TARGET_RP2040)
#define UART2 Serial2
#else
Serial.begin(9600);
Serial.println("The Example does not support this MCU");
#endif

J4210U uhf(&UART2, 57600);

int printSettings()
{
  ReaderInfo ri;
  uhf.GetSettings(&ri);
  uhf.printsettings(&ri);
  return 1;
}

// the setup routine runs once when you press reset:
void setup()
{
  Serial.begin(COM_PORT_BAUD); // for serial monitor
#if defined(NANO_ESP32)
  UART2.begin(DEFAULT_READER_BAUD, SERIAL_8N1, D6, D7); // D6(RX), D7(TX)
#else
  UART2.begin(DEFAULT_READER_BAUD); // Rx2, Tx2 pin for the Reader
#endif
  printSettings();
}

// the loop routine runs over and over again forever:
void loop()
{
  /*
  Example of Taking action based on recognized tag:
      Lets take 2 tag tag 1 and tag2 that is recognized
      and we want to take a specific action if our tags matches
      a very basic way of implementing that would be
  */
  unsigned char tag1epc[] = {0x33, 0x30, 0xAF, 0xEC, 0x2B, 0x01, 0x15, 0xC0, 0x00, 0x00, 0x00, 0x01};
  unsigned char tag2epc[] = {0x32, 0x70, 0xAF, 0xEC, 0x2B, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x03};

  int numOfTags = uhf.Inventory(true);
  for (int i = 0; i < numOfTags; i++)
  {

    ScanData *sd;
    sd = uhf.GetResult(i);
    if (memcmp(sd->EPCNUM, tag1epc, sizeof(tag1epc)))
    {
      // do something
      Serial.println("TAG Matched");
    }
    else
    {
      continue;
    }
    uhf.printtag(i);
  }

  delay(3000);
}
