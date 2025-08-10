import 'dart:convert';

class TagData {
  final String epc;
  final int antenna;
  final int rssi;
  final int count;
  final DateTime timestamp;
  
  TagData({
    required this.epc,
    required this.antenna,
    required this.rssi,
    required this.count,
    required this.timestamp,
  });
  
  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is TagData && other.epc == epc;
  }
  
  @override
  int get hashCode => epc.hashCode;

  factory TagData.fromJson(Map<String, dynamic> json) {
    String rssiString = json['RSSI']?.toString() ?? json['rssi']?.toString() ?? '0';
    
    // Handle negative RSSI values properly
    int rssiValue;
    try {
      rssiValue = int.parse(rssiString.replaceAll('dBm', '').trim());
    } catch (e) {
      print('⚠️ [TagData] Failed to parse RSSI: $rssiString, using default -50');
      rssiValue = -50;
    }

    return TagData(
      epc: json['EPC'] ?? json['epc'] ?? '',
      rssi: rssiValue,
      antenna: json['ant'] ?? json['antenna'] ?? 1,
      count: json['count'] ?? 1,
      timestamp: DateTime.now(), // Use current time as detection time
    );
  }

  factory TagData.fromJsonString(String jsonString) {
    Map<String, dynamic> json = jsonDecode(jsonString);
    return TagData.fromJson(json);
  }
  
  @override
  String toString() {
    return 'EPC: $epc, Ant: $antenna, RSSI: ${rssi}dBm, Count: $count';
  }
}