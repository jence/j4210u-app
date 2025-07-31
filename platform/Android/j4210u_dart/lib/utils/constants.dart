class AppConstants {
  static const String appTitle = 'UHF RFID Reader';
  
  // Tab labels
  static const String tagsTab = 'Tags';
  static const String readerInfoTab = 'Reader Info';
  static const String logsTab = 'Logs';
  
  // Connection statuses
  static const String disconnected = 'Disconnected';
  static const String connecting = 'Connecting...';
  static const String connected = 'Connected';
  static const String connectionFailed = 'Connection failed';
  static const String scanning = 'Scanning...';
  static const String noTagsFound = 'No tags found';
  
  // Button labels
  static const String connect = 'CONNECT';
  static const String disconnect = 'DISCONNECT';
  static const String scanTags = 'SCAN TAGS';
  static const String scanning_ = 'SCANNING...';
  static const String getInfo = 'GET INFO';
  static const String clearLogs = 'CLEAR LOGS';
  
  // Other constants
  static const int maxLogs = 100;
  static const int animationDuration = 1500;
  static const int pulseAnimationDuration = 2000;
}