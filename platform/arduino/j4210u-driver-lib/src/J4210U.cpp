/*
 * J4210U.cpp
 *
 *  Created on: Mar 2, 2023
 *      Author: soalib
 */

#include "stdarg.h"
#include "J4210U.h"
#include "string.h"

#define PRESET_VALUE 0xFFFF
#define POLYNOMIAL  0x8408


// --------------- Utility Functions ---------------------
void cprintf(const char *fmt, ... ){
    char tmp[128] = ""; // resulting string limited to 64 chars
    va_list args;
    va_start (args, fmt );
    vsnprintf(tmp, sizeof(tmp), fmt, args);
    va_end (args);
    Serial.print(tmp);
    Serial.flush();
}

unsigned long stopwatch(bool start) {
    static unsigned long m = 0;
    if (start) {
        m = millis();
        return 0.0;
    } else {
        unsigned long elapsed = millis() - m;
        return elapsed;
    }
}

void bytes2hex(unsigned char *uid, int n, char *z) {
    char p[3] = "00";
    *z = 0;
    for(int i=0;i<n;i++) {
        sprintf(p, "%02X", uid[i] & 0xFF);
        strcat(z, p);
    }
}

#if 1
// --------------- Platform Interface Functions ---------------------
bool J4210U::platform_write(unsigned char* message_buffer, unsigned short buffer_size,
                                   unsigned short* number_bytes_transmitted)
{
    *number_bytes_transmitted = handle_->write(message_buffer, buffer_size);
    handle_->flush();
    return *number_bytes_transmitted > 0;
}

bool J4210U::platform_read(unsigned char* message_buffer, unsigned short buffer_size,
                                  unsigned short* number_bytes_received, unsigned short timeout_ms)
{
    handle_->setTimeout(timeout_ms);
    *number_bytes_received = handle_->readBytes(message_buffer, buffer_size);
    return *number_bytes_received > 0;
}
#endif

unsigned short J4210U::gencrc(unsigned char const  *data, unsigned char n)
{
    unsigned char i,j;
    unsigned short int  crc = PRESET_VALUE;

    for(i = 0; i < n; i++) {
        crc = (crc ^ (data[i] & 0xFF)) & 0xFFFF;
        //cprintf("%04X ", crc);
        for(j = 0; j < 8; j++) {
            if(crc & 0x0001) {
                crc = ((crc >> 1) ^ POLYNOMIAL) & 0xFFFF;
            } else {
                crc = (crc >> 1) & 0xFFFF;
            }
        }
    }
    return crc;
}

// ------------------------- Private Members -----------------------------
int J4210U::transfer(unsigned char *command, int cmdsize, unsigned char* response, int responseSize, int sleepms, int pause) {
    unsigned short n;
    command[0] = cmdsize - 1;
    unsigned short crc = gencrc(command, cmdsize - 2);
    command[cmdsize-2] = crc & 0xFF;
    command[cmdsize-1] = (crc >> 8);

    //printarr(command, cmdsize);
    //stopwatch(true);
    int e = platform_write(command, cmdsize, &n);

    //cprintf("Write: e = %d, n = %d\n", e, n);
    if (!e) {
        return e;
    }
    response[0] = 0;
    if (pause > 0) {
        delay(pause);
    }
    e = platform_read(response, responseSize, &n, sleepms);
    //cprintf("Read Finished in = %5.2f, size = %d\n", stopwatch(false), n);
    //printarr(response, n);
    //cprintf("n = %d, e = %d\n", n, e);
    if (!e) {
        return 1;
    }
    if (n > 0)
        return n;
    //printarr(response, response[0]+1);
    return 0;
}


unsigned int J4210U::GetSeriaNo() {
    unsigned char command[] = {0x04,0x00,0x4C, 0, 0};
    unsigned char response[10];
    int n = transfer(command, 5, response, sizeof(response), 100, 100);
    if (n==0) {
        error_ = "FAILED to retrieve serial number.";
        return 0;
    }
    unsigned int SerialNo;
    SerialNo = response[4] + (response[5]<<8) + (response[6]<<16) + (response[7]<<24);
    return SerialNo;
}

