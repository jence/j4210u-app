import 'package:flutter/material.dart';
import 'package:j42210_dart/models/tag_data.dart';
import '../models/uhf_tag.dart';

class TagListItem extends StatelessWidget {
  final TagData tag;

  const TagListItem({Key? key, required this.tag}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: EdgeInsets.symmetric(horizontal: 16, vertical: 4),
      child: ListTile(
        leading: CircleAvatar(
          backgroundColor: Colors.blue,
          child: Text(
            tag.antenna.toString(),
            style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
          ),
        ),
        title: Text(
          tag.epc,
          style: TextStyle(fontFamily: 'monospace', fontWeight: FontWeight.bold),
        ),
        subtitle: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('RSSI: ${tag.rssi} dBm'),
            if (tag.count >0) Text('Count: ${tag.count}', style: TextStyle(fontSize: 12)),
            
            if (tag.timestamp!=null) Text('ESP32 Time: ${tag.timestamp}', style: TextStyle(fontSize: 10, color: Colors.grey)),

          ],
        ),
        trailing: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              '${tag.timestamp.hour.toString().padLeft(2, '0')}:'
              '${tag.timestamp.minute.toString().padLeft(2, '0')}:'
              '${tag.timestamp.second.toString().padLeft(2, '0')}',
              style: TextStyle(color: Colors.grey, fontSize: 12, fontWeight: FontWeight.bold),
            ),
            Text(
              '${tag.timestamp.day}/${tag.timestamp.month}',
              style: TextStyle(color: Colors.grey, fontSize: 10),
            ),
          ],
        ),
        isThreeLine: true,
      ),
    );
  }
}