class Info {
  final List<dynamic> files;

  Info(this.files);

  Info.fromJson(Map<String, dynamic> json)
      : files = json['files'];

  Map<String, dynamic> toJson() =>
    {
      'files': files
    };
}
