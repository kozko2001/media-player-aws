import 'package:http/http.dart' as http;
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import 'package:mobile/request.dart';
import 'package:mobile/info.dart';

class MockClient extends Mock implements http.Client {}

main() {
   group('fetchInfo', () {
    test('returns a Info if the http call completes successfully', () async {
      final client = MockClient();
      final json = r'''{"files":["music/lamacarena.mp3"]}''';

      when(client.get('http://lambda.allocsoc.net/info.json'))
          .thenAnswer((_) async => http.Response(json, 200));

      expect(await fetchInfo(client), isInstanceOf<Info>());
    });

    test('throws an exception if the http call completes with an error', () {
      final client = MockClient();

      when(client.get('http://lambda.allocsoc.net/info.json'))
          .thenAnswer((_) async => http.Response('Not Found', 404));

      expect(fetchInfo(client), throwsException);
    });
  });
}
