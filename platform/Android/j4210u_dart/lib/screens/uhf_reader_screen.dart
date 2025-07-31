import 'package:flutter/material.dart';
import '../models/tag_data.dart';
import '../utils/helpers.dart';
import '../widgets/tag_item_card.dart';
import 'dart:async';
import 'dart:convert';
import '../services/bluetooth_service.dart';

class UHFReaderScreen extends StatefulWidget {
  final BluetoothService bluetoothService;
  final String deviceName;

  const UHFReaderScreen({
    Key? key,
    required this.bluetoothService,
    required this.deviceName,
  }) : super(key: key);

  @override
  _UHFReaderScreenState createState() => _UHFReaderScreenState();
}

class _UHFReaderScreenState extends State<UHFReaderScreen> {
  List<TagData> _tags = [];
  bool _isListening = true;
  StreamSubscription<String>? _dataSubscription;
  String _lastRawData = '';
  String _connectionStatus = 'Connected';
  Timer? _connectionCheckTimer;
  int _successfulUpdates = 0;
  int _failedUpdates = 0;

  //New class member added

  String _buffer = '';
  int _packetCount = 0;
  DateTime? _lastArrayReceived;

@override
void initState() {
  super.initState();
  print('🚀 [INIT] UHF Reader Screen initializing');
  print('📱 [DEVICE] Connected to: ${widget.deviceName}');
  print('🔗 [STATUS] Initial connection status: ${widget.bluetoothService.isConnected}');
  _resetBuffer();
  _setupDataListener();
  _startConnectionCheck();
}

void _resetBuffer() {
  _buffer = '';
  _packetCount = 0;
  _lastArrayReceived = null;
  print('🔄 [BUFFER_RESET] Buffer and counters reset');
}

// Update the clear tags method
void _clearTags() {
  setState(() {
    _tags.clear();
    _successfulUpdates = 0;
    _failedUpdates = 0;
  });
  _resetBuffer(); // Also reset buffer
  print('🧹 [CLEAR] All tags cleared and buffer reset');
}

  void _setupDataListener() {
    print('🎧 [LISTENER] Setting up data listener');
    _dataSubscription = widget.bluetoothService.dataStream.listen(
      (data) {
        print('🔔 [LISTENER] Data received from stream: "$data"');
        _handleReceivedData(data);
      },
      onError: (error) {
        print('❌ [LISTENER] Stream error: $error');
        setState(() {
          _connectionStatus = 'Stream Error: $error';
        });
      },
      onDone: () {
        print('✅ [LISTENER] Stream closed');
        setState(() {
          _connectionStatus = 'Stream Closed';
        });
      },
    );
  }

