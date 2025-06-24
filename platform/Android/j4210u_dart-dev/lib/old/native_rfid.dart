// Create a new file: lib/native_rfid.dart
import 'package:flutter/services.dart';

class NativeRFID {
  static const platform = MethodChannel('com.example.rfid/native');
  
  static Future<bool> connect(String devicePath, int baudRate) async {
    try {
      final result = await platform.invokeMethod('connect', {
        'devicePath': devicePath,
        'baudRate': baudRate,
      });
      return result as bool;
    } catch (e) {
      print('Connect error: $e');
      return false;
    }
  }
  
  static Future<void> disconnect() async {
    try {
      await platform.invokeMethod('disconnect');
    } catch (e) {
      print('Disconnect error: $e');
    }
  }
  
  static Future<List<String>> scanTags() async {
    try {
      final result = await platform.invokeMethod('scanTags');
      return List<String>.from(result);
    } catch (e) {
      print('Scan error: $e');
      return [];
    }
  }
  
  static Future<String> getSettings() async {
    try {
      final result = await platform.invokeMethod('getSettings');
      return result as String;
    } catch (e) {
      print('Get settings error: $e');
      return 'Error: $e';
    }
  }
}