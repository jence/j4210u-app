import 'package:flutter/material.dart';
import '../services/mu910_tester.dart'; // Make sure this imports your updated diagnostic class
import 'package:flutter/services.dart';

class MU910TestPage extends StatefulWidget {
  @override
  _MU910TestPageState createState() => _MU910TestPageState();
}

class _MU910TestPageState extends State<MU910TestPage> {
  final UHFDeviceDiagnostic _diagnostic = UHFDeviceDiagnostic(); // Updated class name
  bool _isConnecting = false;
  bool _isTesting = false;

  @override
  void dispose() {
    _diagnostic.disconnect();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('UHF Device Diagnostic'),
        backgroundColor: Colors.blue,
        elevation: 2,
      ),
      body: Padding(
        padding: EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Status Card
            Card(
              color: _getStatusColor(),
              elevation: 4,
              child: Padding(
                padding: EdgeInsets.all(16.0),
                child: Column(
                  children: [
                    Icon(
                      _getStatusIcon(),
                      size: 48,
                      color: Colors.white,
                    ),
                    SizedBox(height: 8),
                    Text(
                      _getStatusText(),
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                        color: Colors.white,
                      ),
                      textAlign: TextAlign.center,
                    ),
                    if (_diagnostic.deviceType != 'Unknown') ...[
                      SizedBox(height: 4),
                      Text(
                        'Device Type: ${_diagnostic.deviceType}',
                        style: TextStyle(
                          fontSize: 14,
                          color: Colors.white70,
                        ),
                      ),
                    ],
                  ],
                ),
              ),
            ),
            
            SizedBox(height: 16),
            
            // Control Buttons
            Row(
              children: [
                Expanded(
                  child: ElevatedButton.icon(
                    onPressed: _isConnecting ? null : _connectDevice,
                    icon: _isConnecting 
                        ? SizedBox(
                            width: 16,
                            height: 16,
                            child: CircularProgressIndicator(
                              color: Colors.white,
                              strokeWidth: 2,
                            ),
                          )
                        : Icon(Icons.usb),
                    label: Text(_isConnecting ? 'Connecting...' : 'Connect Device'),
                    style: ElevatedButton.styleFrom(
                      padding: EdgeInsets.symmetric(vertical: 12),
                    ),
                  ),
                ),
                SizedBox(width: 8),
                Expanded(
                  child: ElevatedButton.icon(
                    onPressed: (!_diagnostic.isConnected || _isTesting) ? null : _runDiagnostic,
                    icon: _isTesting 
                        ? SizedBox(
                            width: 16,
                            height: 16,
                            child: CircularProgressIndicator(
                              color: Colors.white,
                              strokeWidth: 2,
                            ),
                          )
                        : Icon(Icons.search),
                    label: Text(_isTesting ? 'Testing...' : 'Run Diagnostic'),
                    style: ElevatedButton.styleFrom(
                      padding: EdgeInsets.symmetric(vertical: 12),
                    ),
                  ),
                ),
              ],
            ),
            
            SizedBox(height: 16),
            