  void _startConnectionCheck() {
    // Check connection status every 5 seconds
    _connectionCheckTimer = Timer.periodic(Duration(seconds: 5), (timer) {
      if (mounted) {
        bool currentlyConnected = widget.bluetoothService.isConnected;
        if (!currentlyConnected && _connectionStatus == 'Connected') {
          setState(() {
            _connectionStatus = 'Connection Lost';
          });
          print('🔴 [CONNECTION_CHECK] Connection lost detected');
        } else if (currentlyConnected && _connectionStatus != 'Connected') {
          setState(() {
            _connectionStatus = 'Connected';
          });
          print('🟢 [CONNECTION_CHECK] Connection restored');
        }
      }
    });
  }

void _handleReceivedData(String data) {
  print('📡 [DATA_RAW] Packet #${++_packetCount} - Received ${data.length} bytes');
  print('📡 [DATA_HEX] ${data.codeUnits.map((e) => e.toRadixString(16).padLeft(2, '0')).join(' ')}');
  print('📄 [DATA_UTF8] UTF-8 decoded: "$data"');
  
  // Always update last raw data for debugging
  if (mounted) {
    setState(() {
      _lastRawData = data;
    });
  }
  
  // Handle special messages
  if (data == 'DISCONNECTED') {
    print('🔴 [STATUS] Device disconnected');
    if (mounted) {
      setState(() {
        _connectionStatus = 'Disconnected';
      });
    }
    return;
  }
  
  if (data.startsWith('ERROR:')) {
    print('❌ [STATUS] Error received: $data');
    if (mounted) {
      setState(() {
        _connectionStatus = 'Error: ${data.substring(6)}';
        _failedUpdates++;
      });
    }
    return;
  }
  
  // Check if this is the start of a new array (button press)
  if (data.trim().startsWith('[')) {
    print('🆕 [NEW_ARRAY] New array detected - clearing buffer');
    _buffer = ''; // Clear previous buffer
    _packetCount = 1; // Reset packet count for new array
  }
  
  // Append new data to buffer
  _buffer += data;
  print('📦 [BUFFER] Current buffer length: ${_buffer.length}');
  print('📋 [BUFFER_CONTENT] "${_buffer.length > 200 ? _buffer.substring(0, 200) + '...' : _buffer}"');
  
  // Clean the buffer
  String cleanBuffer = _buffer.trim();
  cleanBuffer = cleanBuffer.replaceAll(RegExp(r'[\x00-\x08\x0B\x0C\x0E-\x1F\x7F]'), '');
  print('🧹 [BUFFER_CLEAN] Cleaned buffer length: ${cleanBuffer.length}');
  
  // Try to process if we have a complete array
  if (cleanBuffer.startsWith('[')) {
    print('🔍 [JSON_SEARCH] Found array start at beginning');
    
    // Check if we have a complete array
    if (_isCompleteJsonArray(cleanBuffer)) {
      print('✅ [JSON_COMPLETE] Complete array found!');
      _processNewArray(cleanBuffer);
      _buffer = ''; // Clear buffer after processing
    } else {
      print('⏳ [JSON_ARRAY] Incomplete JSON array, waiting for more data...');
    }
  } else {
    print('⚠️ [JSON] Data doesn\'t start with array bracket');
  }
  
  print('🔄 [BUFFER_RETURN] Returning buffer with ${_buffer.length} chars');
}

bool _isCompleteJsonArray(String buffer) {
  int bracketCount = 0;
  bool inString = false;
  bool escapeNext = false;
  
  for (int i = 0; i < buffer.length; i++) {
    String char = buffer[i];
    
    if (escapeNext) {
      escapeNext = false;
      continue;
    }
    
    if (char == '\\') {
      escapeNext = true;
      continue;
    }
    
    if (char == '"') {
      inString = !inString;
      continue;
    }
    
    if (!inString) {
      if (char == '[') {
        bracketCount++;
      } else if (char == ']') {
        bracketCount--;
        if (bracketCount == 0) {
          print('🔍 [BRACKET_SEARCH] Found matching ] at position $i');
          return true; // Found complete array
        }
      }
    }
  }
  
  print('🔍 [BRACKET_SEARCH] No matching ] found for [ at position 0 (final count: $bracketCount)');
  return false;
}

void _processNewArray(String jsonArrayString) {
  try {
    print('🆕 [NEW_ARRAY] Processing new complete array');
    List<dynamic> jsonArray = jsonDecode(jsonArrayString);
    print('✅ [NEW_ARRAY] Successfully parsed array with ${jsonArray.length} items');
    
    if (jsonArray.isEmpty) {
      print('⚠️ [NEW_ARRAY] Empty array received - clearing tags');
      if (mounted) {
        setState(() {
          _tags.clear();
          _successfulUpdates++;
          _lastArrayReceived = DateTime.now();
        });
      }
      return;
    }
    
    // Process all items in the array
    List<TagData> newTags = [];
    for (int i = 0; i < jsonArray.length; i++) {
      if (jsonArray[i] is Map<String, dynamic>) {
        print('🏷️ [TAG] Processing tag $i: ${jsonArray[i]}');
        TagData? tag = _createTagFromJson(jsonArray[i]);
        if (tag != null) {
          newTags.add(tag);
        }
      } else {
        print('⚠️ [TAG] Array item $i is not a valid object: ${jsonArray[i]}');
      }
    }
    
    if (mounted) {
      // REPLACE all existing tags with new array (clear previous data)
      setState(() {
        _tags = newTags; // Replace, don't append
        _successfulUpdates++;
        _lastArrayReceived = DateTime.now();
      });
      print('🔄 [ARRAY_REPLACE] REPLACED all tags with ${newTags.length} new tags from button press');
      print('📊 [TAG_LIST] New tags: ${_tags.map((t) => t.epc).take(5).join(", ")}${_tags.length > 5 ? "..." : ""}');
    }
    
  } catch (e) {
    print('💥 [NEW_ARRAY] Error processing new array: $e');
    if (mounted) {
      setState(() {
        _failedUpdates++;
      });
    }
  }
}

