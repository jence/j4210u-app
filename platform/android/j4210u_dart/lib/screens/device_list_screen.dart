import 'package:flutter/material.dart';
import 'package:flutter_bluetooth_serial/flutter_bluetooth_serial.dart';
import '../services/bluetooth_service.dart';
import '../widgets/device_tile.dart';
import 'uhf_reader_screen.dart';

class DeviceListScreen extends StatefulWidget {
  @override
  _DeviceListScreenState createState() => _DeviceListScreenState();
}

class _DeviceListScreenState extends State<DeviceListScreen> {
  final BluetoothService _bluetoothService = BluetoothService();
  List<BluetoothDevice> _bondedDevices = [];
  bool _isLoading = true;
  bool _connectionHandedOff = false; // Track if connection was handed off

  @override
  void initState() {
    super.initState();
    _loadBondedDevices();
  }

  Future<void> _loadBondedDevices() async {
    try {
      bool isEnabled = await _bluetoothService.isBluetoothEnabled();
      if (!isEnabled) {
        await _bluetoothService.enableBluetooth();
      }
      
      List<BluetoothDevice> devices = await _bluetoothService.getBondedDevices();
      setState(() {
        _bondedDevices = devices;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _isLoading = false;
      });
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error loading devices: $e')),
      );
    }
  }

  Future<void> _connectToDevice(BluetoothDevice device) async {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => Center(child: CircularProgressIndicator()),
    );

    bool connected = await _bluetoothService.connectToDevice(device);
    Navigator.pop(context); // Close loading dialog

    if (connected) {
      // Mark that we're handing off the connection to prevent disposal
      _connectionHandedOff = true;
      
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(
          builder: (context) => UHFReaderScreen(
            bluetoothService: _bluetoothService,
            deviceName: device.name ?? 'Unknown Device',
          ),
        ),
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Failed to connect to ${device.name}')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Select UHF Reader'),
      ),
      body: _isLoading
          ? Center(child: CircularProgressIndicator())
          : _bondedDevices.isEmpty
              ? Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(Icons.bluetooth_disabled, size: 64, color: Colors.grey),
                      SizedBox(height: 16),
                      Text('No paired devices found'),
                      SizedBox(height: 16),
                      ElevatedButton(
                        onPressed: _loadBondedDevices,
                        child: Text('Refresh'),
                      ),
                    ],
                  ),
                )
              : ListView.builder(
                  itemCount: _bondedDevices.length,
                  itemBuilder: (context, index) {
                    return DeviceTile(
                      device: _bondedDevices[index],
                      onTap: () => _connectToDevice(_bondedDevices[index]),
                    );
                  },
                ),
    );
  }

  @override
  void dispose() {
    // Only dispose the bluetooth service if we haven't handed off the connection
    if (!_connectionHandedOff) {
      print('🔌 [DEVICE_LIST] Disposing BluetoothService (no connection handed off)');
      _bluetoothService.dispose();
    } else {
      print('✋ [DEVICE_LIST] NOT disposing BluetoothService (connection handed off to UHF Reader)');
    }
    super.dispose();
  }
}