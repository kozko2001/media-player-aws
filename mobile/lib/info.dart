class Info {
  final List<String> files;

  Info(this.files);

  Info.fromJson(Map<String, dynamic> json)
      : files = json['files'].cast<String>();

  Map<String, dynamic> toJson() =>
    {
      'files': files
    };
}