  void _processJsonArray(String jsonArrayString) {
    try {
      print('📋 [JSON_ARRAY] Processing JSON array');
      List<dynamic> jsonArray = jsonDecode(jsonArrayString);
      print('✅ [JSON_ARRAY] Successfully parsed array with ${jsonArray.length} items');
      
      if (jsonArray.isEmpty) {
        print('⚠️ [JSON_ARRAY] Empty array received');
        return;
      }
      
      // Process all items in the array
      List<TagData> newTags = [];
      for (int i = 0; i < jsonArray.length; i++) {
        if (jsonArray[i] is Map<String, dynamic>) {
          print('🏷️ [TAG] Processing tag $i: ${jsonArray[i]}');
          TagData? tag = _createTagFromJson(jsonArray[i]);
          if (tag != null) {
            newTags.add(tag);
          }
        } else {
          print('⚠️ [TAG] Array item $i is not a valid object: ${jsonArray[i]}');
        }
      }
      
      if (newTags.isNotEmpty && mounted) {
        // REPLACE all existing tags with new array
        setState(() {
          _tags = newTags;
          _successfulUpdates++;
        });
        print('🔄 [ARRAY_REPLACE] Replaced all tags with ${newTags.length} new tags from array');
      } else {
        print('⚠️ [ARRAY_REPLACE] No valid tags found in array');
      }
      
    } catch (e) {
      print('💥 [JSON_ARRAY] Error processing JSON array: $e');
      if (mounted) {
        setState(() {
          _failedUpdates++;
        });
      }
    }
  }

  void _processJsonObject(String jsonObjectString) {
    try {
      print('📄 [JSON_OBJECT] Processing single JSON object');
      Map<String, dynamic> jsonObject = jsonDecode(jsonObjectString);
      print('✅ [JSON_OBJECT] Successfully parsed single object');
      
      TagData? tag = _createTagFromJson(jsonObject);
      if (tag != null) {
        _addSingleTag(tag);
      }
      
    } catch (e) {
      print('💥 [JSON_OBJECT] Error processing JSON object: $e');
      if (mounted) {
        setState(() {
          _failedUpdates++;
        });
      }
    }
  }

  void _tryExtractJson(String data) {
    print('🔍 [EXTRACT] Trying to extract JSON from malformed data');
    
    // Try to find JSON patterns in the data
    RegExp jsonArrayPattern = RegExp(r'\[.*?\]', dotAll: true);
    RegExp jsonObjectPattern = RegExp(r'\{.*?\}', dotAll: true);
    
    // First try to find arrays
    Match? arrayMatch = jsonArrayPattern.firstMatch(data);
    if (arrayMatch != null) {
      String extractedArray = arrayMatch.group(0)!;
      print('🎯 [EXTRACT] Found potential JSON array: $extractedArray');
      _processNewArray(extractedArray);
      return;
    }
    
    // Then try to find objects
    Match? objectMatch = jsonObjectPattern.firstMatch(data);
    if (objectMatch != null) {
      String extractedObject = objectMatch.group(0)!;
      print('🎯 [EXTRACT] Found potential JSON object: $extractedObject');
      _processJsonObject(extractedObject);
      return;
    }
    
    print('❌ [EXTRACT] No valid JSON patterns found in data');
  }

TagData? _createTagFromJson(Map<String, dynamic> tagJson) {
  try {
    print('🏷️ [TAG_CREATE] Creating tag from JSON: $tagJson');
    
    // Check for EPC field (case-insensitive)
    String? epcValue;
    if (tagJson.containsKey('EPC') && tagJson['EPC'] != null) {
      epcValue = tagJson['EPC'].toString().trim();
    } else if (tagJson.containsKey('epc') && tagJson['epc'] != null) {
      epcValue = tagJson['epc'].toString().trim();
    }
    
    // Validate EPC field
    if (epcValue == null || epcValue.isEmpty) {
      print('⚠️ [TAG_SKIP] Missing or empty EPC field');
      print('🔍 [TAG_DEBUG] Available keys: ${tagJson.keys.toList()}');
      return null;
    }
    
    // Create a normalized JSON object with lowercase keys for TagData.fromJson
    Map<String, dynamic> normalizedJson = {
      'epc': epcValue,
      'rssi': tagJson['RSSI'] ?? tagJson['rssi'] ?? 0,
      'antenna': tagJson['ant'] ?? tagJson['antenna'] ?? 1,
      'count': tagJson['count'] ?? 1,
      'timestamp': tagJson['Timestamp'] ?? tagJson['timestamp'] ?? DateTime.now().toIso8601String(),
    };
    
    TagData tag = TagData.fromJson(normalizedJson);
    print('✅ [TAG_CREATED] Tag created - EPC: ${tag.epc}, RSSI: ${tag.rssi}, Antenna: ${tag.antenna}');
    
    return tag;
  } catch (e) {
    print('💥 [TAG_ERROR] Error creating tag: $e');
    print('📄 [TAG_ERROR] Tag JSON was: $tagJson');
    return null;
  }
}

