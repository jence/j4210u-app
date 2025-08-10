import 'package:flutter/material.dart';
import '../services/uhf_rfid_manager.dart';
import '../utils/constants.dart';
import '../utils/colors.dart';

class ControlButtons extends StatelessWidget {
  final UHFRFIDManager rfidManager;
  final bool isScanning;
  final Animation<double> scanAnimation;
  final VoidCallback onConnect;
  final VoidCallback onDisconnect;
  final VoidCallback onScan;
  final VoidCallback onGetInfo;
  final VoidCallback onClearLogs;

  const ControlButtons({
    super.key,
    required this.rfidManager,
    required this.isScanning,
    required this.scanAnimation,
    required this.onConnect,
    required this.onDisconnect,
    required this.onScan,
    required this.onGetInfo,
    required this.onClearLogs,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Row(
          children: [
            Expanded(
              child: ElevatedButton.icon(
                icon: Icon(
                  rfidManager.isConnected ? Icons.link_off : Icons.link,
                ),
                label: Text(
                  rfidManager.isConnected ? AppConstants.disconnect : AppConstants.connect,
                ),
                onPressed: rfidManager.isConnected ? onDisconnect : onConnect,
                style: ElevatedButton.styleFrom(
                  backgroundColor: rfidManager.isConnected 
                      ? AppColors.error
                      : Theme.of(context).primaryColor,
                  foregroundColor: Colors.white,
                ),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: _buildScanButton(),
            ),
          ],
        ),
        const SizedBox(height: 12),
        Row(
          children: [
            Expanded(
              child: OutlinedButton.icon(
                icon: const Icon(Icons.info_outline),
                label: const Text(AppConstants.getInfo),
                onPressed: rfidManager.isConnected ? onGetInfo : null,
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: OutlinedButton.icon(
                icon: const Icon(Icons.clear),
                label: const Text(AppConstants.clearLogs),
                onPressed: onClearLogs,
              ),
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildScanButton() {
    return AnimatedBuilder(
      animation: scanAnimation,
      builder: (context, child) {
        return ElevatedButton.icon(
          icon: isScanning
              ? SizedBox(
                  width: 16,
                  height: 16,
                  child: CircularProgressIndicator(
                    strokeWidth: 2,
                    color: Colors.white,
                    value: scanAnimation.value,
                  ),
                )
              : const Icon(Icons.search),
          label: Text(isScanning ? AppConstants.scanning_ : AppConstants.scanTags),
          onPressed: rfidManager.isConnected && !isScanning ? onScan : null,
          style: ElevatedButton.styleFrom(
            backgroundColor: AppColors.success,
            foregroundColor: Colors.white,
          ),
        );
      },
    );
  }
}