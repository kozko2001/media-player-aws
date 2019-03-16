import 'package:test/test.dart';
import 'package:mobile/info.dart';
import 'dart:convert';

void main() {
  test('parse response info.json', () {
    final String jsonString = r'''{"files":["music/lamacarena.mp3"]}''';
    Map infoMap = jsonDecode(jsonString);
    var info = new Info.fromJson(infoMap);

    expect(info.files[0], "music/lamacarena.mp3");
  });
}