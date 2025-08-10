import 'package:flutter/material.dart';
import '../services/uhf_rfid_manager.dart';
import '../models/tag_data.dart';
import '../utils/helpers.dart';
import '../utils/constants.dart';
import 'connection_status_card.dart';
import 'control_buttons.dart';
import 'statistics_card.dart';

class ScannerTab extends StatelessWidget {
  final UHFRFIDManager rfidManager;
  final String status;
  final bool isScanning;
  final int totalTagsScanned;
  final List<TagData> tags;
  final DateTime? lastScanTime;
  final Animation<double> pulseAnimation;
  final Animation<double> scanAnimation;
  final VoidCallback onConnect;
  final VoidCallback onDisconnect;
  final VoidCallback onScan;
  final VoidCallback onGetInfo;
  final VoidCallback onClearLogs;

  const ScannerTab({
    super.key,
    required this.rfidManager,
    required this.status,
    required this.isScanning,
    required this.totalTagsScanned,
    required this.tags,
    required this.lastScanTime,
    required this.pulseAnimation,
    required this.scanAnimation,
    required this.onConnect,
    required this.onDisconnect,
    required this.onScan,
    required this.onGetInfo,
    required this.onClearLogs,
  });

  @override
  Widget build(BuildContext context) {
    return RefreshIndicator(
      onRefresh: () async => onScan(),
      child: SingleChildScrollView(
        physics: const AlwaysScrollableScrollPhysics(),
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            ConnectionStatusCard(
              rfidManager: rfidManager,
              status: status,
              pulseAnimation: pulseAnimation,
            ),
            const SizedBox(height: 16),
            ControlButtons(
              rfidManager: rfidManager,
              isScanning: isScanning,
              scanAnimation: scanAnimation,
              onConnect: onConnect,
              onDisconnect: onDisconnect,
              onScan: onScan,
              onGetInfo: onGetInfo,
              onClearLogs: onClearLogs,
            ),
            const SizedBox(height: 16),
            if (rfidManager.isConnected || totalTagsScanned > 0)
              StatisticsCard(
                totalTagsScanned: totalTagsScanned,
                tags: tags,
                lastScanTime: lastScanTime,
              ),
          ],
        ),
      ),
    );
  }
}