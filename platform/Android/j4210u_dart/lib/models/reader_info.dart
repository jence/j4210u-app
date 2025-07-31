class ReaderInfo {
  int serial = 0;
  List<int> versionInfo = [0, 0];
  int antenna = 0;
  
  int comAdr = 0;
  int readerType = 0;
  int protocol = 0;
  String band = '';
  int power = 0;
  int scanTime = 0;
  int beepOn = 0;
  int reserved1 = 0;
  int reserved2 = 0;
  
  int maxFreq = 0;
  int minFreq = 0;
  int baudRate = 0;
  
  @override
  String toString() {
    return 'Version: ${versionInfo[0]}.${versionInfo[1]}, '
           'Serial: $serial, '
           'Type: $readerType, '
           'Protocol: 0x${protocol.toRadixString(16)}, '
           'Band: $band, '
           'Power: ${power}dB, '
           'ScanTime: ${scanTime * 100}ms, '
           'MinFreq: ${minFreq / 1000.0}MHz, '
           'MaxFreq: ${maxFreq / 1000.0}MHz, '
           'BaudRate: $baudRate';
  }
}

class ScanData {
  int type = 0;
  int ant = 0;
  int rssi = 0;
  int count = 0;
  String data = '';
  
  @override
  String toString() {
    return 'Ant: $ant, RSSI: $rssi, Count: $count, EPC: $data';
  }
}