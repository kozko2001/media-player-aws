import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import './request.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.green,
      ),
      home: new Scaffold(
        appBar: AppBar(
          title: Text("Media Content AWS"),
        ),
        body: MusicList()),
    );
  }
}

class MusicList extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => MusicListState();
}

class MusicListState extends State<MusicList> {
  var songs = new List<String>();

  _getInfo() async {
    var info = await fetchInfo(http.Client());
    setState(() {
        songs = info.files;
    });
  }

   initState() {
    super.initState();
    _getInfo();
}

  @override
  Widget build(BuildContext context) {
    return ListView.builder(
          itemCount: songs.length,
          itemBuilder: (context, index) {
            return ListTile(
              title: Text(songs[index]),
              onTap: () => print("heeeloo")
              );
          },
    );
  }
}