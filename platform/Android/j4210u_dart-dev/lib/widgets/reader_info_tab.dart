import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../services/uhf_rfid_manager.dart';
import '../models/reader_info.dart';
import '../utils/helpers.dart';
import '../utils/colors.dart';
import 'settings_dialog.dart';

class ReaderInfoTab extends StatelessWidget {
  final UHFRFIDManager rfidManager;
  final ReaderInfo? readerInfo;
  final VoidCallback onRefresh;

  const ReaderInfoTab({
    super.key,
    required this.rfidManager,
    required this.readerInfo,
    required this.onRefresh,
  });

  @override
  Widget build(BuildContext context) {
    if (!rfidManager.isConnected) {
      return _buildNotConnectedState();
    }

    if (readerInfo == null) {
      return _buildLoadingState();
    }

    return _buildReaderInfoContent(context);
  }

  Widget _buildNotConnectedState() {
    return const Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(
            Icons.link_off,
            size: 64,
            color: Colors.grey,
          ),
          SizedBox(height: 16),
          Text(
            'Device Not Connected',
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.w500,
              color: Colors.grey,
            ),
          ),
          SizedBox(height: 8),
          Text(
            'Connect to device to view reader information',
            style: TextStyle(
              fontSize: 14,
              color: Colors.grey,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildLoadingState() {
    return const Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          CircularProgressIndicator(),
          SizedBox(height: 16),
          Text(
            'Loading Reader Information...',
            style: TextStyle(
              fontSize: 16,
              color: Colors.grey,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildReaderInfoContent(BuildContext context) {
    return RefreshIndicator(
      onRefresh: () async => onRefresh(),
      child: SingleChildScrollView(
        physics: const AlwaysScrollableScrollPhysics(),
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildDeviceInfoCard(),
            const SizedBox(height: 16),
            _buildConnectionInfoCard(),
            const SizedBox(height: 16),
            _buildFrequencyInfoCard(),
            const SizedBox(height: 16),
            _buildSettingsCard(context),
            const SizedBox(height: 16),
            _buildRawInfoCard(context),
          ],
        ),
      ),
    );
  }

  Widget _buildDeviceInfoCard() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: AppColors.info.withOpacity(0.1),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: const Icon(
                    Icons.device_hub,
                    color: AppColors.info,
                    size: 20,
                  ),
                ),
                const SizedBox(width: 12),
                const Text(
                  'Device Information',
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            _buildInfoRow('Device Type', rfidManager.isMU910 ? 'MU910' : 'MU903'),
            _buildInfoRow('Version', '${readerInfo!.versionInfo[0]}.${readerInfo!.versionInfo[1]}'),
            _buildInfoRow('Reader Type', '0x${readerInfo!.readerType.toRadixString(16).toUpperCase()}'),
            _buildInfoRow('Protocol', '0x${readerInfo!.protocol.toRadixString(16).toUpperCase()}'),
            if (rfidManager.deviceName != null)
              _buildInfoRow('Device Name', rfidManager.deviceName!),
          ],
        ),
      ),
    );
  }

  Widget _buildConnectionInfoCard() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: AppColors.success.withOpacity(0.1),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: const Icon(
                    Icons.settings_ethernet,
                    color: AppColors.success,
                    size: 20,
                  ),
                ),
                const SizedBox(width: 12),
                const Text(
                  'Connection Information',
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            _buildInfoRow('Baud Rate', '${rfidManager.baudRate}'),
            _buildInfoRow('Communication', rfidManager.isMU910 ? 'USB CDC Serial' : 'USB Serial'),
            _buildInfoRow('Address', '0x${readerInfo!.comAdr.toRadixString(16).padLeft(2, '0').toUpperCase()}'),
            _buildInfoRow('Antenna', readerInfo!.antenna.toString()),
          ],
        ),
      ),
    );
  }

  Widget _buildFrequencyInfoCard() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: AppColors.warning.withOpacity(0.1),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: const Icon(
                    Icons.radio,
                    color: AppColors.warning,
                    size: 20,
                  ),
                ),
                const SizedBox(width: 12),
                const Text(
                  'Frequency Information',
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            _buildInfoRow('Band', readerInfo!.band),
            _buildInfoRow('Min Frequency', '${(readerInfo!.minFreq / 1000.0).toStringAsFixed(3)} MHz'),
            _buildInfoRow('Max Frequency', '${(readerInfo!.maxFreq / 1000.0).toStringAsFixed(3)} MHz'),
            _buildInfoRow('Power', '${readerInfo!.power} dB'),
          ],
        ),
      ),
    );
  }

Widget _buildSettingsCard(BuildContext context) {
  return Card(
    child: Padding(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: Colors.purple.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: const Icon(
                  Icons.settings,
                  color: Colors.purple,
                  size: 20,
                ),
              ),
              const SizedBox(width: 12),
              const Expanded(
                child: Text(
                  'Reader Settings',
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
              ElevatedButton.icon(
                onPressed: () => _showSettingsDialog(context),
                icon: const Icon(Icons.tune, size: 16),
                label: const Text('Configure'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.blue,
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(
                    horizontal: 12,
                    vertical: 8,
                  ),
                  textStyle: const TextStyle(fontSize: 12),
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          _buildInfoRow('Band', '${readerInfo!.band} - ${rfidManager.getBandDisplayName(readerInfo!.band)}'),
          _buildInfoRow('Power', '${readerInfo!.power} dB'),
          _buildInfoRow('Scan Time', '${readerInfo!.scanTime * 100} ms'),
          _buildInfoRow('Beep', readerInfo!.beepOn == 1 ? 'Enabled' : 'Disabled'),
          if (readerInfo!.serial != 0)
            _buildInfoRow('Serial', readerInfo!.serial.toString()),
        ],
      ),
    ),
  );
}

void _showSettingsDialog(BuildContext context) {
  showDialog(
    context: context,
    builder: (context) => SettingsDialog(
      rfidManager: rfidManager,
      currentSettings: readerInfo,
      onSettingsChanged: (band, power) {
        // Refresh reader info after settings change
        Future.delayed(const Duration(milliseconds: 500), () {
          onRefresh();
        });
      },
    ),
  );
}

  Widget _buildRawInfoCard(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: Colors.grey.withOpacity(0.1),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: const Icon(
                    Icons.code,
                    color: Colors.grey,
                    size: 20,
                  ),
                ),
                const SizedBox(width: 12),
                const Expanded(
                  child: Text(
                    'Raw Information',
                    style: TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
                IconButton(
                  icon: const Icon(Icons.copy, size: 20),
                  onPressed: () {
                    Clipboard.setData(ClipboardData(text: readerInfo.toString()));
                    Helpers.showSnackBar(context, 'Reader information copied to clipboard');
                  },
                  style: IconButton.styleFrom(
                    backgroundColor: Colors.blue.withOpacity(0.1),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: Colors.grey[50],
                borderRadius: BorderRadius.circular(8),
                border: Border.all(color: Colors.grey[200]!),
              ),
              child: SelectableText(
                readerInfo.toString(),
                style: const TextStyle(
                  fontFamily: 'monospace',
                  fontSize: 12,
                  height: 1.4,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildInfoRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 120,
            child: Text(
              label,
              style: TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.w500,
                color: Colors.grey[600],
              ),
            ),
          ),
          Expanded(
            child: Text(
              value,
              style: const TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
        ],
      ),
    );
  }
}