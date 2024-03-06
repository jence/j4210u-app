/**
 * J4210U UHF Reader/Writer demo.
 *
 * @author: Ejaz Jamil
*/

#include <Arduino.h>
#include "J4210U.h"
#define DEFAULT_READER_BAUD 57600
#define COM_PORT_BAUD 9600
// #define NANO_ESP32 0

#if defined(__AVR_ATmega2560__)
  //for arduino mega 17(RX2), 16(TX2)
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
  #define UART2 Serial1  // for arduino leonardo : 0(RX), 1(TX)

#elif defined(TARGET_RP2040)
  #define UART2 Serial2
#else
Serial.begin(9600);
Serial.println("The Example does not support this MCU");
#endif

J4210U uhf(&UART2, 57600);

void getMemoryContent(J4210U &uhf, ScanData *sd) {
}

void getTID(J4210U &uhf, ScanData *sd) {
  cprintf(">>> getTID() STARTED:\n");
  char epcnum[sd->epclen * 2];
  bytes2hex(sd->EPCNUM, sd->epclen, epcnum);
  cprintf("Getting TID for EPCNUM: %s\n", epcnum);

  unsigned char tid[16];
  unsigned char tidlen = 0;
  if (!uhf.GetTID(sd->EPCNUM, sd->epclen, tid, &tidlen)) {
    //uhf.printtag(i);
    char str[32];
    bytes2hex(sd->EPCNUM, sd->epclen, str);
    cprintf("TID: %s\n", str);
  } else {
    cprintf("Failed to obtain TID.\n");
  }
  cprintf("getInventory() ENDED <<<\n");
}

void getTagInfo(J4210U &uhf, ScanData *sd) {
}

int printSettings() {
  ReaderInfo ri;
  uhf.GetSettings(&ri);
  uhf.printsettings(&ri);
  return 1;
}

int setSettings() {
  ReaderInfo ri;
  ri.ScanTime = 300;
  ri.BeepOn = 0;
  if (!uhf.SetSettings(&ri)) {
    cprintf("Failed to set settings.\n");
    return 0;
  } else {
    uhf.GetSettings(&ri);
    uhf.printsettings(&ri);
  }
  return 1;
}

int getInventory() {
  cprintf("\n>>> getInventory() STARTED:\n");

  int n = uhf.Inventory(false);
  cprintf("Found %d tags.\n", n);
  for (int i = 0; i < n; i++) {
    uhf.printtag(i);
  }
  cprintf("getInventory() ENDED <<<\n");
  return n;
}

void scan(ScanData *sd) {
  int n = getInventory();
  for (int i = 0; i < n; i++) {
    sd = uhf.GetResult(i);
    if (sd == 0)
      break;
    cprintf("Getting TID for tag %d\n", i);
    unsigned char tid[16];
    unsigned char tidlen = 0;
    if (uhf.GetTID(sd->EPCNUM, sd->epclen, tid, &tidlen)) {
      uhf.printtag(i);
      char str[32];
      bytes2hex(tid, tidlen, str);
      cprintf("TID: %s\n", str);
    }
  }
}

// the setup routine runs once when you press reset:
void setup() {
  Serial.begin(COM_PORT_BAUD);  // for serial monitor
#if defined(NANO_ESP32)
  UART2.begin(DEFAULT_READER_BAUD, SERIAL_8N1, D6, D7);  // D6(RX), D7(TX)
#else
  UART2.begin(DEFAULT_READER_BAUD);  //Rx2, Tx2 pin for the Reader
#endif
  printSettings();
}

// the loop routine runs over and over again forever:
void loop() {
  ScanData sd;
  scan(&sd);
  // printSettings();
  delay(3000);
}
