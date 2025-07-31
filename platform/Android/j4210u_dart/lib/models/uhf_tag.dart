import 'dart:convert';

class UHFTag {
  final String epc;
  final String rssi;
  final int? antenna;
  final int? count;
  final int? epcLength;
  final String? timestamp;
  final String? tid;
  final String? user;
  final DateTime detectedTime;

  UHFTag({
    required this.epc,
    required this.rssi,
    this.antenna,
    this.count,
    this.epcLength,
    this.timestamp,
    this.tid,
    this.user,
    required this.detectedTime,
  });

  factory UHFTag.fromJson(Map<String, dynamic> json) {
    return UHFTag(
      epc: json['EPC'] ?? json['epc'] ?? '',
      rssi: json['RSSI']?.toString() ?? json['rssi']?.toString() ?? '0',
      antenna: json['ant'] ?? json['antenna'],
      count: json['count'],
      epcLength: json['epclen'] ?? json['epcLength'],
      timestamp: json['Timestamp'] ?? json['timestamp'],
      tid: json['TID'] ?? json['tid'],
      user: json['USER'] ?? json['user'],
      detectedTime: DateTime.now(),
    );
  }

  factory UHFTag.fromJsonString(String jsonString) {
    Map<String, dynamic> json = jsonDecode(jsonString);
    return UHFTag.fromJson(json);
  }

  @override
  String toString() {
    return 'EPC: $epc, RSSI: $rssi dBm';
  }
}