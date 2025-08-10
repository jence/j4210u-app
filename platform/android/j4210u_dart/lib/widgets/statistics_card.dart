import 'package:flutter/material.dart';
import '../models/tag_data.dart';
import '../utils/helpers.dart';

class StatisticsCard extends StatelessWidget {
  final int totalTagsScanned;
  final List<TagData> tags;
  final DateTime? lastScanTime;

  const StatisticsCard({
    super.key,
    required this.totalTagsScanned,
    required this.tags,
    required this.lastScanTime,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'STATISTICS',
              style: TextStyle(
                fontWeight: FontWeight.bold,
                fontSize: 16,
                color: Colors.grey,
                letterSpacing: 0.5,
              ),
            ),
            const SizedBox(height: 16),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceAround,
              children: [
                _buildStatItem(
                  value: '$totalTagsScanned',
                  label: 'Total Tags',
                  color: Colors.blue,
                ),
                _buildStatItem(
                  value: '${tags.length}',
                  label: 'Last Scan',
                  color: Colors.green,
                ),
                _buildStatItem(
                  value: lastScanTime != null
                      ? Helpers.formatTime(lastScanTime!)
                      : '--:--',
                  label: 'Last Time',
                  color: Colors.orange,
                ),
              ],
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
            fontSize: 24,
            fontWeight: FontWeight.bold,
            color: color,
          ),
        ),
        const SizedBox(height: 4),
        Text(
          label,
          style: const TextStyle(
            fontSize: 12,
            color: Colors.grey,
            fontWeight: FontWeight.w500,
          ),
        ),
      ],
    );
  }
}