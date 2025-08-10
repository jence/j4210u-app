import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:j42210_dart/screens/home_screen.dart';
import '../services/uhf_rfid_manager.dart';
import '../models/tag_data.dart';
import '../models/reader_info.dart';
import '../widgets/tags_tab.dart';
import '../widgets/logs_tab.dart';
import '../widgets/reader_info_tab.dart';
import '../utils/constants.dart';
import '../utils/helpers.dart';
import '../utils/colors.dart';
import 'mu910_test_page.dart';

class RFIDReaderScreen extends StatefulWidget {
  const RFIDReaderScreen({super.key});

  @override
  State<RFIDReaderScreen> createState() => _RFIDReaderScreenState();
}

class _RFIDReaderScreenState extends State<RFIDReaderScreen>
    with TickerProviderStateMixin {
  final UHFRFIDManager _rfidManager = UHFRFIDManager();

  String _status = AppConstants.disconnected;
  bool _isScanning = false;
  List<TagData> _tags = [];
  DateTime? _lastScanTime;
  int _totalTagsScanned = 0;
  int _scanCount = 0;
  final List<String> _logs = [];
  ReaderInfo? _readerInfo;
  var isTimerRunning=false;
  late AnimationController _pulseController;
  late AnimationController _scanController;
  late Animation<double> _pulseAnimation;
  late Animation<double> _scanAnimation;

  @override
  void initState() {
    super.initState();
    // _checkGPIO();
    _initializeAnimations();
  }

  void _initializeAnimations() {
    _pulseController = AnimationController(
      duration: const Duration(
        milliseconds: AppConstants.pulseAnimationDuration,
      ),
      vsync: this,
    );
    _scanController = AnimationController(
      duration: const Duration(milliseconds: AppConstants.animationDuration),
      vsync: this,
    );

    _pulseAnimation = Tween<double>(begin: 0.9, end: 1.1).animate(
      CurvedAnimation(parent: _pulseController, curve: Curves.easeInOut),
    );
    _scanAnimation = Tween<double>(begin: 0.0, end: 1.0).animate(
      CurvedAnimation(parent: _scanController, curve: Curves.easeInOut),
    );
  }

  @override
  void dispose() {
    _pulseController.dispose();
    _scanController.dispose();
    _rfidManager.disconnect();
    super.dispose();
  }

  void _addLog(String message) {
    setState(() {
      _logs.insert(0, '${Helpers.formatLogTime(DateTime.now())}: $message');
      if (_logs.length > AppConstants.maxLogs) _logs.removeLast();
    });
  }

  Future<void> _connectToReader() async {
    setState(() {
      _status = AppConstants.connecting;
      _tags.clear();
      _readerInfo = null;
    });

    _addLog('🔌 Starting connection...');

    try {
      final success = await _rfidManager.connect();

      setState(() {
        if (success) {
          _status = AppConstants.connected;
          _pulseController.repeat(reverse: true);
          _addLog('✅ Connected successfully');
          _getReaderInfo(); // Automatically get reader info on connect
        } else {
          _status = AppConstants.connectionFailed;
          _addLog('❌ Connection failed');
        }
      });
    } catch (e) {
      setState(() {
        _status = 'Error: ${e.toString()}';
      });
      _addLog('❌ Connection error: $e');
    }
  }

  Future<void> _disconnect() async {
    _pulseController.stop();
    _scanController.stop();
    await _rfidManager.disconnect();
    setState(() {
      isTimerRunning =false;
      _status = AppConstants.disconnected;
      _isScanning = false;
      _tags.clear();
      _scanCount = 0;
      _totalTagsScanned = 0;
      _readerInfo = null;
    });
    _addLog('🔌 Disconnected');
  }

  Future<void> _scanTags() async {
    if (!_rfidManager.isConnected || _isScanning) return;

    setState(() {
      _isScanning = true;
      _status = AppConstants.scanning;
      _scanCount++;
      _tags.clear();
    });

    _scanController.repeat();
    _addLog('🔍 Starting tag scan...');

    try {
      final foundTags = await _rfidManager.scanTags();

      setState(() {
        _tags = foundTags;
        _totalTagsScanned += foundTags.length;
        _lastScanTime = DateTime.now();
        _status = foundTags.isEmpty
            ? AppConstants.noTagsFound
            : 'Found ${foundTags.length} tag(s)';
      });

      _addLog('📋 Scan complete: ${foundTags.length} tags found');
      for (final tag in foundTags) {
        _addLog(
          '🏷️ Tag: ${tag.epc} (Ant: ${tag.antenna}, RSSI: ${tag.rssi}dBm)',
        );
      }
    } catch (e) {
      setState(() {
        _status = 'Scan failed: ${e.toString()}';
      });
      _addLog('❌ Scan error: $e');
    } finally {
      _scanController.stop();
      setState(() {
        _isScanning = false;
      });
    }
  }

  Future<void> _getReaderInfo() async {
    if (!_rfidManager.isConnected) return;

    _addLog('ℹ️ Getting reader information...');
    try {
      final readerInfo = await _rfidManager.getSettings();
      if (readerInfo != null) {
        setState(() {
          _readerInfo = readerInfo;
        });
        _addLog('ℹ️ Reader info retrieved');
      }
    } catch (e) {
      Helpers.showSnackBar(context, 'Error: $e', isError: true);
      _addLog('❌ Reader info error: $e');
    }
  }

  void _clearLogs() {
    setState(() {
      _logs.clear();
    });
    _addLog('🧹 Logs cleared');
  }

  Color _getStatusColor() {
    return Helpers.getStatusColor(_status, _rfidManager.isConnected);
  }

  IconData _getStatusIcon() {
    return Helpers.getStatusIcon(_status, _rfidManager.isConnected);
  }

  @override
  Widget build(BuildContext context) {
    
    return DefaultTabController(
      length: 3,
      child: Scaffold(
        appBar: AppBar(
          title: const Text(AppConstants.appTitle),
          actions: [
            IconButton(
              icon: const Icon(Icons.bug_report),
              onPressed: () => Navigator.push(
                context,
                MaterialPageRoute(builder: (context) => MU910TestPage()),
              ),
              tooltip: 'Device Diagnostics',
            ),
            IconButton(
              icon: const Icon(Icons.bluetooth),
              onPressed: () => Navigator.push(
                context,
                MaterialPageRoute(builder: (context) => HomeScreen()),
              ),
              tooltip: 'Handle Bluetooth Connections',
            ),
          ],
        ),
        body: Column(
          children: [
                       
            // Connection Status and Control Buttons Section
            Container(
              color: Colors.white,
              child: Column(
                children: [
                  // Connection Status
                  Container(
                    padding: const EdgeInsets.all(16),
                    child: Row(
                      children: [
                        AnimatedBuilder(
                          animation: _pulseAnimation,
                          builder: (context, child) {
                            return Transform.scale(
                              scale: _rfidManager.isConnected
                                  ? _pulseAnimation.value
                                  : 1.0,
                              child: Container(
                                padding: const EdgeInsets.all(8),
                                decoration: BoxDecoration(
                                  color: _getStatusColor().withOpacity(0.1),
                                  borderRadius: BorderRadius.circular(8),
                                ),
                                child: Icon(
                                  _getStatusIcon(),
                                  color: _getStatusColor(),
                                  size: 24,
                                ),
                              ),
                            );
                          },
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                _status,
                                style: const TextStyle(
                                  fontSize: 16,
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                              if (_rfidManager.isConnected) ...[
                                const SizedBox(height: 2),
                                Row(
                                  children: [
                                    Container(
                                      padding: const EdgeInsets.symmetric(
                                        horizontal: 6,
                                        vertical: 2,
                                      ),
                                      decoration: BoxDecoration(
                                        color: _rfidManager.isMU910
                                            ? AppColors.mu910.withOpacity(0.1)
                                            : AppColors.mu903.withOpacity(0.1),
                                        borderRadius: BorderRadius.circular(4),
                                        border: Border.all(
                                          color: _rfidManager.isMU910
                                              ? AppColors.mu910
                                              : AppColors.mu903,
                                          width: 1,
                                        ),
                                      ),
                                      child: Text(
                                        _rfidManager.isMU910
                                            ? 'MU910'
                                            : 'MU903',
                                        style: TextStyle(
                                          fontSize: 10,
                                          fontWeight: FontWeight.w600,
                                          color: _rfidManager.isMU910
                                              ? AppColors.mu910
                                              : AppColors.mu903,
                                        ),
                                      ),
                                    ),
                                    const SizedBox(width: 8),
                                    Text(
                                      '${_rfidManager.baudRate} baud',
                                      style: TextStyle(
                                        fontSize: 11,
                                        color: Colors.grey[600],
                                      ),
                                    ),
                                  ],
                                ),

                  Row(
                    children: [
                      Checkbox(
                        value: isTimerRunning,
                        onChanged: (bool? value) {
                          setState(() {
                            isTimerRunning = value ?? false;
                          });
                          // Restart GPIO check with the updated timer state
                         if(value ==true) _checkGPIO();
                        },
                      ),
                      Text("Trigger"),
                    ],
                  ),
                                // ElevatedButton(
                                //   onPressed: () {
                                //    _checkGPIO();
                                //   },
                                //   child: Text('GetGPI'),
                                // ),
                              ],
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),

                  // Control Buttons
                  Padding(
                    padding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
                    child: Row(
                      children: [
                        Expanded(
                          child: ElevatedButton.icon(
                            icon: Icon(
                              _rfidManager.isConnected
                                  ? Icons.link_off
                                  : Icons.link,
                              size: 18,
                            ),
                            label: Text(
                              _rfidManager.isConnected
                                  ? 'DISCONNECT'
                                  : 'CONNECT',
                              style: const TextStyle(fontSize: 13),
                            ),
                            onPressed: _rfidManager.isConnected
                                ? _disconnect
                                : _connectToReader,
                            style: ElevatedButton.styleFrom(
                              backgroundColor: _rfidManager.isConnected
                                  ? AppColors.error
                                  : Theme.of(context).primaryColor,
                              foregroundColor: Colors.white,
                              padding: const EdgeInsets.symmetric(vertical: 12),
                            ),
                          ),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: AnimatedBuilder(
                            animation: _scanAnimation,
                            builder: (context, child) {
                              return ElevatedButton.icon(
                                icon: _isScanning
                                    ? SizedBox(
                                        width: 14,
                                        height: 14,
                                        child: CircularProgressIndicator(
                                          strokeWidth: 2,
                                          color: Colors.white,
                                          value: _scanAnimation.value,
                                        ),
                                      )
                                    : const Icon(Icons.search, size: 18),
                                label: Text(
                                  _isScanning ? 'SCANNING...' : 'SCAN TAGS',
                                  style: const TextStyle(fontSize: 13),
                                ),
                                onPressed:
                                    _rfidManager.isConnected && !_isScanning
                                    ? _scanTags
                                    : null,
                                style: ElevatedButton.styleFrom(
                                  backgroundColor: AppColors.success,
                                  foregroundColor: Colors.white,
                                  padding: const EdgeInsets.symmetric(
                                    vertical: 12,
                                  ),
                                ),
                              );
                            },
                          ),
                        ),
                      ],
                    ),
                  ),

                  // Statistics Row (if connected)
                  if (_rfidManager.isConnected || _totalTagsScanned > 0)
                    Container(
                      padding: const EdgeInsets.fromLTRB(16, 0, 16, 12),
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.spaceAround,
                        children: [
                          _buildStatItem(
                            value: '$_totalTagsScanned',
                            label: 'Total',
                            color: Colors.blue,
                          ),
                          _buildStatItem(
                            value: '${_tags.length}',
                            label: 'Last Scan',
                            color: Colors.green,
                          ),
                          _buildStatItem(
                            value: _lastScanTime != null
                                ? Helpers.formatTime(_lastScanTime!)
                                : '--:--',
                            label: 'Time',
                            color: Colors.orange,
                          ),
                        ],
                      ),
                    ),

                  // Tab Bar
                  const TabBar(
                    labelColor: AppColors.primary,
                    unselectedLabelColor: Colors.grey,
                    indicatorColor: AppColors.primary,
                    tabs: [
                      Tab(
                        icon: Tooltip(message: 'Tags', child: Icon(Icons.list)),
                      ),
                      Tab(
                        icon: Tooltip(
                          message: 'Reader Info',
                          child: Icon(Icons.info),
                        ),
                      ),
                      Tab(
                        icon: Tooltip(
                          message: 'Logs',
                          child: Icon(Icons.terminal),
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),

            // Tab Views
            Expanded(
              child: TabBarView(
                children: [
                  TagsTab(
                    tags: _tags,
                    isScanning: _isScanning,
                    isConnected: _rfidManager.isConnected,
                    scanAnimation: _scanAnimation,
                    onTagCopy: (tag, index) {
                      Helpers.showSnackBar(
                        context,
                        'Tag ${index + 1} copied to clipboard',
                      );
                    },
                  ),
                  ReaderInfoTab(
                    rfidManager: _rfidManager,
                    readerInfo: _readerInfo,
                    onRefresh: _getReaderInfo,
                  ),
                  LogsTab(logs: _logs, onClearLogs: _clearLogs),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildStatItem({
    required String value,
    required String label,
    required Color color,
  }) {
    return Column(
      children: [
        Text(
          value,
          style: TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.bold,
            color: color,
          ),
        ),
        Text(
          label,
          style: const TextStyle(
            fontSize: 11,
            color: Colors.grey,
            fontWeight: FontWeight.w500,
          ),
        ),
      ],
    );
  }

  // void _checkGPIO() {
  //   Timer.periodic(Duration(milliseconds: 500), (timer) async {
  //     if (_rfidManager.isConnected) {
  //       var res = await _rfidManager.getGPI(0);
  //       debugPrint("###########Res is $res");
  //       if (res == 1) {
  //         debugPrint("###########Res is 1");
  //        await _scanTags();
  //       //  timer.cancel();
  //       res=0;
  //       timer.cancel();
  //       }
  //     }
      
  //   });
  // }

  Timer? _gpioTimer; // Declare a variable to store the Timer instance.

void _checkGPIO() {
  // If the checkbox is unchecked, cancel the timer.
  if (!isTimerRunning) {
    _gpioTimer?.cancel(); // Stop the timer if it's running
    return;
  }

  // If the checkbox is checked, start the timer.
  if (_gpioTimer == null || !_gpioTimer!.isActive) {
    _gpioTimer = Timer.periodic(Duration(milliseconds: 500), (timer) async {
      if (_rfidManager.isConnected) {
        var res = await _rfidManager.getGPI(0);
        debugPrint("###########Res is $res");
        if (res == 1) {
          debugPrint("###########Res is 1");
          await _scanTags();
        }
      }
    });
  }
}
}
