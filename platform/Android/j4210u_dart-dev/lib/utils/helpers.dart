import 'package:flutter/material.dart';
import 'colors.dart';

class Helpers {
  static Color getRssiColor(int rssi) {
    if (rssi >= -30) return AppColors.rssiExcellent;
    if (rssi >= -50) return AppColors.rssiGood;
    if (rssi >= -70) return AppColors.rssiFair;
    return AppColors.rssiPoor;
  }
  
  static Color getStatusColor(String status, bool isConnected) {
    if (isConnected) return AppColors.success;
    if (status.contains('Error') || status.contains('failed')) return AppColors.error;
    if (status.contains('Connecting') || status.contains('Scanning')) return AppColors.warning;
    return AppColors.inactive;
  }
  
  static IconData getStatusIcon(String status, bool isConnected) {
    if (isConnected) return Icons.check_circle;
    if (status.contains('Error') || status.contains('failed')) return Icons.error;
    if (status.contains('Connecting') || status.contains('Scanning')) return Icons.sync;
    return Icons.radio_button_unchecked;
  }
  
  static String formatTime(DateTime dateTime) {
    return '${dateTime.hour.toString().padLeft(2, '0')}:${dateTime.minute.toString().padLeft(2, '0')}';
  }
  
  static String formatLogTime(DateTime dateTime) {
    return dateTime.toString().substring(11, 19);
  }
  
  static void showSnackBar(BuildContext context, String message, {bool isError = false}) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Row(
          children: [
            Icon(
              isError ? Icons.error : Icons.check_circle,
              color: Colors.white,
              size: 20,
            ),
            const SizedBox(width: 12),
            Expanded(child: Text(message)),
          ],
        ),
        backgroundColor: isError ? AppColors.error : AppColors.success,
        behavior: SnackBarBehavior.floating,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        margin: const EdgeInsets.all(16),
      ),
    );
  }
}