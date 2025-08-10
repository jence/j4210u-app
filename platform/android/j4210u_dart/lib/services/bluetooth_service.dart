import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';
import 'package:flutter_bluetooth_serial/flutter_bluetooth_serial.dart';

class BluetoothService {
  BluetoothConnection? connection;
  StreamSubscription<Uint8List>? _streamSubscription;
  final StreamController<String> _dataController = StreamController<String>.broadcast();
  bool _isDisposed = false;
  Timer? _heartbeatTimer;
  int _dataPacketCount = 0;
  
  Stream<String> get dataStream => _dataController.stream;

  Future<List<BluetoothDevice>> getBondedDevices() async {
    return await FlutterBluetoothSerial.instance.getBondedDevices();
  }

  Future<bool> isBluetoothEnabled() async {
    return await FlutterBluetoothSerial.instance.isEnabled ?? false;
  }

  Future<void> enableBluetooth() async {
    await FlutterBluetoothSerial.instance.requestEnable();
  }

  Future<bool> connectToDevice(BluetoothDevice device) async {
    try {
      print('🔵 [BLUETOOTH] Attempting to connect to ${device.name} (${device.address})');
      
      // Ensure any previous connection is closed
      if (connection != null) {
        await connection!.close();
        connection = null;
      }
      
      // Cancel any existing stream subscription
      _streamSubscription?.cancel();
      _streamSubscription = null;
      
      // Cancel heartbeat timer
      _heartbeatTimer?.cancel();
      _heartbeatTimer = null;
      
      connection = await BluetoothConnection.toAddress(device.address);
      print('🟢 [BLUETOOTH] Connection established successfully');
      
      if (connection == null || !connection!.isConnected) {
        print('❌ [BLUETOOTH] Connection failed - not connected after establishment');
        return false;
      }
      
      // Reset packet counter
      _dataPacketCount = 0;
      
      String buffer = '';
      _streamSubscription = connection!.input!.listen(
        (Uint8List data) {
          // Check if service has been disposed
          if (_isDisposed) {
            print('⚠️ [BLUETOOTH] Service disposed, ignoring data');
            return;
          }
          
          _dataPacketCount++;
          
          try {
            print('📡 [DATA_RAW] Packet #${_dataPacketCount} - Received ${data.length} bytes');
            print('📡 [DATA_HEX] ${data.map((b) => b.toRadixString(16).padLeft(2, '0')).join(' ')}');
            
            // Use UTF-8 for better compatibility
            String receivedData = String.fromCharCodes(data);
            print('📄 [DATA_UTF8] UTF-8 decoded: "$receivedData"');
            
            buffer += receivedData;
            print('📦 [BUFFER] Current buffer length: ${buffer.length}');
            
            // Show buffer content (truncated if too long)
            String bufferPreview = buffer.length > 200 ? buffer.substring(0, 200) + '...' : buffer;
            print('📋 [BUFFER_CONTENT] "$bufferPreview"');
            
            // Process complete messages - look for complete JSON structures
            buffer = _processBuffer(buffer);
            
          } catch (e) {
            print('💥 [DATA_PROCESSING] Error processing received data: $e');
            // Don't close connection on data processing errors
          }
        },
        onDone: () {
          print('🔴 [BLUETOOTH] Connection closed by remote device');
          if (!_isDisposed) {
            _dataController.add('DISCONNECTED');
          }
        },
        onError: (error) {
          print('❌ [BLUETOOTH] Stream error: $error');
          // Don't immediately close on stream errors - try to recover
          if (!_isDisposed) {
            _dataController.add('ERROR: $error');
          }
        },
        cancelOnError: false, // Important: Don't cancel stream on errors
      );

      // Start heartbeat to monitor connection
      _startHeartbeat();

      print('✅ [BLUETOOTH] Connection setup complete, listening for data...');
      print('🔧 [CONNECTION] Connection details - isConnected: ${connection!.isConnected}');
      
      return connection!.isConnected;
    } catch (exception) {
      print('💥 [BLUETOOTH] Connection failed with exception: $exception');
      return false;
    }
  }

