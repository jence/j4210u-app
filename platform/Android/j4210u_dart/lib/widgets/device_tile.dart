import 'package:flutter/material.dart';
import 'package:flutter_bluetooth_serial/flutter_bluetooth_serial.dart';

class DeviceTile extends StatelessWidget {
  final BluetoothDevice device;
  final VoidCallback onTap;

  const DeviceTile({
    Key? key,
    required this.device,
    required this.onTap,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return ListTile(
      leading: Icon(Icons.bluetooth),
      title: Text(device.name ?? 'Unknown Device'),
      subtitle: Text(device.address),
      trailing: Icon(Icons.arrow_forward_ios),
      onTap: onTap,
    );
  }
}