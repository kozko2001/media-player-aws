import 'package:http/http.dart' as http;
import 'package:mobile/info.dart';
import 'dart:convert';

Future<Info> fetchInfo(http.Client client) async {
  final response =
      await client.get('http://lambda.allocsoc.net/info.json');

  if (response.statusCode == 200) {
    return Info.fromJson(json.decode(response.body));
  } else {
    throw Exception('Failed to load post');
  }
}