  void _addSingleTag(TagData tag) {
    if (!mounted) return;
    
    // Check for duplicate EPC within last 2 seconds to avoid duplicates
    DateTime now = DateTime.now();
    bool isDuplicate = _tags.any((existingTag) => 
      existingTag.epc == tag.epc && 
      now.difference(existingTag.timestamp).inSeconds < 2
    );
    
    if (isDuplicate) {
      print('⏭️ [TAG_SKIP] Duplicate tag ignored: ${tag.epc}');
      return;
    }
    
    setState(() {
      _tags.insert(0, tag); // Add to beginning
      // Keep only last 200 tags to prevent memory issues
      if (_tags.length > 200) {
        _tags = _tags.take(200).toList();
      }
      _successfulUpdates++;
    });
    
    print('🎉 [TAG_ADDED] Tag added successfully! Total tags: ${_tags.length}');
    print('📊 [TAG_LIST] Current tags: ${_tags.map((t) => t.epc).take(3).join(", ")}${_tags.length > 3 ? "..." : ""}');
  }

  void _toggleListening() {
    setState(() {
      _isListening = !_isListening;
    });
    print('🔄 [LISTENING] Listening toggled to: $_isListening');
  }

  // void _clearTags() {
  //   setState(() {
  //     _tags.clear();
  //     _successfulUpdates = 0;
  //     _failedUpdates = 0;
  //   });
  //   print('🧹 [CLEAR] All tags cleared');
  // }

