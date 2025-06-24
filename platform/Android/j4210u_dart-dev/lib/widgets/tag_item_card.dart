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
      margin: const EdgeInsets.symmetric(vertical: 4.0),
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
      width: 40,
      height: 40,
      decoration: BoxDecoration(
        color: Colors.blue.withOpacity(0.1),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Center(
        child: Text(
          '${index + 1}',
          style: const TextStyle(
            fontWeight: FontWeight.bold,
            color: Colors.blue,
          ),
        ),
      ),
    );
  }

  Widget _buildEpcText() {
    return Text(
      tag.epc,
      style: const TextStyle(
        fontFamily: 'monospace',
        fontSize: 16,
        fontWeight: FontWeight.w500,
      ),
    );
  }

  Widget _buildTagDetails() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text('EPC Code • ${tag.epc.length ~/ 2} bytes'),
        const SizedBox(height: 4),
        Row(
          children: [
            _buildAntennaBadge(),
            const SizedBox(width: 8),
            _buildRssiBadge(),
            const SizedBox(width: 8),
            if (tag.count > 1) _buildCountBadge(),
          ],
        ),
      ],
    );
  }

  Widget _buildAntennaBadge() {
    return Container(
      padding: const EdgeInsets.symmetric(
        horizontal: 8,
        vertical: 2,
      ),
      decoration: BoxDecoration(
        color: AppColors.antenna.withOpacity(0.1),
        borderRadius: BorderRadius.circular(6),
        border: Border.all(
          color: AppColors.antenna.withOpacity(0.3),
        ),
      ),
      child: Text(
        'Ant ${tag.antenna}',
        style: const TextStyle(
          fontSize: 11,
          fontWeight: FontWeight.w600,
          color: AppColors.antenna,
        ),
      ),
    );
  }

  Widget _buildRssiBadge() {
    final rssiColor = Helpers.getRssiColor(tag.rssi);
    return Container(
      padding: const EdgeInsets.symmetric(
        horizontal: 8,
        vertical: 2,
      ),
      decoration: BoxDecoration(
        color: rssiColor.withOpacity(0.1),
        borderRadius: BorderRadius.circular(6),
        border: Border.all(
          color: rssiColor.withOpacity(0.3),
        ),
      ),
      child: Text(
        '${tag.rssi}dBm',
        style: TextStyle(
          fontSize: 11,
          fontWeight: FontWeight.w600,
          color: rssiColor,
        ),
      ),
    );
  }

  Widget _buildCountBadge() {
    return Container(
      padding: const EdgeInsets.symmetric(
        horizontal: 8,
        vertical: 2,
      ),
      decoration: BoxDecoration(
        color: AppColors.count.withOpacity(0.1),
        borderRadius: BorderRadius.circular(6),
        border: Border.all(
          color: AppColors.count.withOpacity(0.3),
        ),
      ),
      child: Text(
        '×${tag.count}',
        style: const TextStyle(
          fontSize: 11,
          fontWeight: FontWeight.w600,
          color: AppColors.count,
        ),
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