bool J4210U::GetSettings(ReaderInfo *ri) {
    int retries = 10;
    int timeout = 0;
    do {
        unsigned char command[] = {0x04,0x00,0x21, 0, 0};
        unsigned char response[64];
    //    int retries = 10;
        int n = 0;
        n = transfer(command, sizeof(command), response, sizeof(response), 100, 100);

        //printarr(response, sizeof(response));
        if (command[2] != response[2])
            continue;
        if (n!=18) {
            char err[128];
            sprintf(err,"Failed to load all reader info. Expecting 18 parameters, returned %d parameters.", n);
            //cprintf("Failed to load all reader info. Expecting 18 parameters, returned %d parameters.", n);
            error_ = err;
            continue;
        }


    //    int status = response[3];
        ri->ComAdr = response[1];
        char* ver = ri->VersionInfo;
        ver[0] = response[4];
        ver[1] = response[5];
        ri->ReaderType = response[6];
        readertype_ = ri->ReaderType;
        ri->Protocol = response[7];
        int dmaxfre = response[8];
        int dminfre = response[9];
        ri->Power = response[10];
        ri->ScanTime = response[11] * 100;
        ((unsigned char*)&ri->Antenna)[0] = response[12];
        ri->BeepOn = response[13];
        // bbffffff
        ri->Band = ((dmaxfre >> 6) << 2) | (dminfre >> 6);
        ri->MinFreq = (dminfre & 0x3f);
        ri->MaxFreq = dmaxfre & 0x3f;

        if (ri->Protocol & 0x01) {
            ri->Protocol = 0x6B;
        } else if (ri->Protocol & 0x02) {
            ri->Protocol = 0x6C;
        }

        switch(ri->Band) {
        case 1: // Chinese Band
            ri->MinFreq = 920.125 * 1000;
            ri->MaxFreq = 920.125 * 1000 + ri->MaxFreq * 0.25 * 1000;
            ri->Band = 'C';
            break;
        case 2: // US Band
            ri->MinFreq = 902.75 * 1000;
            ri->MaxFreq = 902.75 * 1000 + ri->MaxFreq * 0.5 * 1000;
            ri->Band = 'U';
            break;
        case 3: // Korean Band
            ri->MinFreq = 917.1 * 1000;
            ri->MaxFreq = 917.1 * 1000 + ri->MaxFreq * 0.2 * 1000;
            ri->Band = 'K';
            break;
        case 4: // EU Band
            ri->MinFreq = 865.1 * 1000;
            ri->MaxFreq = 865.1 * 1000 + ri->MaxFreq * 0.2 * 1000;
            ri->Band = 'E';
            break;
        }

        ri->Serial = GetSeriaNo();
        ri->BaudRate = baud_;
        scantime_ = ri->ScanTime;
        //printarr((unsigned char*)ri, sizeof(ReaderInfo));

        return true;
    } while(retries--);
    return false;
}

void J4210U::printsettings(ReaderInfo *ri) {
    cprintf("Version: %d.%d\n", ri->VersionInfo[0], ri->VersionInfo[1]);
    cprintf("Serial: %u\n",ri->Serial);
    cprintf("Reader Type: %d\n", ri->ReaderType);
    cprintf("Protocol: %X\n", ri->Protocol);
    cprintf("Band: %c\n", ri->Band);
    cprintf("Min Freq: %d KHz\n", ri->MinFreq);
    cprintf("Max Freq: %d KHz\n", ri->MaxFreq);
    cprintf("Power: %ddB\n", ri->Power);
    cprintf("Scan Time: %dms\n", ri->ScanTime);
    cprintf("Antenna: %d\n", ri->Antenna);
    cprintf("BeepOn: %d\n", ri->BeepOn);
    cprintf("Baud Rate: %d\n", ri->BaudRate);
}

J4210U::J4210U(HardwareSerial *serial, int baudrate)
{
    handle_ = serial;
    baud_ = baudrate;
    //handle_->begin(baudrate);
    scan_ = (ScanData*)malloc(sizeof(ScanData) * scansize_);
}

J4210U::~J4210U()
{
    free(scan_);
}

void J4210U::printarr(unsigned char *arr, int size) {
    for(int i=0;i<size;i++) {
        cprintf("%02X ", arr[i]);
    }
    cprintf("\n");
    fflush(stdout);
}

int J4210U::getresponse(unsigned char* response, int responseSize, int *len, unsigned char **data) {
    int i = 0;
    *len = response[i++] - 5;
//    unsigned char adr = response[i++];
//    unsigned char cmd = response[i++];
    unsigned char status = response[i++];

    *data = response + 4;
    return status;
}

bool J4210U::SetRegion(unsigned char region) {
    unsigned char maxfre;
    unsigned char minfre;
    switch(region) {
    case 'C':
        maxfre = 0x13;
        minfre = 0x40;
        break;
    case 'U':
        maxfre = 0x31;
        minfre = 0x80;
        break;
    case 'K':
        maxfre = 0x1F;
        minfre = 0xC0;
        break;
    case 'E':
        maxfre = 0x4E;
        minfre = 0x00;
        break;
    default:
        return false;
    }
    unsigned char command[] = {0x06,0x00,0x22, maxfre, minfre, 0, 0};
    unsigned char response[6];
    int retries = 10;
    do {
        int n = transfer(command, sizeof(command), response, sizeof(response), 100, 100);
        if (n==0) {
            error_ = "FAILED to send write request.";
            continue;
        }
        if (response[3] == 0)
            return true;
    } while(retries--);
    return false;
}

bool J4210U::SetRfPower(unsigned char powerDbm) {
    unsigned char command[] = {0x05,0x00,0x2F, powerDbm, 0, 0};
    unsigned char response[6];
    int n = transfer(command, sizeof(command), response, sizeof(response), 100, 0);
    if (n==0) {
        error_ = "FAILED to send write request.";
        return false;
    }
    return true;
}

bool J4210U::SetBeep(bool on) {
    int retries = 10;
    do {
        unsigned char command[] = {0x05,0x00,0x40, on, 0, 0};
        unsigned char response[6];
        int n = transfer(command, sizeof(command), response, sizeof(response), 100, 100);
        if (n==0)
            continue;
        return true;
    } while(retries--);
    return false;
}

bool J4210U::SetScanTime(unsigned char scantime) {
    unsigned char command[] = {0x05,0x00,0x25, scantime, 0, 0};
    unsigned char response[6];
    int retries = 10;
    do {
        int n = transfer(command, sizeof(command), response, sizeof(response), 2, 0);
        if (n==0) {
            error_ = "FAILED to send write request.";
            continue;
        }
        scantime_ = scantime * 100;
        return true;
    } while(retries--);
    return false;
}

int J4210U::getBaudIndex(int baud) {
    if (baud >= 15000 && baud < 30000)
        baud = 1;
    else if (baud >= 30000 && baud < 45000)
        baud = 2;
    else if (baud >= 45000 && baud < 80000)
        baud = 5;
    else if (baud >= 80000)
        baud = 6;
    else
        baud = 0;
    return baud;
}

