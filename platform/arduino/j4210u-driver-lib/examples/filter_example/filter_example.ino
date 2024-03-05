/**
 * J4210U UHF Reader/Writer demo.
 *
 * @author: Ejaz Jamil
 */

#include <Arduino.h>
#include <J4210U.h>
#include <vector>

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
Example of Masked/Filtered Inventory
  Lets take a known tag with EPC of 33 30 AF EC 2B 01 15 C0 00 00 00 01
  If we want to do an inventory Scan but want a specific tag that starts with 33 30
  Starting point of the filter in the EPC will be 0 and our filterData is 33 30  and the length of filter is 2
  then our code will look like
*/
  unsigned char filterData[] = {0x33, 0x30}; // first 2 bytes of epc
  int filterOffset = 0;                      // matching starts from 0 index of EPC
  int filterLength = sizeof(filterData);

  uhf.SetFilter(filterOffset, filterLength, (unsigned char *)filterData);
  int numOfTags = uhf.Inventory(true);
  std::vector<ScanData> v;
  for (int i = 0; i < numOfTags; i++)
  {
    Serial.println("From First Filter!");
    ScanData *sd;
    sd = uhf.GetResult(i);
    v.push_back(*sd);
    uhf.printtag(i);
  }
  delay(3000);

  /*
  Lets take another known tag with EPC of 32 70 AF EC 2B 00 02 00 00 00 03
  if we want to do the same type of filtering but this time we want to find with last 2 byte 00 03 then
  Starting point of the filter in the EPC will be 10 and our filterData is 00 03  and the length of filter is 2
  then our code will look like
*/
  unsigned char filterData2[] = {0x00, 0x03}; // first 2 bytes of epc
  filterOffset = 10;                          // matching starts from 0 index of EPC
  filterLength = sizeof(filterData2);

  uhf.SetFilter(filterOffset, filterLength, (unsigned char *)filterData2);
  numOfTags = uhf.Inventory(true);
  for (int i = 0; i < numOfTags; i++)
  {
    Serial.println("From Second Filter!");
    ScanData *sd;
    sd = uhf.GetResult(i);
    uhf.printtag(i);
  }
  delay(3000);
}
