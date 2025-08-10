import 'package:flutter/material.dart';

class LogsTab extends StatelessWidget {
  final List<String> logs;
  final VoidCallback onClearLogs;

  const LogsTab({
    super.key,
    required this.logs,
    required this.onClearLogs,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        _buildHeader(),
        Expanded(
          child: logs.isEmpty ? _buildEmptyState() : _buildLogsList(),
        ),
      ],
    );
  }

  Widget _buildHeader() {
    return Container(
      padding: const EdgeInsets.all(16.0),
      decoration: BoxDecoration(
        color: Colors.white,
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 4,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Row(
        children: [
          const Icon(Icons.terminal, color: Colors.grey),
          const SizedBox(width: 8),
          const Text(
            'Debug Logs',
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
            ),
          ),
          const Spacer(),
          TextButton.icon(
            icon: const Icon(Icons.clear),
            label: const Text('Clear'),
            onPressed: onClearLogs,
          ),
        ],
      ),
    );
  }

  Widget _buildEmptyState() {
    return const Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.terminal, size: 64, color: Colors.grey),
          SizedBox(height: 16),
          Text(
            'No logs yet',
            style: TextStyle(
              color: Colors.grey,
              fontSize: 18,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildLogsList() {
    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: logs.length,
      itemBuilder: (context, index) => Container(
        margin: const EdgeInsets.symmetric(vertical: 2),
        padding: const EdgeInsets.symmetric(
          horizontal: 12,
          vertical: 8,
        ),
        decoration: BoxDecoration(
          color: index.isEven 
              ? Colors.grey[50]
              : Colors.transparent,
          borderRadius: BorderRadius.circular(8),
        ),
        child: Text(
          logs[index],
          style: const TextStyle(
            fontFamily: 'monospace',
            fontSize: 13,
            color: Colors.black87,
            height: 1.3,
          ),
        ),
      ),
    );
  }
}