bool J4210U::SetBaudRate (int baud) {
    baud = getBaudIndex(baud);
    unsigned char command[] = {0x05,0x00,0x28, (unsigned char)baud, 0, 0};
    unsigned char response[6];
    int retries = 10;
    do {
        int n = transfer(command, sizeof(command), response, sizeof(response), 0, 0);
        if (n==0)
            continue;
        break;
    } while(retries--);
//    baud_ = baud;
    return true;
}

bool J4210U::SetSettings(ReaderInfo *ri) {
    ReaderInfo old;
    bool status = GetSettings(&old);
    if (!status) {
        error_ = "Failed to retrieve settings.";
        return false;
    }
    error_ = "";
    bool verify = true;
    if (old.Band != ri->Band) {
        // change band
        verify &= SetRegion(ri->Band);
        if (!verify)
            error_ += "Failed To Set Region. ";
    }
    if (old.BeepOn != ri->BeepOn) {
        // change beep setting
        verify &= SetBeep(ri->BeepOn);
        if (!verify)
            error_ += "Failed To Set Beep. ";
    }
    if (old.Power != ri->Power) {
        // change power
        verify &= SetRfPower(ri->Power);
        if (!verify)
            error_ = "Failed To Set Power. ";
    }
    if (old.ScanTime != ri->ScanTime) {
        // change scan time
        verify &= SetScanTime(ri->ScanTime/100);
        if (!verify)
            error_ = "Failed To Set ScanTime. ";
    }
    /*
    if (old.ComAdr != ri->ComAdr) {
        // change bcomadr
        verify = SetAddress(ri->ComAdr);
    }
    */
    if (!verify) {
        return false;
    }
    // now verify settings
    ReaderInfo riv;
    status = GetSettings(&riv);
    if (!status) {
        return false;
    }
    verify = true;

//    cprintf("-------ri------\n");
//    printsettings(ri);
//    cprintf("-------riv------\n");
//    printsettings(&riv);
//    cprintf("-------------\n");

    if (ri->Band != riv.Band) {
        error_ += "Failed to verify Band settings. ";
        verify = false;
    }
    if (ri->BeepOn != riv.BeepOn) {
        error_ += "Failed to verify Beep settings. ";
        verify = false;
    }
    if (ri->Power != riv.Power) {
        error_ += "Failed to verify Power settings. ";
        verify = false;
    }
    if (ri->ScanTime != riv.ScanTime) {
        error_ += "Failed to verify Scan Time settings. ";
        verify = false;
    }
    if (baud_ != ri->BaudRate) {
        SetBaudRate(ri->BaudRate);
    }
    return verify;
}

bool J4210U::clearbuff() {
    unsigned char command[] = {0x04,0x00,0x73, 0, 0};
    unsigned char response[6];
    int n = transfer(command, sizeof(command), response, sizeof(response), 0, 0);
    if (n==0)
        return false;
    return (response[3]==0);
}

int J4210U::gettagnum() {
    unsigned char command[] = {0x04,0x00,0x74, 0, 0};
    unsigned char response[8];
    int n = transfer(command, sizeof(command), response, sizeof(response), 0, 0);
    if (n==0) {
        error_ = "FAILED to send write request.";
        return 0;
    }
    if (response[3]==0) {
        return (response[4] << 8) | response[5];
    }
    return 0;
}


/** \brief
 *  Gets Inventory, applies filter if specified.
 * \param port port name in the form of COM1, COM2 ... Baudrate must be one of the following:
 *     9600, 19200, 38400, 57600(default), 115200.
 * \return array which is multiple of dataLength. {type,ant,RSSI,count,epcnum[]}, where type, RSSI
 *     and count are int. epcnum array size is (dataLength - 4*4). The total array size will be even.
 *     If the operation fails, returns 0;
 */
int J4210U::Inventory(bool filter) {
    int n = InventoryNB(scan_, filter);
    return n;
}

/**
 * Filter: x*, 2:x, x:2
 */
