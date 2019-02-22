provider "aws" {
  region = "eu-west-1"
}

resource "aws_s3_bucket" "b" {
  bucket = "kzk-media-player-aws"
  acl    = "private"

  tags = {
    Name        = "Media Player AWS"
  }
}


resource "aws_iam_role" "iam_for_lambda" {
  name = "iam_for_lambda"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF
}

resource "aws_lambda_function" "test_lambda" {
  filename         = "payload.zip"
  function_name    = "lambda_function_name"
  role             = "${aws_iam_role.iam_for_lambda.arn}"
  handler          = "index.handler"
  source_code_hash = "${base64sha256(file("payload.zip"))}"
  runtime          = "nodejs8.10"

  environment {
    variables = {
    }
  }
}

resource "aws_lb" "media-player-aws-lb" {
  name               = "media-player-aws-lb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = ["${aws_security_group.lb_sg.id}"]
  subnets            = ["${aws_default_subnet.default_subnet_az1.id}", "${aws_default_subnet.default_subnet_az2.id}"]

  enable_deletion_protection = true

  tags = {

  }
}


resource "aws_security_group" "lb_sg" {
  name        = "allow_all"
  description = "Allow all inbound traffic"

  ingress {
    from_port   = 0
    to_port     = 65535
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "allow_all"
  }
}



resource "aws_default_subnet" "default_subnet_az1" {
  availability_zone = "eu-west-1a"

  tags = {
    Name = "Default subnet 1"
  }
}

resource "aws_default_subnet" "default_subnet_az2" {
  availability_zone = "eu-west-1b"

  tags = {
    Name = "Default subnet 2"
  }
}


resource "aws_default_vpc" "default" {
  tags = {
    Name = "Default VPC"
  }
}
