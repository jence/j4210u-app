import 'dart:typed_data';
import 'dart:async';

class DeviceDetector {
  /// Test if device is MU910 by sending RFM_MODULE_INIT command
  static Future<bool> is910(Future<Uint8List> Function(Uint8List, {int timeoutMs, int pause}) transferFunction) async {
    try {
      // Empty serial buffer first (like in your C code)
      await Future.delayed(const Duration(milliseconds: 100));
      
      // Command from your C code: {0xCF, 0xFF, 0x00, 0x50, 0x00, 0x00, 0x00}
      // But we need to calculate CRC properly
      final commandData = [0xCF, 0xFF, 0x00, 0x50, 0x00];
      final crc = _generateMU910CRC(Uint8List.fromList(commandData), commandData.length);
      
      final command = Uint8List.fromList([
        ...commandData,
        (crc >> 8) & 0xFF, // CRC high byte first in MU910
        crc & 0xFF,        // CRC low byte
      ]);
      
      int retry = 3;
      
      while (retry > 0) {
        try {
          final response = await transferFunction(command, timeoutMs: 500, pause: 50);
          
          // Check for valid MU910 response
          if (response.isNotEmpty && response.length > 5 && 
              response[0] == 0xCF && response[1] == 0xFF && response[5] == 0x00) {
            return true;
          }
        } catch (e) {
          // Continue to next retry
        }
        retry--;
        await Future.delayed(const Duration(milliseconds: 100));
      }
      
      return false;
    } catch (e) {
      return false;
    }
  }
  
  /// Generate CRC for MU910 commands (exactly like your C code)
  static int _generateMU910CRC(Uint8List data, int length) {
    const int PRESET_VALUE = 0xFFFF;
    const int POLYNOMIAL = 0x8408;
    
    int crc = PRESET_VALUE;
    
    for (int i = 0; i < length; i++) {
      crc = (crc ^ (data[i] & 0xFF)) & 0xFFFF;
      for (int j = 0; j < 8; j++) {
        if (crc & 0x0001 != 0) {
          crc = ((crc >> 1) ^ POLYNOMIAL) & 0xFFFF;
        } else {
          crc = (crc >> 1) & 0xFFFF;
        }
      }
    }
    
    return crc;
  }
}