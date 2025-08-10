import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../models/tag_data.dart';
import '../utils/helpers.dart';
import '../utils/colors.dart';

class TagItemCard extends StatelessWidget {
  final TagData tag;
  final int index;
  final VoidCallback onCopy;

  const TagItemCard({
    super.key,
    required this.tag,
    required this.index,
    required this.onCopy,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
      elevation: 2,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
      ),
      child: ListTile(
        contentPadding: const EdgeInsets.all(16),
        leading: _buildIndexBadge(),
        title: _buildEpcText(),
        subtitle: _buildTagDetails(),
        trailing: _buildCopyButton(),
      ),
    );
  }

  Widget _buildIndexBadge() {
    return Container(
      width: 45,
      height: 45,
      decoration: BoxDecoration(
        color: Colors.blue.withOpacity(0.1),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(
          color: Colors.blue.withOpacity(0.2),
          width: 1,
        ),
      ),
      child: Center(
        child: Text(
          '${index + 1}',
          style: const TextStyle(
            fontWeight: FontWeight.bold,
            color: Colors.blue,
            fontSize: 16,
          ),
        ),
      ),
    );
  }

  Widget _buildEpcText() {
    return Text(
      Helpers.formatEpc(tag.epc),
      style: TextStyle(
        fontFamily: 'monospace',
        fontSize: 16,
        fontWeight: FontWeight.w600,
        color: AppColors.textPrimary,
      ),
    );
  }

  Widget _buildTagDetails() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const SizedBox(height: 4),
        Text(
          'EPC Code • ${tag.epc.length ~/ 2} bytes',
          style: TextStyle(
            fontSize: 12,
            color: AppColors.textSecondary,
            fontWeight: FontWeight.w500,
          ),
        ),
        const SizedBox(height: 8),
        Row(
          children: [
            _buildAntennaBadge(),
            const SizedBox(width: 8),
            _buildRssiBadge(),
            const SizedBox(width: 8),
            // if (tag.count > 1) _buildCountBadge(),
          ],
        ),
        const SizedBox(height: 6),
        Text(
          'Detected: ${Helpers.formatTime(tag.timestamp)}',
          style: TextStyle(
            fontSize: 10,
            color: AppColors.timestamp,
            fontWeight: FontWeight.w500,
          ),
        ),
      ],
    );
  }

  Widget _buildAntennaBadge() {
    final antennaColor = Helpers.getAntennaColor(tag.antenna);
    return Container(
      padding: const EdgeInsets.symmetric(
        horizontal: 8,
        vertical: 3,
      ),
      decoration: BoxDecoration(
        color: antennaColor.withOpacity(0.1),
        borderRadius: BorderRadius.circular(6),
        border: Border.all(
          color: antennaColor.withOpacity(0.3),
        ),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(
            Icons.settings_input_antenna,
            size: 12,
            color: antennaColor,
          ),
          const SizedBox(width: 4),
          Text(
            'Ant ${tag.antenna}',
            style: TextStyle(
              fontSize: 11,
              fontWeight: FontWeight.w600,
              color: antennaColor,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildRssiBadge() {
    final rssiColor = Helpers.getRssiColor(tag.rssi);
    return Container(
      padding: const EdgeInsets.symmetric(
        horizontal: 8,
        vertical: 3,
      ),
      decoration: BoxDecoration(
        color: rssiColor.withOpacity(0.1),
        borderRadius: BorderRadius.circular(6),
        border: Border.all(
          color: rssiColor.withOpacity(0.3),
        ),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(
            Icons.signal_cellular_alt,
            size: 12,
            color: rssiColor,
          ),
          const SizedBox(width: 4),
          Text(
            '${tag.rssi}dBm',
            style: TextStyle(
              fontSize: 11,
              fontWeight: FontWeight.w600,
              color: rssiColor,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildCountBadge() {
    return Container(
      padding: const EdgeInsets.symmetric(
        horizontal: 8,
        vertical: 3,
      ),
      decoration: BoxDecoration(
        color: AppColors.count.withOpacity(0.1),
        borderRadius: BorderRadius.circular(6),
        border: Border.all(
          color: AppColors.count.withOpacity(0.3),
        ),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(
            Icons.repeat,
            size: 12,
            color: AppColors.count,
          ),
          const SizedBox(width: 4),
          Text(
            '×${tag.count}',
            style: const TextStyle(
              fontSize: 11,
              fontWeight: FontWeight.w600,
              color: AppColors.count,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildCopyButton() {
    return IconButton(
      icon: const Icon(Icons.copy),
      onPressed: () {
        Clipboard.setData(ClipboardData(text: tag.epc));
        onCopy();
      },
      style: IconButton.styleFrom(
        backgroundColor: Colors.blue.withOpacity(0.1),
      ),
    );
  }
}