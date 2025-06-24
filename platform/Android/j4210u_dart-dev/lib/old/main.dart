// import 'package:flutter/material.dart';
// import 'package:flutter/services.dart';
// import 'package:j42210_dart/raw_usb_test.dart';
// import 'package:j42210_dart/test_direct_usb.dart';
// import 'rfid_manager.dart';

// void main() {
//   runApp(const RFIDReaderApp());
// }

// class RFIDReaderApp extends StatefulWidget {
//   const RFIDReaderApp({super.key});

//   @override
//   State<RFIDReaderApp> createState() => _RFIDReaderAppState();
// }

// class _RFIDReaderAppState extends State<RFIDReaderApp> {
//   final RFIDManager _rfidManager = RFIDManager();
  
//   String _status = 'Disconnected';
//   bool _isScanning = false;
//   List<String> _tags = [];
//   DateTime? _lastScanTime;
//   int _totalTagsScanned = 0;
//   final List<String> _logs = [];
//   DirectUSBTest usbTest = DirectUSBTest();
//   void _addLog(String message) {
//     setState(() {
//       _logs.insert(0, '${DateTime.now().toString().substring(11, 19)}: $message');
//       if (_logs.length > 100) _logs.removeLast();
//     });
//   }

//   Future<void> _connectToReader() async {
//     setState(() {
//       _status = 'Connecting...';
//       _tags.clear();
//     });
    
//     _addLog('Starting connection...');

//     try {
//       final success = await _rfidManager.connect();
      
//       setState(() {
//         if (success) {
//           _status = 'Connected: ${_rfidManager.deviceName} @ ${_rfidManager.baudRate} baud';
//           _addLog('✅ Connected successfully');
//         } else {
//           _status = 'Connection failed';
//           _addLog('❌ Connection failed');
//         }
//       });
//     } catch (e) {
//       setState(() {
//         _status = 'Error: ${e.toString()}';
//       });
//       _addLog('❌ Connection error: $e');
//     }
//   }

//   Future<void> _disconnect() async {
//     await _rfidManager.disconnect();
//     setState(() {
//       _status = 'Disconnected';
//       _isScanning = false;
//       _tags.clear();
//     });
//     _addLog('🔌 Disconnected');
//   }

//   Future<void> _scanTags() async {
//     if (!_rfidManager.isConnected || _isScanning) return;

//     setState(() {
//       _isScanning = true;
//       _status = 'Scanning...';
//       _tags.clear();
//     });
    
//     _addLog('🔍 Starting tag scan...');

//     try {
//       final foundTags = await _rfidManager.scanTags();
      
//       setState(() {
//         _tags = foundTags;
//         _totalTagsScanned += foundTags.length;
//         _lastScanTime = DateTime.now();
//         _status = foundTags.isEmpty 
//             ? 'No tags found' 
//             : 'Found ${foundTags.length} tag(s)';
//       });
      
//       _addLog('📋 Scan complete: ${foundTags.length} tags found');
//       for (final tag in foundTags) {
//         _addLog('🏷️ Tag: $tag');
//       }
//     } catch (e) {
//       setState(() {
//         _status = 'Scan failed: ${e.toString()}';
//       });
//       _addLog('❌ Scan error: $e');
//     } finally {
//       setState(() {
//         _isScanning = false;
//       });
//     }
//   }

//   Future<void> _listDevices() async {
//     _addLog('📱 Listing USB devices...');
//     try {
//       final devices = await RFIDManager.listDevices();
//       _addLog('Found ${devices.length} USB devices:');
//       for (int i = 0; i < devices.length; i++) {
//         _addLog('  $i: ${devices[i]}');
//       }
//     } catch (e) {
//       _addLog('❌ Error listing devices: $e');
//     }

//    await DirectUSBTest.testDirectUSBAccess();
//   }

//   void _clearLogs() {
//     setState(() {
//       _logs.clear();
//     });
//   }

//   @override
//   Widget build(BuildContext context) {
//     return MaterialApp(
//       title: 'UHF RFID Reader',
//       theme: ThemeData(
//         primarySwatch: Colors.blue,
//         visualDensity: VisualDensity.adaptivePlatformDensity,
//       ),
//       home: DefaultTabController(
//         length: 3,
//         child: Builder(
//           builder: (context) {
//             return Scaffold(
//               appBar: AppBar(
//                 title: const Text('UHF RFID Reader'),
//                 bottom: const TabBar(
//                   tabs: [
//                     Tab(icon: Icon(Icons.nfc), text: 'Scanner'),
//                     Tab(icon: Icon(Icons.list), text: 'Tags'),
//                     Tab(icon: Icon(Icons.terminal), text: 'Logs'),
//                   ],
//                 ),
//                 actions: [
//                   IconButton(
//                     icon: const Icon(Icons.refresh),
//                     onPressed: _rfidManager.isConnected ? _scanTags : null,
//                   ),
//                 ],
//               ),
//               body: TabBarView(
//                 children: [
//                   // Scanner Tab
//                   _buildScannerTab(),
//                   // Tags Tab
//                   _buildTagsTab(),
//                   // Logs Tab
//                   _buildLogsTab(),
//                 ],
//               ),
//             );
//           }
//         ),
//       ),
//     );
//   }

