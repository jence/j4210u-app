import 'package:flutter/material.dart';
import 'colors.dart';

class Helpers {
  static Color getRssiColor(int rssi) {
    if (rssi >= -30) return AppColors.rssiExcellent;
    if (rssi >= -50) return AppColors.rssiGood;
    if (rssi >= -70) return AppColors.rssiFair;
    return AppColors.rssiPoor;
  }
  
    static String getRssiStrength(int rssi) {
    if (rssi >= -50) {
      return 'Excellent';
    } else if (rssi >= -60) {
      return 'Good';
    } else if (rssi >= -70) {
      return 'Fair';
    } else if (rssi >= -80) {
      return 'Poor';
    } else {
      return 'Very Poor';
    }
  }

    static String formatEpc(String epc) {
    if (epc.length <= 8) return epc;
    
    // Add space every 4 characters for better readability
    StringBuffer formatted = StringBuffer();
    for (int i = 0; i < epc.length; i += 4) {
      if (i > 0) formatted.write(' ');
      int end = (i + 4 < epc.length) ? i + 4 : epc.length;
      formatted.write(epc.substring(i, end));
    }
    return formatted.toString();
  }

  static int getEpcByteLength(String epc) {
    return (epc.length / 2).floor();
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
  
  // static String formatTime(DateTime dateTime) {
  //   return '${dateTime.hour.toString().padLeft(2, '0')}:${dateTime.minute.toString().padLeft(2, '0')}';
  // }
  
  static String formatLogTime(DateTime dateTime) {
    return dateTime.toString().substring(11, 19);
  }
  

  static String formatTime(DateTime dateTime) {
    return '${dateTime.hour.toString().padLeft(2, '0')}:'
           '${dateTime.minute.toString().padLeft(2, '0')}:'
           '${dateTime.second.toString().padLeft(2, '0')}';
  }
  
  /// Format date for display
  static String formatDate(DateTime dateTime) {
    return '${dateTime.day}/${dateTime.month}/${dateTime.year}';
  }
  
  /// Get antenna color (you can customize this based on antenna number)
  static Color getAntennaColor(int antenna) {
    // Different colors for different antennas
    List<Color> antennaColors = [
      AppColors.antenna,
      Color(0xFF2196F3), // Blue
      Color(0xFFE91E63), // Pink
      Color(0xFF00BCD4), // Cyan
    ];
    
    return antennaColors[antenna % antennaColors.length];
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