  void _reconnect() async {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('Please go back and reconnect from device list'),
        duration: Duration(seconds: 3),
      ),
    );
  }

  Future<bool> _onWillPop() async {
    bool? shouldPop = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Disconnect from ${widget.deviceName}?'),
        content: Text('Are you sure you want to disconnect from the UHF reader?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: Text('Cancel'),
          ),
          TextButton(
            onPressed: () => Navigator.of(context).pop(true),
            child: Text('Disconnect'),
          ),
        ],
      ),
    );
    
    return shouldPop ?? false;
  }

  @override
  Widget build(BuildContext context) {
    bool isConnected = widget.bluetoothService.isConnected && _connectionStatus == 'Connected';
    
    return WillPopScope(
      onWillPop: _onWillPop,
      child: Scaffold(
        appBar: AppBar(
          title: Text(widget.deviceName),
          actions: [
            IconButton(
              icon: Icon(Icons.clear),
              onPressed: _clearTags,
              tooltip: 'Clear Tags',
            ),
          ],
        ),
        body: Column(
          children: [
            // Connection Status
            Container(
              width: double.infinity,
              padding: EdgeInsets.all(16),
              color: isConnected ? Colors.green : Colors.red,
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          _connectionStatus,
                          style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
                        ),
                        if (_successfulUpdates > 0 || _failedUpdates > 0)
                          Text(
                            'Updates: $_successfulUpdates success, $_failedUpdates failed',
                            style: TextStyle(color: Colors.white70, fontSize: 12),
                          ),
                      ],
                    ),
                  ),
                  if (!isConnected)
                    ElevatedButton(
                      onPressed: _reconnect,
                      child: Text('Reconnect'),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.white,
                        foregroundColor: Colors.red,
                      ),
                    ),
                ],
              ),
            ),
            
            // Control Panel
            Container(
              padding: EdgeInsets.all(16),
              child: Row(
                children: [
                  Expanded(
                    child: Card(
                      child: Padding(
                        padding: EdgeInsets.all(16),
                        child: Column(
                          children: [
                            Icon(
                              Icons.nfc,
                              color: Colors.blue,
                              size: 32,
                            ),
                            SizedBox(height: 8),
                            Text(
                              'Total Tags',
                              style: TextStyle(fontWeight: FontWeight.bold),
                            ),
                            SizedBox(height: 8),
                            Text(
                              '${_tags.length}',
                              style: TextStyle(
                                fontSize: 24,
                                fontWeight: FontWeight.bold,
                                color: Colors.blue,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ),
            
            // Debug Info (Uncomment for debugging)
            // if (_lastRawData.isNotEmpty)
            //   Container(
            //     margin: EdgeInsets.symmetric(horizontal: 16),
            //     padding: EdgeInsets.all(12),
            //     decoration: BoxDecoration(
            //       color: Colors.grey[100],
            //       borderRadius: BorderRadius.circular(8),
            //       border: Border.all(color: Colors.grey[300]!),
            //     ),
            //     child: Column(
            //       crossAxisAlignment: CrossAxisAlignment.start,
            //       children: [
            //         Row(
            //           children: [
            //             Icon(Icons.bug_report, size: 16, color: Colors.orange),
            //             SizedBox(width: 8),
            //             Text(
            //               'Debug Info - Last Data Received:',
            //               style: TextStyle(fontWeight: FontWeight.bold, fontSize: 12),
            //             ),
            //           ],
            //         ),
            //         SizedBox(height: 8),
            //         Container(
            //           width: double.infinity,
            //           padding: EdgeInsets.all(8),
            //           decoration: BoxDecoration(
            //             color: Colors.black87,
            //             borderRadius: BorderRadius.circular(4),
            //           ),
            //           child: Text(
            //             _lastRawData,
            //             style: TextStyle(
            //               fontFamily: 'monospace',
            //               fontSize: 10,
            //               color: Colors.green,
            //             ),
            //             maxLines: 5,
            //             overflow: TextOverflow.ellipsis,
            //           ),
            //         ),
            //       ],
            //     ),
            //   ),
            
            // SizedBox(height: 16),
            
            // Tags List
            Expanded(
              child: _tags.isEmpty
                  ? Center(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(
                            Icons.nfc,
                            size: 64,
                            color: Colors.grey,
                          ),
                          SizedBox(height: 16),
                          Text(
                            isConnected 
                              ? 'Waiting for tags...\nPlace a tag near your UHF reader'
                              : 'Not connected to UHF reader',
                            textAlign: TextAlign.center,
                            style: TextStyle(fontSize: 16, color: Colors.grey),
                          ),
                          if (isConnected) ...[
                            SizedBox(height: 16),
                            Text(
                              'J4311UH will automatically send JSON data when tags are detected',
                              textAlign: TextAlign.center,
                              style: TextStyle(fontSize: 12, color: Colors.grey[600]),
                            ),
                            SizedBox(height: 8),
                            // Text(
                            //   'Expected format: [{"epc":"...","rssi":...,"antenna":...}]',
                            //   textAlign: TextAlign.center,
                            //   style: TextStyle(fontSize: 10, color: Colors.grey[500], fontFamily: 'monospace'),
                            // ),
                          ],
                        ],
                      ),
                    )
                  : ListView.builder(
                      itemCount: _tags.length,
                      itemBuilder: (context, index) {
                        return _isListening 
                          ? TagItemCard(
                              tag: _tags[index],
                              index: index,
                              onCopy: () {
                                Helpers.showSnackBar(
                                  context,
                                  'Tag ${index + 1} EPC copied to clipboard',
                                );
                              },
                            )
                          : Opacity(
                              opacity: 0.5,
                              child: TagItemCard(
                                tag: _tags[index],
                                index: index,
                                onCopy: () {},
                              ),
                            );
                      },
                    ),
            ),
          ],
        ),
        // floatingActionButton: FloatingActionButton(
        //   onPressed: _toggleListening,
        //   child: Icon(_isListening ? Icons.pause : Icons.play_arrow),
        //   backgroundColor: _isListening ? Colors.orange : Colors.green,
        //   tooltip: _isListening ? 'Pause Listening' : 'Resume Listening',
        // ),
      ),
    );
  }

  @override
  void dispose() {
    print('🗑️ [UHF_SCREEN] Disposing UHF Reader Screen');
    _connectionCheckTimer?.cancel();
    _dataSubscription?.cancel();
    super.dispose();
    // Note: Don't dispose the bluetooth service here as it might be used elsewhere
  }
}