//   Widget _buildScannerTab() {
//     return Padding(
//       padding: const EdgeInsets.all(16.0),
//       child: Column(
//         children: [
//           // Connection Status Card
//           Card(
//             child: Padding(
//               padding: const EdgeInsets.all(16.0),
//               child: Column(
//                 crossAxisAlignment: CrossAxisAlignment.start,
//                 children: [
//                   const Text('CONNECTION STATUS',
//                       style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
//                   const SizedBox(height: 8),
//                   Row(
//                     children: [
//                       Icon(
//                         _rfidManager.isConnected ? Icons.check_circle : Icons.error_outline,
//                         color: _rfidManager.isConnected ? Colors.green : Colors.orange,
//                         size: 24,
//                       ),
//                       const SizedBox(width: 12),
//                       Expanded(
//                         child: Text(_status, style: const TextStyle(fontSize: 14)),
//                       ),
//                     ],
//                   ),
//                   if (_rfidManager.isConnected) ...[
//                     const SizedBox(height: 8),
//                     Text(
//                       _rfidManager.deviceInfo ?? 'Unknown device',
//                       style: const TextStyle(fontSize: 12, color: Colors.grey),
//                     ),
//                   ],
//                 ],
//               ),
//             ),
//           ),

//           const SizedBox(height: 16),
//           //Debug Buttons:
//           Row(
//   children: [
//     Expanded(
//       child: OutlinedButton.icon(
//         icon: const Icon(Icons.bug_report),
//         label: const Text('RAW DEBUG'),
//         onPressed: _rfidManager.isConnected ? () async {
//           // await _rfidManager.debugRawCommunication();
//         } : null,
//       ),
//     ),
//     const SizedBox(width: 12),
//     Expanded(
//       child: OutlinedButton.icon(
//   icon: const Icon(Icons.usb),
//   label: const Text('RAW USB TEST'),
//   onPressed: () async {
//     await RawUSBTest.testRawUSBCommunication();
//   },
// ),
//     ),
//   ],
// ),
//           // Control Buttons
//           Row(
//             children: [
//               Expanded(
//                 child: ElevatedButton.icon(
//                   icon: Icon(_rfidManager.isConnected ? Icons.link_off : Icons.link),
//                   label: Text(_rfidManager.isConnected ? 'DISCONNECT' : 'CONNECT'),
//                   onPressed: _rfidManager.isConnected ? _disconnect : _connectToReader,
//                   style: ElevatedButton.styleFrom(
//                     padding: const EdgeInsets.symmetric(vertical: 16),
//                   ),
//                 ),
//               ),
//               const SizedBox(width: 12),
//               Expanded(
//                 child: ElevatedButton.icon(
//                   icon: _isScanning 
//                       ? const SizedBox(
//                           width: 16, 
//                           height: 16, 
//                           child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white)
//                         )
//                       : const Icon(Icons.search),
//                   label: Text(_isScanning ? 'SCANNING...' : 'SCAN TAGS'),
//                   onPressed: _rfidManager.isConnected && !_isScanning ? _scanTags : null,
//                   style: ElevatedButton.styleFrom(
//                     padding: const EdgeInsets.symmetric(vertical: 16),
//                   ),
//                 ),
//               ),
//               Expanded(child: // In your _buildScannerTab() method, add this button:
//             OutlinedButton.icon(
//               icon: const Icon(Icons.info),
//               label: const Text('GET INFO'),
//               onPressed: _rfidManager.isConnected ? () async {
//                 try {
//                   final info = await _rfidManager.getReaderSettings();
//                   ScaffoldMessenger.of(context).showSnackBar(
//                     SnackBar(content: Text(info), duration: const Duration(seconds: 5)),
//                   );
//                 } catch (e) {
//                   ScaffoldMessenger.of(context).showSnackBar(
//                     SnackBar(content: Text('Error: $e')),
//                   );
//                 }
//               } : null,
//             ),),
//             ],
//           ),

//           const SizedBox(height: 12),