int J4210U::InventoryNB(ScanData *v, bool filter) {
//    time_t t0;
//    char str[100];

//    t0 = clock();

//    int count = 0;
    clearbuff();

    unsigned char cmd[128];
    int i = 0;
    cmd[i++] = 0x00;
    cmd[i++] = 0x00;
    cmd[i++] = 0x18;
    cmd[i++] = q_;
    cmd[i++] = session_;

    if (filter) {
        cmd[i++] = 0x01;
        unsigned bitadr = filter_.MaskAdr * 8 + 32;
        cmd[i++] = bitadr >> 8;
        cmd[i++] = bitadr & 0xFF;
        cmd[i++] = filter_.MaskLen*8;
        for(int j=0;j<filter_.MaskLen;j++) {
            cmd[i++] = filter_.MaskData[j];
        }
        cmd[i++] = 0x00;
        cmd[i++] = 0x00;
        cmd[i++] = scantime_/100;
    } else {
        cmd[i++] = 0x00;
        cmd[i++] = 0x80;
        cmd[i++] = scantime_/100;
    }

    cmd[i++] = 0x00;
    cmd[i++] = 0x00;

    unsigned char *command = cmd;
    unsigned char cmdlen = i;

    unsigned char response[1024];
    cprintf("Scantime = %d\n", scantime_);
    stopwatch(true);
    int n = transfer(command, cmdlen, response, sizeof(response), 1, scantime_ + 10);
    //cprintf("Transfer Finished in = %ldms, size = %d\n", stopwatch(false), n);
    //printarr(command, cmdlen);
    //printarr(response, n);
    //printf("transfer returned %d\n", n);

    if (n==0) {
        cprintf("BREAKPOINT [1]\n");
        return -1;
    }
    int len;
    unsigned char* data;
//    int status =
    delay(scantime_/4);
    getresponse(response, sizeof(response), &len, &data);

//    count = (data[0] << 8) | data[1];
//    int tagnum = (data[2] << 8) | data[3];
    //cprintf("Scan Finished in %10.2f seconds.\n", elapsed(t0));
    //cprintf("Count = %d, Tag Num = %d\n", count, tagnum);

//    int tagnum2 = gettagnum();

    // get buffer data
    unsigned char cmdbuf[] = {0x04,0x00,0x72, 0, 0};
    unsigned char bufresp[1024];
    memset(bufresp, 0, sizeof(bufresp));
    int retries = 2;
    bool repeat = true;
    int tagcount = 0;
    while(repeat) {
        do {
            //cprintf("Trying : %d\n");
            n = transfer(cmdbuf, sizeof(cmdbuf), bufresp, sizeof(bufresp), 5, scantime_ + 100);
            //printarr(cmdbuf, sizeof(cmdbuf));
            if (n==0) {
                cprintf("BREAKPOINT [2]\n");
                return -1;
            }
            if (bufresp[3] == 0x01)
                repeat = false;
            if (bufresp[2] == 0x72)
                break;
        } while(retries--);
        //status =
        getresponse(bufresp, sizeof(bufresp), &len, &data);
        //printarr(bufresp, len);

        //cprintf("Buffer Finished in %10.2f seconds.\n", elapsed(t0));
    //    if (status != 1 && status != 3)
    //        return false;
        if (bufresp[6] == bufresp[7] && bufresp[0] == 0) {
            cprintf("BREAKPOINT [3]\n");
            return -1;
        }
        len -= 2;
        int epcnt = *data++; // read epcnum count and then point to the epcnum data
        int N = epcnt;
        cprintf("TAGS DETECTED: %d.\n", epcnt);
        if (epcnt > scansize_) {
            cprintf("Scan buffer size is %d. Only first %d will be reported.\n", scansize_, scansize_);
            N = scansize_;
        }
        tagcount = epcnt;

        for(int j=0;j<N;j++) {
    //        printarr(data, lenepc);
            ScanData sd;
            sd.ant = *data++;
            sd.epclen = *data++;
            memcpy(sd.EPCNUM, data, sd.epclen);
            //sd.data = jstring((char*)data, len);
            unsigned char zeros[sizeof(sd.epclen)];
            memset(zeros, 0, sizeof(zeros));
            if (memcmp(sd.EPCNUM, zeros, sd.epclen) == 0)
            //if (sd.data == jstring(len,'0'))
                continue; // EPCNUM all zeros - invalid
            data += sd.epclen;
            sd.type = MEM_EPC;
            sd.RSSI = *data++;
            sd.count = *data++;
            v[j] = sd;
            //printtag(j);
        }
    }
    cprintf("Inventory Finished in = %ldms\n", stopwatch(false));
    return tagcount;
}

void J4210U::printtag(int index) {
    if (index >= scansize_) {
        cprintf("Index Out of Range. Scan size_ = %d\n", scansize_);
        return;
    }
    ScanData sd = scan_[index];
    char epcnum[sd.epclen*2];
    bytes2hex(sd.EPCNUM, sd.epclen, epcnum);
    cprintf("[% 2d] EPCNUM: % 24s | ANT: %d | RSSI= % 3d | COUNT: %d\n", index, epcnum, sd.ant, (int)sd.RSSI, sd.count, sd.type);
}

bool J4210U::Auth(unsigned char* password, unsigned char size) {
    if (password) {
        if (size < 4) {
            error_ = "Password length must be at lease 8 bytes.";
            return false;
        }
        memcpy(password_, password, size);
        //password_ = jstring((char*)password, 4);
        return true;
    }
    //password_ = jstring(4,0);
    memset(password_, 0, sizeof(password_));
    return true;
}

