import 'package:path_provider/path_provider.dart';
import 'dart:io'; 
import 'package:path/path.dart'; 
import 'package:flutter_downloader/flutter_downloader.dart';
import 'package:http/http.dart' as http;
import 'package:open_file/open_file.dart';
import 'package:permission_handler/permission_handler.dart';

Future<void> askPermissions() async {
  var status  = await PermissionHandler().checkPermissionStatus(PermissionGroup.storage);
  var isGranted = status ==PermissionStatus.granted;

  if(!isGranted) {
    await PermissionHandler().requestPermissions([PermissionGroup.storage]);
  }
}

Future<void> downloadFile(String file) async {
  await askPermissions();
  var localFilepath = await getLocalFile(file);

  var exists = await isAlreadyDownloaded(localFilepath);
  if (!exists) {
    await createDownloadTask(file, dirname(localFilepath), basename(localFilepath));
  } else {
    OpenFile.open(localFilepath);
  }
}

Future<String> getLocalFile(String file) async {
  String dir = (await getExternalStorageDirectory()).path;
  String filename = file.replaceAll(new RegExp(r'/'), '_');

  return "$dir/$filename";
}

Future<String> getRedirectUrl(String url) async {
  var client = http.Client();
  var uri = Uri.parse(url);
  var req = http.Request("GET", uri);
  req.followRedirects = false;
  var response = await client.send(req);

  if(response.statusCode == 301) {
    return response.headers['location'];
  }

  return url;
}

Future<bool> isAlreadyDownloaded(String filename) async {
  File _file = new File(filename);
  var exists = await _file.exists();
  return exists;
}

createDownloadTask(String filedownload, String folder, String filename) async {
  String dir = (await getApplicationDocumentsDirectory()).path;
  String url = "http://lambda.allocsoc.net/download/$filedownload";

  var redirectedUrl = await getRedirectUrl(url);

  return await FlutterDownloader.enqueue(
    url: redirectedUrl,
    savedDir: folder,
    fileName: filename,
    showNotification: true, // show download progress in status bar (for Android)
    openFileFromNotification: true, // click on notification to open downloaded file (for Android)
  );
}