//           // Utility Buttons
//           Row(
//             children: [
//               Expanded(
//                 child: OutlinedButton.icon(
//                   icon: const Icon(Icons.devices),
//                   label: const Text('LIST DEVICES'),
//                   onPressed: _listDevices,
//                 ),
//               ),
//               const SizedBox(width: 12),
//               Expanded(
//                 child: OutlinedButton.icon(
//                   icon: const Icon(Icons.clear),
//                   label: const Text('CLEAR LOGS'),
//                   onPressed: _clearLogs,
//                 ),
//               ),
//             ],
//           ),

//           const SizedBox(height: 16),

//           // Stats
//           if (_totalTagsScanned > 0)
//             Card(
//               child: Padding(
//                 padding: const EdgeInsets.all(16.0),
//                 child: Row(
//                   mainAxisAlignment: MainAxisAlignment.spaceAround,
//                   children: [
//                     Column(
//                       children: [
//                         Text('$_totalTagsScanned', style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold)),
//                         const Text('Total Tags', style: TextStyle(fontSize: 12)),
//                       ],
//                     ),
//                     Column(
//                       children: [
//                         Text('${_tags.length}', style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold)),
//                         const Text('Last Scan', style: TextStyle(fontSize: 12)),
//                       ],
//                     ),
//                     if (_lastScanTime != null)
//                       Column(
//                         children: [
//                           Text(_lastScanTime!.toLocal().toString().substring(11, 19), 
//                                style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
//                           const Text('Last Time', style: TextStyle(fontSize: 12)),
//                         ],
//                       ),
//                   ],
//                 ),
//               ),
//             ),
//         ],
//       ),
//     );
//   }

//   Widget _buildTagsTab() {
//     return _tags.isEmpty
//         ? Center(
//             child: Column(
//               mainAxisAlignment: MainAxisAlignment.center,
//               children: [
//                 Icon(Icons.nfc, size: 64, color: Colors.grey[400]),
//                 const SizedBox(height: 16),
//                 Text(
//                   'No tags detected',
//                   style: TextStyle(color: Colors.grey[600], fontSize: 18),
//                 ),
//                 const SizedBox(height: 8),
//                 Text(
//                   _rfidManager.isConnected ? 'Press scan to find tags' : 'Connect device first',
//                   style: TextStyle(color: Colors.grey[500], fontSize: 14),
//                 ),
//               ],
//             ),
//           )
//         : ListView.builder(
//             padding: const EdgeInsets.all(16),
//             itemCount: _tags.length,
//             itemBuilder: (context, index) => Card(
//               margin: const EdgeInsets.symmetric(vertical: 4.0),
//               child: ListTile(
//                 leading: CircleAvatar(
//                   backgroundColor: Colors.blue,
//                   child: Text('${index + 1}', style: const TextStyle(color: Colors.white)),
//                 ),
//                 title: Text(
//                   _tags[index],
//                   style: const TextStyle(fontFamily: 'monospace', fontSize: 16),
//                 ),
//                 subtitle: Text('EPC Code • ${_tags[index].length ~/ 2} bytes'),
//                 trailing: IconButton(
//                   icon: const Icon(Icons.copy),
//                   onPressed: () {
//                     Clipboard.setData(ClipboardData(text: _tags[index]));
//                     ScaffoldMessenger.of(context).showSnackBar(
//                       SnackBar(
//                         content: Text('Copied: ${_tags[index]}'),
//                         duration: const Duration(seconds: 2),
//                       ),
//                     );
//                   },
//                 ),
//               ),
//             ),
//           );
//   }

//   Widget _buildLogsTab() {
//     return Column(
//       children: [
//         Padding(
//           padding: const EdgeInsets.all(16.0),
//           child: Row(
//             children: [
//               const Icon(Icons.terminal, color: Colors.grey),
//               const SizedBox(width: 8),
//               const Text('Debug Logs', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
//               const Spacer(),
//               TextButton.icon(
//                 icon: const Icon(Icons.clear),
//                 label: const Text('Clear'),
//                 onPressed: _clearLogs,
//               ),
//             ],
//           ),
//         ),
//         Expanded(
//           child: _logs.isEmpty
//               ? const Center(
//                   child: Text('No logs yet', style: TextStyle(color: Colors.grey)),
//                 )
//               : ListView.builder(
//                   padding: const EdgeInsets.symmetric(horizontal: 16),
//                   itemCount: _logs.length,
//                   itemBuilder: (context, index) => Padding(
//                     padding: const EdgeInsets.symmetric(vertical: 2),
//                     child: Text(
//                       _logs[index],
//                       style: const TextStyle(
//                         fontFamily: 'monospace',
//                         fontSize: 12,
//                         color: Colors.black87,
//                       ),
//                     ),
//                   ),
//                 ),
//         ),
//       ],
//     );
//   }

//   @override
//   void dispose() {
//     _rfidManager.disconnect();
//     super.dispose();
//   }
// }