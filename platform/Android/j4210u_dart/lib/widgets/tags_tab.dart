import 'package:flutter/material.dart';
import '../models/tag_data.dart';
import 'tag_item_card.dart';

class TagsTab extends StatelessWidget {
  final List<TagData> tags;
  final bool isScanning;
  final bool isConnected;
  final Animation<double> scanAnimation;
  final Function(TagData tag, int index) onTagCopy;

  const TagsTab({
    super.key,
    required this.tags,
    required this.isScanning,
    required this.isConnected,
    required this.scanAnimation,
    required this.onTagCopy,
  });

  @override
  Widget build(BuildContext context) {
    return tags.isEmpty
        ? _buildEmptyState()
        : _buildTagsList();
  }

  Widget _buildEmptyState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          AnimatedBuilder(
            animation: scanAnimation,
            builder: (context, child) {
              return Transform.rotate(
                angle: isScanning ? scanAnimation.value * 6.28 : 0,
                child: Icon(
                  Icons.nfc,
                  size: 64,
                  color: isScanning ? Colors.blue : Colors.grey[400],
                ),
              );
            },
          ),
          const SizedBox(height: 16),
          Text(
            isScanning
                ? 'Scanning for tags...'
                : 'No tags detected',
            style: TextStyle(
              color: Colors.grey[600],
              fontSize: 18,
              fontWeight: FontWeight.w500,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            isConnected 
                ? 'Press scan to find tags' 
                : 'Connect device first',
            style: TextStyle(
              color: Colors.grey[500],
              fontSize: 14,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildTagsList() {
    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: tags.length,
      itemBuilder: (context, index) => TagItemCard(
        tag: tags[index],
        index: index,
        onCopy: () => onTagCopy(tags[index], index),
      ),
    );
  }
}