  void _startHeartbeat() {
    _heartbeatTimer = Timer.periodic(Duration(seconds: 10), (timer) {
      if (connection != null && connection!.isConnected && !_isDisposed) {
        print('💓 [HEARTBEAT] Connection alive - Packets received: ${_dataPacketCount}');
        
        // Send a simple command to ESP32 to check if it's responsive
        // You can uncomment this if your ESP32 supports commands
        // sendCommand('STATUS');
      } else {
        print('💔 [HEARTBEAT] Connection lost or disposed');
        timer.cancel();
      }
    });
  }

  String _processBuffer(String buffer) {
    String remainingBuffer = buffer;
    
    // Clean the buffer first - remove control characters but preserve structure
    remainingBuffer = remainingBuffer.replaceAll(RegExp(r'[\x00-\x08\x0B\x0C\x0E-\x1F\x7F]'), '');
    
    print('🧹 [BUFFER_CLEAN] Cleaned buffer length: ${remainingBuffer.length}');
    
    // Process multiple complete JSON structures in the buffer
    while (remainingBuffer.isNotEmpty) {
      remainingBuffer = remainingBuffer.trim();
      
      if (remainingBuffer.isEmpty) break;
      
      bool processedSomething = false;
      
      // Method 1: Look for complete JSON arrays [...]
      if (remainingBuffer.startsWith('[')) {
        print('🔍 [JSON_SEARCH] Found array start at beginning');
        int arrayEnd = _findMatchingBracket(remainingBuffer, 0, '[', ']');
        if (arrayEnd != -1) {
          String jsonArray = remainingBuffer.substring(0, arrayEnd + 1);
          print('🎯 [JSON_ARRAY] Found complete JSON array (${jsonArray.length} chars)');
          
          if (_validateAndSendJson(jsonArray)) {
            remainingBuffer = remainingBuffer.substring(arrayEnd + 1);
            processedSomething = true;
          } else {
            // Remove malformed array start and continue
            remainingBuffer = remainingBuffer.substring(1);
            processedSomething = true;
          }
        } else {
          print('⏳ [JSON_ARRAY] Incomplete JSON array, waiting for more data...');
          // Keep the buffer as is, waiting for more data
          break;
        }
      } 
      // Method 2: Look for complete JSON objects {...}
      else if (remainingBuffer.startsWith('{')) {
        print('🔍 [JSON_SEARCH] Found object start at beginning');
        int objectEnd = _findMatchingBracket(remainingBuffer, 0, '{', '}');
        if (objectEnd != -1) {
          String jsonObject = remainingBuffer.substring(0, objectEnd + 1);
          print('🎯 [JSON_OBJECT] Found complete JSON object (${jsonObject.length} chars)');
          
          if (_validateAndSendJson(jsonObject)) {
            remainingBuffer = remainingBuffer.substring(objectEnd + 1);
            processedSomething = true;
          } else {
            // Remove malformed object start and continue
            remainingBuffer = remainingBuffer.substring(1);
            processedSomething = true;
          }
        } else {
          print('⏳ [JSON_OBJECT] Incomplete JSON object, waiting for more data...');
          // Keep the buffer as is, waiting for more data
          break;
        }
      } else {
        // Remove leading garbage characters until we find a JSON start
        int nextJsonStart = -1;
        for (int i = 0; i < remainingBuffer.length; i++) {
          if (remainingBuffer[i] == '{' || remainingBuffer[i] == '[') {
            nextJsonStart = i;
            break;
          }
        }
        
        if (nextJsonStart > 0) {
          print('🗑️ [CLEANUP] Removing ${nextJsonStart} garbage characters');
          remainingBuffer = remainingBuffer.substring(nextJsonStart);
          processedSomething = true;
        } else if (nextJsonStart == -1) {
          print('🔍 [JSON_SEARCH] No JSON start markers found, clearing buffer');
          remainingBuffer = '';
          break;
        }
      }
      
      // Prevent infinite loops
      if (!processedSomething) {
        print('⚠️ [SAFETY] No progress made, removing first character to prevent infinite loop');
        remainingBuffer = remainingBuffer.length > 1 ? remainingBuffer.substring(1) : '';
      }
    }
    
    // Prevent buffer from growing too large
    if (remainingBuffer.length > 10000) {
      print('🚨 [BUFFER] Buffer too large (${remainingBuffer.length}), keeping only last 5000 chars');
      remainingBuffer = remainingBuffer.substring(remainingBuffer.length - 5000);
    }
    
    print('🔄 [BUFFER_RETURN] Returning buffer with ${remainingBuffer.length} chars');
    return remainingBuffer;
  }

