import 'package:flutter/material.dart';
import '../services/uhf_rfid_manager.dart';
import '../utils/helpers.dart';
import '../utils/colors.dart';

class ConnectionStatusCard extends StatelessWidget {
  final UHFRFIDManager rfidManager;
  final String status;
  final Animation<double> pulseAnimation;

  const ConnectionStatusCard({
    super.key,
    required this.rfidManager,
    required this.status,
    required this.pulseAnimation,
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
              'CONNECTION STATUS',
              style: TextStyle(
                fontWeight: FontWeight.bold,
                fontSize: 16,
                color: Colors.grey,
                letterSpacing: 0.5,
              ),
            ),
            const SizedBox(height: 16),
            Row(
              children: [
                _buildStatusIcon(),
                const SizedBox(width: 16),
                Expanded(
                  child: _buildStatusInfo(),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildStatusIcon() {
    return AnimatedBuilder(
      animation: pulseAnimation,
      builder: (context, child) {
        return Transform.scale(
          scale: rfidManager.isConnected ? pulseAnimation.value : 1.0,
          child: Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: Helpers.getStatusColor(status, rfidManager.isConnected).withOpacity(0.1),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Icon(
              Helpers.getStatusIcon(status, rfidManager.isConnected),
              color: Helpers.getStatusColor(status, rfidManager.isConnected),
              size: 28,
            ),
          ),
        );
      },
    );
  }

  Widget _buildStatusInfo() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          status,
          style: const TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.w600,
          ),
        ),
        const SizedBox(height: 4),
        if (rfidManager.isConnected) ...[
          Row(
            children: [
              Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 8,
                  vertical: 4,
                ),
                decoration: BoxDecoration(
                  color: rfidManager.isMU910 
                      ? AppColors.mu910.withOpacity(0.1)
                      : AppColors.mu903.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(
                    color: rfidManager.isMU910 ? AppColors.mu910 : AppColors.mu903,
                    width: 1,
                  ),
                ),
                child: Text(
                  rfidManager.isMU910 ? 'MU910' : 'MU903',
                  style: TextStyle(
                    fontSize: 12,
                    fontWeight: FontWeight.w600,
                    color: rfidManager.isMU910 ? AppColors.mu910 : AppColors.mu903,
                  ),
                ),
              ),
              const SizedBox(width: 8),
              Text(
                '${rfidManager.baudRate} baud',
                style: TextStyle(
                  fontSize: 12,
                  color: Colors.grey[600],
                  fontWeight: FontWeight.w500,
                ),
              ),
            ],
          ),
          if (rfidManager.deviceName != null)
            Text(
              rfidManager.deviceName!,
              style: TextStyle(
                fontSize: 11,
                color: Colors.grey[500],
              ),
            ),
        ] else ...[
          Text(
            'Connect to start scanning',
            style: TextStyle(
              fontSize: 14,
              color: Colors.grey[600],
            ),
          ),
        ],
      ],
    );
  }
}