            // Test Results Summary
            if (_diagnostic.testResults.isNotEmpty) ...[
              Card(
                elevation: 2,
                child: Padding(
                  padding: EdgeInsets.all(16.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Icon(Icons.assessment, color: Colors.blue),
                          SizedBox(width: 8),
                          Text(
                            'Diagnostic Results',
                            style: TextStyle(
                              fontSize: 18,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ],
                      ),
                      Divider(),
                      ..._diagnostic.testResults.entries.map((entry) {
                        return Padding(
                          padding: EdgeInsets.symmetric(vertical: 4),
                          child: Row(
                            children: [
                              Icon(
                                entry.value ? Icons.check_circle : Icons.cancel,
                                color: entry.value ? Colors.green : Colors.red,
                                size: 20,
                              ),
                              SizedBox(width: 8),
                              Expanded(
                                child: Text(
                                  entry.key,
                                  style: TextStyle(fontSize: 14),
                                ),
                              ),
                              Text(
                                entry.value ? 'PASS' : 'FAIL',
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  color: entry.value ? Colors.green : Colors.red,
                                ),
                              ),
                            ],
                          ),
                        );
                      }).toList(),
                    ],
                  ),
                ),
              ),
              SizedBox(height: 16),
            ],
            
            // Quick Actions (if device is detected)
            if (_diagnostic.isConnected && _diagnostic.deviceType != 'Unknown') ...[
              Card(
                elevation: 2,
                child: Padding(
                  padding: EdgeInsets.all(16.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Quick Actions',
                        style: TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      SizedBox(height: 8),
                      Row(
                        children: [
                          Expanded(
                            child: ElevatedButton(
                              onPressed: _disconnect,
                              child: Text('Disconnect'),
                              style: ElevatedButton.styleFrom(
                                backgroundColor: Colors.red,
                                foregroundColor: Colors.white,
                              ),
                            ),
                          ),
                          SizedBox(width: 8),
                          Expanded(
                            child: ElevatedButton(
                              onPressed: _clearLog,
                              child: Text('Clear Log'),
                              style: ElevatedButton.styleFrom(
                                backgroundColor: Colors.orange,
                                foregroundColor: Colors.white,
                              ),
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ),
              SizedBox(height: 16),
            ],
            
            // Log Output
            Row(
              children: [
                Icon(Icons.terminal, color: Colors.grey[600]),
                SizedBox(width: 8),
                Text(
                  'Diagnostic Log',
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                Spacer(),
                if (_diagnostic.detectionLog.isNotEmpty)
                  IconButton(
                    onPressed: _copyLogToClipboard,
                    icon: Icon(Icons.copy, size: 20),
                    tooltip: 'Copy log to clipboard',
                  ),
              ],
            ),
            SizedBox(height: 8),
            Expanded(
              child: Card(
                elevation: 2,
                child: Container(
                  width: double.infinity,
                  padding: EdgeInsets.all(12.0),
                  child: SingleChildScrollView(
                    child: SelectableText(
                      _diagnostic.detectionLog.isEmpty 
                          ? 'No logs yet.\n\n1. Connect to start device detection\n2. Run diagnostic to test communication\n3. Check results above'
                          : _diagnostic.detectionLog,
                      style: TextStyle(
                        fontFamily: 'Courier',
                        fontSize: 12,
                        height: 1.4,
                      ),
                    ),
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Color _getStatusColor() {
    if (!_diagnostic.isConnected) return Colors.grey[700]!;
    if (_diagnostic.deviceType != 'Unknown') return Colors.green;
    if (_diagnostic.testResults.isNotEmpty) return Colors.red;
    return Colors.blue;
  }

  IconData _getStatusIcon() {
    if (!_diagnostic.isConnected) return Icons.usb_off;
    if (_diagnostic.deviceType != 'Unknown') return Icons.check_circle;
    if (_diagnostic.testResults.isNotEmpty) return Icons.error;
    return Icons.usb;
  }

  String _getStatusText() {
    if (!_diagnostic.isConnected) return 'Not Connected';
    if (_diagnostic.deviceType != 'Unknown') return '${_diagnostic.deviceType} Detected!';
    if (_diagnostic.testResults.isNotEmpty) return 'No Device Response';
    return 'Connected - Ready for Diagnostic';
  }

  Future<void> _connectDevice() async {
    setState(() {
      _isConnecting = true;
    });

    try {
      final connected = await _diagnostic.connect();
      if (connected && mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Device connected successfully!'),
            backgroundColor: Colors.green,
          ),
        );
      } else if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Failed to connect to device'),
            backgroundColor: Colors.red,
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Connection error: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    } finally {
      if (mounted) {
        setState(() {
          _isConnecting = false;
        });
      }
    }
  }

  Future<void> _runDiagnostic() async {
    setState(() {
      _isTesting = true;
    });

    try {
      final detected = await _diagnostic.detectDevice();
      if (mounted) {
        final message = detected 
            ? 'Device detected: ${_diagnostic.deviceType}'
            : 'No compatible device found';
        final color = detected ? Colors.green : Colors.orange;
        
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(message),
            backgroundColor: color,
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Diagnostic error: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    } finally {
      if (mounted) {
        setState(() {
          _isTesting = false;
        });
      }
    }
  }

  Future<void> _disconnect() async {
    await _diagnostic.disconnect();
    if (mounted) {
      setState(() {});
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Device disconnected'),
          backgroundColor: Colors.blue,
        ),
      );
    }
  }

  void _clearLog() {
    setState(() {
      // _diagnostic._detectionLog = ''; // You'll need to add a public method for this
    });
  }

Future<void> _copyLogToClipboard() async {
  await Clipboard.setData(ClipboardData(text: _diagnostic.detectionLog));
  if (mounted) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('Log copied to clipboard'),
        backgroundColor: Colors.blue,
      ),
    );
  }
}
}