  bool _validateAndSendJson(String jsonString) {
    try {
      // Validate JSON structure
      dynamic parsed = jsonDecode(jsonString);
      print('✅ [JSON_VALIDATE] Valid JSON parsed successfully');
      
      // Log the structure for debugging
      if (parsed is List) {
        print('📋 [JSON_STRUCTURE] Array with ${parsed.length} items');
        // Ensure all array items are objects with required fields
        for (int i = 0; i < parsed.length; i++) {
          if (parsed[i] is! Map<String, dynamic>) {
            print('❌ [JSON_VALIDATE] Array item $i is not an object');
            return false;
          }
        }
      } else if (parsed is Map<String, dynamic>) {
        print('📄 [JSON_STRUCTURE] Object with keys: ${parsed.keys.join(', ')}');
      } else {
        print('❌ [JSON_VALIDATE] JSON is neither array nor object');
        return false;
      }
      
      // Send valid JSON to UI
      if (!_isDisposed) {
        print('📤 [JSON_SEND] Sending valid JSON to UI');
        _dataController.add(jsonString);
      }
      
      return true;
    } catch (e) {
      print('🚫 [JSON_VALIDATE] Invalid JSON: $e');
      print('📄 [JSON_INVALID] Data was: "$jsonString"');
      return false;
    }
  }

  int _findMatchingBracket(String text, int startIndex, String openBracket, String closeBracket) {
    if (startIndex >= text.length || text[startIndex] != openBracket) {
      print('🚫 [BRACKET_MATCH] Invalid start position or character');
      return -1;
    }
    
    int bracketCount = 1;
    bool inString = false;
    bool escapeNext = false;
    
    for (int i = startIndex + 1; i < text.length; i++) {
      String char = text[i];
      
      // Handle escape sequences
      if (escapeNext) {
        escapeNext = false;
        continue;
      }
      
      if (char == '\\' && inString) {
        escapeNext = true;
        continue;
      }
      
      // Handle string boundaries
      if (char == '"') {
        inString = !inString;
        continue;
      }
      
      // Only count brackets when not inside strings
      if (!inString) {
        if (char == openBracket) {
          bracketCount++;
        } else if (char == closeBracket) {
          bracketCount--;
          if (bracketCount == 0) {
            print('✅ [BRACKET_MATCH] Found matching bracket at position $i');
            return i;
          }
        }
      }
    }
    
    print('🔍 [BRACKET_SEARCH] No matching $closeBracket found for $openBracket at position $startIndex (final count: $bracketCount)');
    return -1;
  }

  Future<void> sendCommand(String command) async {
    if (connection != null && connection!.isConnected && !_isDisposed) {
      try {
        connection!.output.add(Uint8List.fromList(utf8.encode(command + '\n')));
        await connection!.output.allSent;
        print('📤 [COMMAND] Sent: $command');
      } catch (e) {
        print('❌ [COMMAND] Failed to send command: $e');
      }
    } else {
      print('⚠️ [COMMAND] Cannot send command - not connected or disposed');
    }
  }

  void disconnect() {
    print('🔌 [DISCONNECT] Attempting to disconnect gracefully');
    
    // Cancel heartbeat
    _heartbeatTimer?.cancel();
    _heartbeatTimer = null;
    
    try {
      _streamSubscription?.cancel();
      _streamSubscription = null;
      print('✅ [DISCONNECT] Stream subscription cancelled');
    } catch (e) {
      print('⚠️ [DISCONNECT] Error cancelling subscription: $e');
    }
    
    try {
      connection?.close();
      connection?.dispose();
      print('✅ [DISCONNECT] Connection closed and disposed');
    } catch (e) {
      print('⚠️ [DISCONNECT] Error disposing connection: $e');
    }
    
    connection = null;
    print('🏁 [DISCONNECT] Disconnect complete');
  }

  bool get isConnected => connection?.isConnected ?? false;

  void dispose() {
    print('🗑️ [DISPOSE] Disposing BluetoothService');
    _isDisposed = true;
    disconnect();
    
    try {
      _dataController.close();
      print('✅ [DISPOSE] Data controller closed');
    } catch (e) {
      print('⚠️ [DISPOSE] Error closing data controller: $e');
    }
  }
}