bool J4210U::Write(unsigned char* epcnum, unsigned char epcLen, unsigned char* data, int wsize, int windex, int memtype) {
    //cout << "Password: " << tohex(password_) << endl;
    if (data == 0) {
        error_ = "No writable data provided.";
        return false;
    }
    if (epcLen > 15) {
        char str[100];
        sprintf(str,"EPCNUM length is too long : %d.",epcLen);
        error_ = str;
        return false;
    }
//    if (password_.size() < 4) {
//        error_ = "Password is not set or does not match.";
//        return false;
//    }

    char* password = (char*)password_;
//    unsigned char command[] = {0x05,0x00,0x03,
//        (unsigned char)(size/2), epcLen/2,
//        password[3], password[2], password[1], password[0]};
//    unsigned char cc[sizeof(command) + epcLen + 2];
//    jstring command;
    unsigned char command[64];
    int i = 0;

    command[i++] = 0x00;
    command[i++] = 0x00;
    command[i++] = 0x03;
    command[i++] = wsize;
    command[i++] = (char)epcLen/2;
    for(int j=0;j<epcLen;j++) {
        command[i++] = epcnum[j];
    }
    command[i++] = memtype;

//    command.push_back(0x00); // number of bytes following this byte
//    command.push_back(0x00); // address - always zero
//    command.push_back(0x03); // command
//    command.push_back(wsize); // number of words to be written
//    command.push_back((char)epcLen/2); // number of words in epcnum
//    command += jstring((char*)epcnum, epcLen); // epcnum data
//    command.push_back(memtype); // memory type to be written
    if (memtype == MEM_EPC) {
        command[i++] = 2+windex;
        //command.push_back(2+windex); // offset is 2 for EPCNUM memory
    } else {
        command[i++] = windex;
        //command.push_back(windex); // no offset for other than EPCNUM memory
    }
    for(int j=0;j<wsize*2;j++) {
        command[i++] = data[j];
    }
//    command += jstring((char*)data, wsize*2); // data to be written
    command[i++] = password[3];
    command[i++] = password[2];
    command[i++] = password[1];
    command[i++] = password[0];

//    command.push_back(password[0]); // password lsb
//    command.push_back(password[1]); // password
//    command.push_back(password[2]); // password
//    command.push_back(password[3]); // password msb
    command[i++] = 0x00;
    command[i++] = 0x00;
//    command.push_back(0); // crc place holder
//    command.push_back(0); // crc place holder

    //cout << "Command = " << tohex(command) << endl;

//    unsigned char response[32];
    int retries = 100;
    do {
        unsigned char response[6];
        int n = transfer((unsigned char*)command, i, response, sizeof(response), 0, 50);
        //printarr((unsigned char*)command.data(),command.size());
        //n = transfer((unsigned char*)command.data(), command.size(), response, sizeof(response), delay);
        if (n==0) {
            error_ = "FAILED to send write request.";
            return false;
        }
        char result = (response[3] == 0);
        result &= (response[0] == command[0]);
        //if (result == 0 || result == -3)
        if (result == 1) {
            return true;
        }
    } while(retries--);
    error_ = "FAILED to write.";
    return false;
}

void J4210U::LibVersion(unsigned char *version) {
    version[0] = (LIB_VERSION >> 8) & 0x00FF;
    version[1] = LIB_VERSION & 0x00FF;
}

bool J4210U::WriteWord(unsigned char* epcnum, unsigned char epcLen, unsigned char* data, unsigned char windex, int memtype) {
//    do {
        bool success = Write(epcnum, epcLen, data, 1, windex, memtype);
        if (memtype == MEM_EPC)
            return success;
        if (success) {
            unsigned char data2[2] = {0,0};
            success = ReadWord(epcnum, epcLen, data2, windex, memtype);
            if (success) {
                if (data[0] == data2[0] && data[1] == data2[1])
                    return true; // data successfully written.
            }
        }
//    } while(retries--);
    return false;
}

bool J4210U::Read(unsigned char* epcnum, unsigned epclen, unsigned char* data, int wnum, int windex, int memtype) {
    //cout << "Password: " << tohex(password_) << endl;
    if (data == 0) {
        error_ = "No writable data provided. ";
        return false;
    }
    if (epclen > 15) {
        char str[100];
        sprintf(str,"EPCNUM length is too long : %d. ",epclen);
        error_ = str;
        return false;
    }
//    if (password_.size() < 4) {
//        error_ = "Password is not set or does not match. ";
//        return false;
//    }

    char* password = (char*)password_;
//    unsigned char command[] = {0x05,0x00,0x03,
//        (unsigned char)(size/2), epcLen/2,
//        password[3], password[2], password[1], password[0]};
//    unsigned char cc[sizeof(command) + epcLen + 2];
    //jstring command;
    unsigned char command[64];
    int i = 0;

    if (memtype == MEM_EPC) {
        windex += 2;
    }
    command[i++] = 0x00;
    command[i++] = 0x00;
    command[i++] = 0x02;
    command[i++] = (char)epclen/2;
    for(int j=0;j<epclen;j++) {
        command[i++] = epcnum[j];
    }
    command[i++] = memtype;
    command[i++] = windex;
    command[i++] = wnum;
    command[i++] = password[3];
    command[i++] = password[2];
    command[i++] = password[1];
    command[i++] = password[0];
    command[i++] = 0x00;
    command[i++] = 0x00;

//    command.push_back(0x00);
//    command.push_back(0x00);
//    command.push_back(0x02);
//    command.push_back((char)epclen/2);
//    command += jstring((char*)epcnum, epclen);
//    command.push_back(memtype);
//    command.push_back(windex);
//    command.push_back(wnum);
//    command.push_back(password[3]);
//    command.push_back(password[2]);
//    command.push_back(password[1]);
//    command.push_back(password[0]);
//    command.push_back(0); // crc place holder
//    command.push_back(0); // crc place holder

    //cout << "Command = " << tohex(command) << ", Size = " << command.size() << endl;
    unsigned char response[32];
    int result = -100;
    int retries = 100;
    do {
        int n = transfer((unsigned char*)command, i, response, sizeof(response), 1, 100);
        //printarr((unsigned char*)command.data(),command.size());
        if (n==0) {
            error_ = "FAILED to send write request. ";
            return false;
        }
        result = response[3];
        if (result == 0) { // || result == -5) {
            memcpy(data, &response[4], wnum*2);
            return true;
        }
    } while(retries--);

    error_ = "FAILED to read.";
    return false;
}

bool J4210U::ReadWord(unsigned char* epcnum, unsigned char epclen, unsigned char* data, int windex, int memtype) {
    return Read(epcnum, epclen, data, 1, windex, memtype);
}

