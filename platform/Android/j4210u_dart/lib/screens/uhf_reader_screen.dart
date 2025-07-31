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

  @override
  void initState() {
    super.initState();
    print('🚀 [INIT] UHF Reader Screen initializing');
    print('📱 [DEVICE] Connected to: ${widget.deviceName}');
    print('🔗 [STATUS] Initial connection status: ${widget.bluetoothService.isConnected}');
    _setupDataListener();
    _startConnectionCheck();
  }

  void _setupDataListener() {
    print('🎧 [LISTENER] Setting up data listener');
    _dataSubscription = widget.bluetoothService.dataStream.listen((data) {
      print('🔔 [LISTENER] Data received from stream: "$data"');
      _handleReceivedData(data);
    });
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
    print('🔄 [HANDLER] Processing received data: "$data"');
    setState(() {
      _lastRawData = data;
    });
    
    // Handle special messages
    if (data == 'DISCONNECTED') {
      print('🔴 [STATUS] Device disconnected');
      setState(() {
        _connectionStatus = 'Disconnected';
      });
      return;
    }
    
    if (data.startsWith('ERROR:')) {
      print('❌ [STATUS] Error received: $data');
      setState(() {
        _connectionStatus = 'Error: ${data.substring(6)}';
      });
      return;
    }
    
    // Try to parse JSON data from ESP32
    try {
      // Clean the data - remove any extra characters and whitespace
      String cleanData = data.trim();
      print('🧹 [CLEAN] Cleaned data: "$cleanData"');
      
      // Remove any potential control characters
      cleanData = cleanData.replaceAll(RegExp(r'[\x00-\x1F\x7F]'), '');
      print('🔧 [SANITIZE] Sanitized data: "$cleanData"');
      
      // Check if it's a JSON array [{}] or single object {}
      if (cleanData.startsWith('[') && cleanData.endsWith(']')) {
        // Handle JSON array - REPLACE all existing tags with new array
        print('📋 [JSON] Detected JSON array format - REPLACING all existing tags');
        List<dynamic> jsonArray = jsonDecode(cleanData);
        print('✅ [JSON] Successfully parsed array with ${jsonArray.length} items');
        
        // Clear existing tags and process new array
        List<TagData> newTags = [];
        for (int i = 0; i < jsonArray.length; i++) {
          print('🏷️ [TAG] Processing tag $i: ${jsonArray[i]}');
          TagData? tag = _createTagFromJson(jsonArray[i]);
          if (tag != null) {
            newTags.add(tag);
          }
        }
        
        // Replace all tags with new array
        setState(() {
          _tags = newTags;
        });
        print('🔄 [ARRAY_REPLACE] Replaced all tags with ${newTags.length} new tags from array');
        
      } else if (cleanData.startsWith('{') && cleanData.endsWith('}')) {
        // Handle single JSON object - ADD to existing tags
        print('📄 [JSON] Detected single JSON object format - ADDING to existing tags');
        Map<String, dynamic> jsonObject = jsonDecode(cleanData);
        print('✅ [JSON] Successfully parsed single object');
        _processTagJson(jsonObject);
      } else {
        print('⚠️ [JSON] Data does not look like valid JSON: "$cleanData"');
        print('🔍 [DEBUG] First char: "${cleanData.isNotEmpty ? cleanData[0] : 'EMPTY'}"');
        print('🔍 [DEBUG] Last char: "${cleanData.isNotEmpty ? cleanData[cleanData.length - 1] : 'EMPTY'}"');
        print('🔍 [DEBUG] Length: ${cleanData.length}');
      }
    } catch (e) {
      print('💥 [JSON] Error parsing JSON data: $e');
      print('📄 [JSON] Raw data was: "$data"');
      
      // Show parsing error in debug info
      setState(() {
        _lastRawData = 'Parse Error: $e\nRaw: $data';
      });
    }
  }

  TagData? _createTagFromJson(Map<String, dynamic> tagJson) {
    try {
      print('🏷️ [TAG_CREATE] Creating tag from JSON: $tagJson');
      TagData tag = TagData.fromJson(tagJson);
      print('✅ [TAG_CREATED] Tag created - EPC: ${tag.epc}, RSSI: ${tag.rssi}, Antenna: ${tag.antenna}');
      
      if (tag.epc.isEmpty) {
        print('⚠️ [TAG_SKIP] Empty EPC, tag ignored');
        return null;
      }
      
      return tag;
    } catch (e) {
      print('💥 [TAG_ERROR] Error creating tag: $e');
      print('📄 [TAG_ERROR] Tag JSON was: $tagJson');
      return null;
    }
  }

  void _processTagJson(Map<String, dynamic> tagJson) {
    TagData? tag = _createTagFromJson(tagJson);
    if (tag == null) return;
    
    // Check for duplicate EPC within last 1 second to avoid duplicates
    DateTime now = DateTime.now();
    bool isDuplicate = _tags.any((existingTag) => 
      existingTag.epc == tag.epc && 
      now.difference(existingTag.timestamp).inSeconds < 1
    );
    
    if (isDuplicate) {
      print('⏭️ [TAG_SKIP] Duplicate tag ignored: ${tag.epc}');
      return;
    }
    
    setState(() {
      _tags.insert(0, tag);
      if (_tags.length > 200) _tags.removeLast(); // Keep only last 200 tags
    });
    print('🎉 [TAG_ADDED] Tag added successfully! Total tags: ${_tags.length}');
    print('📊 [TAG_LIST] Current tags: ${_tags.map((t) => t.epc).take(3).join(", ")}${_tags.length > 3 ? "..." : ""}');
  }

  void _toggleListening() {
    setState(() {
      _isListening = !_isListening;
    });
  }

  void _clearTags() {
    setState(() {
      _tags.clear();
    });
  }

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
                  Text(
                    _connectionStatus,
                    style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
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
            
            // Debug Info
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
            
            SizedBox(height: 16),
            
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
                              'ESP32 will automatically send JSON data when tags are detected',
                              textAlign: TextAlign.center,
                              style: TextStyle(fontSize: 12, color: Colors.grey[600]),
                            ),
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
      ),
    );
  }

  @override
  void dispose() {
    print('🗑️ [UHF_SCREEN] Disposing UHF Reader Screen');
    _connectionCheckTimer?.cancel();
    _dataSubscription?.cancel();
    widget.bluetoothService.dispose();
    super.dispose();
  }
}