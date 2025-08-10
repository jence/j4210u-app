
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'screens/rfid_reader_screen.dart';
import 'theme/app_theme.dart';

void main() {


  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'UHF RFID Reader',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.lightTheme,
      home: const RFIDReaderScreen(),
    );
  }
}