/**
 * filterDesc = [~][-][.]aa>>[mm=]value[=nn]<<bb[~][-][.]
 *   if aa: is provided, then the value is right shifted this many bits.
 *   if :bb is provided, then the value is left shifted this many bits.
 *   both aa and bb cannot be specified together.
 *   if neither aa or bb is provided, then value or value is used as prefix.
 *   if *value is provided, then value is used as suffix.
 *   if ---value, where each - indicate 4 bit nibble.
 *   if value---, where each - indicate 4 bit nibble.
 *   value... where each . indicate a bit.
 *   ...value where each . indicate a bit.
 *   ~~~value where each ~ indicate a byte.
 *   value~~~ where each ~ indicate a byte.
 */
bool J4210U::CreateFilter(char* filterDesc) {
    return false;
}

bool J4210U::SetFilter(int maskAdrInByte, int maskLenInByte, unsigned char *maskDataByte) {
    if (maskAdrInByte < 0 || maskAdrInByte > 12) {
        error_ = "Mask starts at an illegal offset.";
        return false;
    }
    filter_.MaskAdr = maskAdrInByte;
    if (maskLenInByte > (12 - maskAdrInByte)) {
        error_ = "Mask length is greater than EPCNUM length.";
        return false;
    }
    for(int j=0;j<maskLenInByte;j++) {
        filter_.MaskData[j] = maskDataByte[j];
    }
    filter_.MaskLen = maskLenInByte;
//    filter_.MaskData = jstring((char*)maskDataByte, maskLenInByte);
//    jstring hex = tohex(filter_.MaskData);
//    cprintf("MASK: MaskAdr = %d, MaskLen = %d, MaskData = %s\n",
//           maskAdrInByte,
//           filter_.MaskData.size(),
//           hex.c_str());
    return true;
}

bool J4210U::WriteMemWord(unsigned char *epcnum, unsigned char epclen, unsigned char *data, unsigned char windex) {
    bool b = WriteWord(epcnum, epclen, data, windex, MEM_USER);
    if (!b) {
        unsigned char data2[2] = {0,0};
        //printarr(epcnum,epcLen);
        //printarr(epc2,epcLen);
        bool b = ReadMemWord(epcnum,epclen,data2,windex);
        //printarr(data,2);
        //printarr((unsigned char*)data2,2);
        if (b) {
            if (data[0] == data2[0] && data[1] == data2[1]) {
                return true;
            }
        }
    }

    return b;
}

bool J4210U::WriteEpcWord(unsigned char *epcnum, unsigned char epclen, unsigned char *data, unsigned char windex) {
    bool b = WriteWord(epcnum, epclen, data, windex, MEM_EPC);
    if (!b) {
        unsigned char epc2[32];
        memcpy(epc2, epcnum, epclen);
        epc2[windex*2] = data[0];
        epc2[windex*2+1] = data[1];
        unsigned char data2[2] = {0,0};
        //printarr(epcnum,epcLen);
        //printarr(epc2,epcLen);
        bool b = ReadEpcWord(epc2,epclen,data2,windex,MEM_EPC);
        //printarr(data,2);
        //printarr((unsigned char*)data2,2);
        if (b) {
            if (data[0] == data2[0] && data[1] == data2[1]) {
                return true;
            }
        }
    }
    return b;
}

bool J4210U::ReadMemWord(unsigned char *epcnum, unsigned char epclen, unsigned char *data, unsigned char windex) {
    return ReadWord(epcnum, epclen, data, windex, MEM_USER);
}

bool J4210U::ReadEpcWord(unsigned char* epcnum, unsigned char epclen, unsigned char* data, int windex, int memtype) {
    return Read(epcnum, epclen, data, 1, windex, MEM_EPC);
}

bool J4210U::TagExists(unsigned char* epcnum, unsigned char epclen) {
    Filter f = filter_; // backup filter
    bool success = SetFilter(0, epclen/2, epcnum);
    if (!success) {
        filter_ = f;
        return false;
    }
    int n = Inventory(true);
    filter_ = f;
    return n > 0;
}

bool J4210U::SetEPC(unsigned char *epcnum, unsigned char epclen, unsigned char *newepc) {
#if 0
    jstring command;

    command.push_back(0x00);
    command.push_back(0x00);
    command.push_back(0x04);
    command.push_back((char)epclen/2);

    char* password = (char*)password_.EPCNUM();

    command.push_back(password[3]);
    command.push_back(password[2]);
    command.push_back(password[1]);
    command.push_back(password[0]);

    command += string((char*)newepc, epclen);

    command.push_back(0); // crc place holder
    command.push_back(0); // crc place holder

    unsigned char response[6];
    int retries = 0;
    while(retries++ < 5) {
        cprintf("Try %d\n", retries);
        int n = transfer((unsigned char*)command.EPCNUM(), command.size(), response, sizeof(response), 100);
        if (n==0)
            return false;
        if (response[3] == 0) {
            return true;
        }
    }

    return false;
#endif

    if (epclen & 0x01) {
        error_ = "FAILED to get EPCNUM. EPCNUM length is not EVEN.";
        return false;
    }
    //return Write(epcnum, epclen, newepc, 2, 1, MEM_EPC, 20);

    unsigned char epccopy[epclen];
    memcpy(epccopy, epcnum, epclen);
    for(int i=0;i<epclen;i+=2) {
        unsigned char windex = i>>1;
        unsigned char* data = newepc + i;
        bool success = WriteWord(epccopy, epclen, data, windex, MEM_EPC);
        if (success == 0) {
            char str[10] = "";
            sprintf(str, "%d", (int)windex);
            error_ = "FAILED to change entire EPCNUM. Could not Modified word at index: ";
            error_ += str;
            return false;
        }
        memcpy(epccopy + i, data, 2);
        char z[64];
        bytes2hex(epccopy, 12, z);
        cprintf("EPCNUM Copy = %s\n", z);
        platform_sleep(50);
    }

    return true;
}

