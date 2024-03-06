/*
 * J4210U.h
 *
 *  Created on: Mar 2, 2023
 *      Author: soalib
 */

#ifndef J4210U_H_
#define J4210U_H_

#include <Arduino.h>

#define LIB_VERSION 0x0108

extern void cprintf(const char *fmt, ... );
extern unsigned long stopwatch(bool start);
extern void bytes2hex(unsigned char *uid, int n, char *z);

typedef struct ReaderInfo {
    // the following constants are reader constants
    // they cannot be changed.
    int Serial = 0;
    char VersionInfo[2] = {0,0};
    unsigned char Antenna = 0;

    // the following variables are reader parameters
    // which can be change. Call SetSettings function
    // to change the settings.
    unsigned char ComAdr;
    unsigned char ReaderType;
    unsigned char Protocol;
    unsigned char Band;
    unsigned char Power;
    unsigned short ScanTime;
    unsigned char BeepOn;
    unsigned char Reserved1;
    unsigned char Reserved2;

    int MaxFreq  = 0;
    int MinFreq  = 0;
    int BaudRate = 0;
} ReaderInfo;

struct ScanData {
    unsigned char type; // 1 byte
    unsigned char ant;  // 1 byte
    signed char RSSI;  // 1 byte
    unsigned char count; // 1 byte
    unsigned char EPCNUM[64]; // 12 byte or 62 byte
    int epclen; //
};

struct ScanResult {
    unsigned char ant;  // 1 byte
    char RSSI;  // 1 byte
    unsigned char count; // 1 byte
    unsigned char epclen;
    unsigned char epcnum[12]; // 12 byte or 62 by7e
};

struct Filter {
    // these are word address (or even address)
    //unsigned char MaskMem;
    int MaskAdr;
    unsigned char MaskData[64];
    unsigned char MaskLen;
};

enum MemoryType {
    MEM_PASSWORD,
    MEM_EPC,
    MEM_TID,
    MEM_USER
};

enum TagType {
    UNKNOWN = 0,

    HIGGS_3,
    HIGGS_4,
    HIGGS_EC,
    HIGGS_9,

    MONZA_4QT,
    MONZA_4E,
    MONZA_4D,
    MONZA_4I,
    MONZA_5,
    MONZA_R6,
    MONZA_R6P,
    MONZA_X2K,
    MONZA_X8K,
    IMPINJ_M730,
    IMPINJ_M750,

    UCODE_7,
    UCODE_8,
    UCODE_8M,
    UCODE_DNA,
    UCODE_DNA_CITY,
    UCODE_DNA_TRACK,

    EM4423,
};

struct TagInfo {
    int type;
    int tidlen;
    unsigned char tid[64];
    char chip[16];
    int epclen;
    int userlen;
    int pwdlen;
};

class J4210U
{
    HardwareSerial *handle_ = 0;
    int scantime_ = 300;
    String error_;
    int baud_;
    unsigned char session_ = 0x00;
    unsigned char q_ = 2;
    unsigned char readertype_ = 16;
    Filter filter_;
    int scansize_ = 10;
    ScanData *scan_ = 0;
    unsigned char password_[4] = {0,0,0,0};

    unsigned int GetSeriaNo();
    unsigned short gencrc(unsigned char const  *data, unsigned char n);
    void platform_sleep(unsigned long ms) {
        delay(ms);
    }
    bool platform_write(unsigned char* message_buffer, unsigned short buffer_size,
                                       unsigned short* number_bytes_transmitted);
    bool platform_read(unsigned char* message_buffer, unsigned short buffer_size,
                                      unsigned short* number_bytes_received, unsigned short timeout_ms);
    int transfer(unsigned char *command, int cmdsize, unsigned char* response, int responseSize, int sleepms, int pause);
    int getresponse(unsigned char* response, int responseSize, int *len, unsigned char **data);
    bool SetRegion(unsigned char region);
    bool SetRfPower(unsigned char powerDbm);
    bool SetBeep(bool on);
    bool SetScanTime(unsigned char scantime);
    bool SetBaudRate (int baud);
    bool clearbuff();
    int getBaudIndex(int baud);
    int gettagnum();
    int InventoryNB(ScanData *v, bool filter);
    int platform_available();

public:
    J4210U(HardwareSerial *serial, int baudrate);
    virtual ~J4210U();

    bool GetSettings(ReaderInfo *ri);
    bool SetSettings(ReaderInfo *ri);
    void printsettings(ReaderInfo *ri);
    void printarr(unsigned char *arr, int size);
    void printtag(int index);
    int Inventory(bool filter);
    bool GetTID(unsigned char *epcnum, unsigned char epclen, unsigned char *tid, unsigned char *tidlen);

    ScanData *GetResult(int index) {
        if (index >= scansize_)
            return 0;
        return &scan_[index];
    }

    bool Auth(unsigned char* password, unsigned char size);
    bool Write(unsigned char* epcnum, unsigned char epcLen, unsigned char* data, int wsize, int windex, int memtype);
    void LibVersion(unsigned char *version);
    bool WriteWord(unsigned char* epcnum, unsigned char epcLen, unsigned char* data, unsigned char windex, int memtype);
    bool Read(unsigned char* epcnum, unsigned epclen, unsigned char* data, int wnum, int windex, int memtype);
    bool ReadWord(unsigned char* epcnum, unsigned char epclen, unsigned char* data, int windex, int memtype);
    bool CreateFilter(char* filterDesc);
    bool SetFilter(int maskAdrInByte, int maskLenInByte, unsigned char *maskDataByte);
    bool WriteMemWord(unsigned char *epcnum, unsigned char epclen, unsigned char *data, unsigned char windex);
    bool WriteEpcWord(unsigned char *epcnum, unsigned char epclen, unsigned char *data, unsigned char windex);
    bool ReadMemWord(unsigned char *epcnum, unsigned char epclen, unsigned char *data, unsigned char windex);
    bool ReadEpcWord(unsigned char* epcnum, unsigned char epclen, unsigned char* data, int windex, int memtype);
    bool TagExists(unsigned char* epcnum, unsigned char epclen);
    bool SetEPC(unsigned char *epcnum, unsigned char epclen, unsigned char *newepc);
    bool Kill();
    bool Lock();
    bool Erase();
    bool Is496Bits();
    bool Set496Bits(bool bits496);
    bool SetPassword(unsigned char* epcnum, unsigned char epcLen, unsigned char* pass, unsigned char size);
    bool SetKillPassword(unsigned char* epcnum, unsigned char epcLen, unsigned char* pass, unsigned char size);
    bool GetKillPassword(unsigned char* epcnum, int epclen, unsigned char* pass, unsigned char *passlen);
    bool SetGPO(unsigned char gpoNumber);
    char GetGPI(unsigned char gpiNumber);
    void LastError(char * buffer);
    bool SetQ(unsigned char q);
    bool SetSession(unsigned char sess);
    bool SetScanBufferSize(int n) {
        scansize_ = n;
        return 1;
    }
    // --------------
    int GetTagInfo(unsigned char *tid, TagInfo *info);
};

#endif /* J4210U_H_ */
