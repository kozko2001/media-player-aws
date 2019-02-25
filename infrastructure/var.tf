
variable "name" {
  type = "string"
  default = "media-player-aws"
}

variable "tag" {
  type = "string"
  default = "Media Player AWS"
}

variable "name-suffix" {
  type   = "string"
}

variable "jar" {
  type    = "string"
  default = "../api/target/scala-2.12/MediaPlayerAwsAPI-assembly-0.1.jar"
}