bool J4210U::Kill() {
    return false;
}

bool J4210U::Lock() {
    return false;
}

bool J4210U::Erase() {
    return false;
}

bool J4210U::Is496Bits() {
    unsigned char command[] = {0x05,0x00,0x71, 0, 0};
    unsigned char response[7];
    int n = transfer(command, sizeof(command), response, sizeof(response), 100, 0);
    if (n==0) {
        error_ = "FAILED to send write request.";
        return false;
    }
    if (response[3] == 0) {
        return (response[4] != 0);
    }
    error_ = "FAILED to get bit info.";
    return false;
}

bool J4210U::Set496Bits(bool bits496) {
    unsigned char command[] = {0x05,0x00,0x70, bits496, 0, 0};
    unsigned char response[6];
    int n = transfer(command, sizeof(command), response, sizeof(response), 5, 0);
    if (n==0) {
        error_ = "FAILED to send write request.";
        return false;
    }
    if (response[3] == 0)
        return true;
    return false;
}

bool J4210U::SetPassword(unsigned char* epcnum, unsigned char epcLen, unsigned char* pass, unsigned char size) {
    if (size != 4) {
        error_ = "Password must be 4 bytes long.";
        return false;
    }

//    return WriteWord((unsigned char*)epcnum,epcLen,pass,3,MEM_PASSWORD);
    return Write((unsigned char*)epcnum, epcLen, pass, size/2, 2, MEM_PASSWORD);
}

bool J4210U::SetKillPassword(unsigned char* epcnum, unsigned char epcLen, unsigned char* pass, unsigned char size) {
    return Write((unsigned char*)epcnum, epcLen, pass, size/2, 0, MEM_PASSWORD);
}

bool J4210U::GetKillPassword(unsigned char* epcnum, int epclen, unsigned char* pass, unsigned char *passlen) {
    *passlen = 8;
    return Read((unsigned char*)epcnum, epclen, pass, 4, 0, MEM_PASSWORD);
}

bool J4210U::GetTID(unsigned char *epcnum, unsigned char epclen, unsigned char *tid, unsigned char *tidlen) {
    *tidlen = epclen;
    int retries = 100;
    bool success;
    error_ = "";
    do {
        cprintf("Retry: %d\n", retries);
        success = Read(epcnum, epclen, tid, epclen/2, 0, MEM_TID);
        if (success) {
            cprintf("Success.\n");
            return true;
            if (tid[0] == 0 && tid[1] == 0 && tid[2] == 0 && tid[3] == 0) {
                continue;
            }
        }
        if (!success) {
            error_ += " Failed to getTID. ";
        } else {
            return success;
        }
    } while(retries--);
    //printarr((unsigned char*)tid, 12);
    // get tag type
    return success;
}

bool J4210U::SetGPO(unsigned char gpoNumber) {
    // gpoNumber should be either 1 for GPO1 or 2 for GPO2
    // if gpoNumber == 3, then both output will be set
    unsigned char command[] = {0x05,0x00,0x46, gpoNumber, 0, 0};
    unsigned char response[6];
    int n = transfer(command, sizeof(command), response, sizeof(response), 100, 0);
    if (n==0) {
        error_ = "FAILED to send write request.";
        return false;
    }
    if (response[3] == 0)
        return true;
    return false;
}

char J4210U::GetGPI(unsigned char gpiNumber) {
    // gpiNumber should be either 1 for GP1 or 2 for GPi2
    // if gpiNumber == 3, state is invalid.
    // if gpiNumber = 4 or 5, then GPO1 and GPO2 values will be returned respectively.
    unsigned char command[] = {0x05,0x00,0x47, 0, 0};
    unsigned char response[7];
    int retries = 10;
    do {
        int n = transfer(command, sizeof(command), response, sizeof(response), 0, 0);
        //printarr(response, 7);
        if (n==0) {
            error_ = "FAILED to send write request.";
            return -1;
        }
        if (response[3] == 0) {
            unsigned char output = response[4] & gpiNumber;
            output >>= (gpiNumber - 1);
            //cprintf("in=%d\n",output);
            return output == 0;
        }
    } while(retries--);
    return -1;
}

void J4210U::LastError(char * buffer) {
    strcpy(buffer, error_.c_str());
}

