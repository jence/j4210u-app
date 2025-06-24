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
  String toString() {
    return 'EPC: $epc, Ant: $antenna, RSSI: ${rssi}dBm, Count: $count';
  }
  
  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is TagData && other.epc == epc;
  }
  
  @override
  int get hashCode => epc.hashCode;
}