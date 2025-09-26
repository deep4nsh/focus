import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() => runApp(FocusApp());

class FocusApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Focus',
      theme: ThemeData(primarySwatch: Colors.blue),
      home: StudyModeHome(),
    );
  }
}

class StudyModeHome extends StatefulWidget {
  @override
  _StudyModeHomeState createState() => _StudyModeHomeState();
}

class _StudyModeHomeState extends State<StudyModeHome> {
  static const platform = MethodChannel('focus.study.mode');
  bool _isStudyModeOn = false;

  void _toggleStudyMode(bool newValue) async {
    setState(() => _isStudyModeOn = newValue);
    try {
      await platform.invokeMethod('toggleStudyMode', {'on': newValue});
    } on PlatformException catch (e) {
      print("Failed to toggle study mode: ${e.message}");
    }
  }

  Future<void> _requestUsageAccess() async {
    try {
      await platform.invokeMethod('requestUsageAccess');
    } on PlatformException catch (e) {
      print("Failed to open usage access settings: ${e.message}");
    }
  }

  Future<void> _requestOverlayPermission() async {
    try {
      await platform.invokeMethod('requestOverlayPermission');
    } on PlatformException catch (e) {
      print("Failed to open overlay permission settings: ${e.message}");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Focus - Study Mode')),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            ElevatedButton(
              onPressed: _requestUsageAccess,
              child: Text('Grant Usage Access'),
            ),
            SizedBox(height: 10),
            ElevatedButton(
              onPressed: _requestOverlayPermission,
              child: Text('Grant Overlay Permission'),
            ),
            SizedBox(height: 30),
            Text(
              'Study Mode is ${_isStudyModeOn ? "ON" : "OFF"}',
              style: TextStyle(fontSize: 24),
            ),
            Switch(
              value: _isStudyModeOn,
              onChanged: _toggleStudyMode,
              activeColor: Colors.blue,
              inactiveThumbColor: Colors.grey,
            ),
          ],
        ),
      ),
    );
  }
}