int J4210U::GetTagInfo(unsigned char *tid, TagInfo *info) {
    unsigned short mfg = (tid[0] << 8) | tid[1];
    unsigned short chip = (tid[2] << 8) | tid[3];
//    unsigned short model = (tid[4] << 8) | tid[5];

    info->pwdlen = 4;
    info->epclen = 12;
    info->tidlen = 12;
    info->userlen = 4;
    info->type = UNKNOWN;

    //cprintf("%04X, %04X\n", mfg, chip);
    //mfg &= 0xFF00;
    switch(mfg) {
    case 0xE280: {
        switch(chip) {
        case 0x1130:
            strcpy(info->chip,"MONZA_5");
            info->type = MONZA_5;
            memcpy(info->tid, tid, info->tidlen);
            return MONZA_5;
        case 0x1105:
            strcpy(info->chip,"MONZA_4QT");
            info->type = MONZA_4QT;
            memcpy(info->tid, tid, info->tidlen);
            return MONZA_4QT;
        case 0x110C:
            strcpy(info->chip,"MONZA_4E");
            info->type = MONZA_4E;
            memcpy(info->tid, tid, info->tidlen);
            return MONZA_4E;
        case 0x1100:
            strcpy(info->chip,"MONZA_4D");
            info->type = MONZA_4D;
            memcpy(info->tid, tid, info->tidlen);
            return MONZA_4D;
        case 0x1114:
            strcpy(info->chip,"MONZA_4I");
            info->type = MONZA_4I;
            memcpy(info->tid, tid, info->tidlen);
            return MONZA_4I;
        case 0x1160:
            strcpy(info->chip,"MONZA_R6");
            info->type = MONZA_R6;
            memcpy(info->tid, tid, info->tidlen);
            info->userlen = 0;
            return MONZA_R6;
        case 0x1170:
            strcpy(info->chip,"MONZA_R6P");
            info->type = MONZA_R6P;
            memcpy(info->tid, tid, info->tidlen);
            info->userlen = 4;
            return MONZA_R6P;
        case 0x1150:
            strcpy(info->chip,"MONZA_X8K");
            info->type = MONZA_X8K;
            memcpy(info->tid, tid, info->tidlen);
            info->userlen = 8192;
            return MONZA_X8K;
        case 0x1140:
            strcpy(info->chip,"MONZA_X2K");
            info->type = MONZA_X2K;
            memcpy(info->tid, tid, info->tidlen);
            info->userlen = 0;
            info->pwdlen = 8;
            info->epclen = 12;
            return MONZA_X2K;
        case 0x1191:
            strcpy(info->chip,"IMPINJ_M730");
            info->type = IMPINJ_M730;
            memcpy(info->tid, tid, info->tidlen);
            info->userlen = 0;
            info->pwdlen = 8;
            return IMPINJ_M730;
        case 0x1190:
            strcpy(info->chip,"IMPINJ_M750");
            info->type = IMPINJ_M750;
            memcpy(info->tid, tid, info->tidlen);
            info->userlen = 4;
            info->pwdlen = 8;
            return IMPINJ_M750;
        break;

        }
    }
    case 0xE200: {
        switch(chip) {
        case 0x3412: // TEST PASS
            strcpy(info->chip,"HIGGS_3");
            info->type = HIGGS_3;
            memcpy(info->tid, tid, info->tidlen);
            info->userlen = 64;
            return HIGGS_3;
        case 0x3414:
            strcpy(info->chip,"HIGGS_4");
            info->type = HIGGS_4;
            memcpy(info->tid, tid, info->tidlen);
            return HIGGS_4;
        case 0x3415:
            strcpy(info->chip,"HIGGS_9");
            info->type = HIGGS_9;
            memcpy(info->tid, tid, info->tidlen);
            return HIGGS_9;
//        case 0x3416:
//            strcpy(info->chip,"HIGGS_EC");
//            info->type = HIGGS_EC;
//            memcpy(info->tid, tid, info->tidlen);
//            info->userlen = 16;
//            info->tidlen = 16;
//            return HIGGS_EC;
        case 0x3811:
            strcpy(info->chip,"HIGGS_EC");
            info->type = HIGGS_EC;
            memcpy(info->tid, tid, info->tidlen);
            info->userlen = 16;
            info->tidlen = 16;
            return HIGGS_EC;
        }

        unsigned short m = chip & 0x0FFF;
        switch(m) {
        case 0x0890:
            strcpy(info->chip,"UCODE_7");
            info->type = UCODE_7;
            memcpy(info->tid, tid, info->tidlen);
            info->userlen = 4;
            return UCODE_7;
        case 0x0894:
            strcpy(info->chip,"UCODE_8");
            info->type = UCODE_8;
            memcpy(info->tid, tid, info->tidlen);
            info->userlen = 0;
            return UCODE_8;
        case 0x0994:
            strcpy(info->chip,"UCODE_8M");
            info->type = UCODE_8M;
            memcpy(info->tid, tid, info->tidlen);
            info->userlen = 4;
            return UCODE_8M;
        }
        break;
    }

    case 0xE2C0: {
        switch(chip) {
        case 0x6F92:
            strcpy(info->chip,"UCODE_DNA");
            info->type = UCODE_DNA;
            memcpy(info->tid, tid, info->tidlen);
            info->userlen = 3072/8;
            info->epclen = 224/8;
            return UCODE_DNA;
        case 0x6F93: // this code is incorrect
            strcpy(info->chip,"UCODE_DNA_CITY");
            info->type = UCODE_DNA_CITY;
            memcpy(info->tid, tid, info->tidlen);
            info->userlen = 1024/8;
            info->epclen = 224/8;
            return UCODE_DNA_CITY;
        case 0x6F94: // this code is incorrect
            strcpy(info->chip,"UCODE_DNA_TRACK");
            info->type = UCODE_DNA_TRACK;
            memcpy(info->tid, tid, info->tidlen);
            info->userlen = 256/8;
            info->epclen = 448/8;
            return UCODE_DNA_TRACK;
        }
    }
    }

    strcpy(info->chip,"UNKNOWN");
    memcpy(info->tid, tid, info->tidlen);
    return UNKNOWN;
}

bool J4210U::SetQ(unsigned char q) {
    if (q_ > 15)
        return 1;
    q_ = q;
    return 0;
}

bool J4210U::SetSession(unsigned char sess) {
    if (session_ > 3)
        return 1;
    session_ = sess;
    return 0;
}
