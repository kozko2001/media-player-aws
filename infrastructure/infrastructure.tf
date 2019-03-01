provider "aws" {
  region = "eu-west-1"
}


resource "aws_s3_bucket" "bucket" {
  bucket = "${var.name}-${var.name-suffix}"
  acl    = "private"

  tags = {
    stack = "${var.tag}"
  }
}

resource "aws_s3_bucket" "page-bucket" {
  bucket = "${var.name-suffix}mediaplayeraws.allocsoc.net"
  acl    = "public-read"
  website {
    index_document = "index.html"
    error_document = "error.html"
  }
  policy = <<POLICY
{
  "Version":"2012-10-17",
  "Statement":[
    {
      "Sid":"AddPerm",
      "Effect":"Allow",
      "Principal": "*",
      "Action":["s3:GetObject"],
      "Resource":["arn:aws:s3:::kzkmediaplayeraws.allocsoc.net/*"]
    }
  ]
}
POLICY
}

resource "aws_s3_bucket_object" "index_js" {
  key        = "index.html"
  bucket     = "${aws_s3_bucket.page-bucket.id}"
  source     = "../web/index.html"
  content_disposition = "inline"
  content_type = "text/html; charset=utf-8"
}


## lambda
data "aws_iam_policy_document" "policy" {
  statement {
    sid    = ""
    effect = "Allow"

    principals {
      identifiers = ["lambda.amazonaws.com"]
      type        = "Service"
    }

    actions = ["sts:AssumeRole"]
  }
}


## give lambda permissions
resource "aws_lambda_permission" "iam_for_alb" {
  statement_id  = "AllowExecutionFromlb"
  action        = "lambda:InvokeFunction"
  function_name = "${aws_lambda_function.lambda.arn}"
  principal     = "elasticloadbalancing.amazonaws.com"
  source_arn    = "${aws_lb_target_group.lambda-target-group.arn}"
}



resource "aws_iam_role" "iam_for_lambda" {
  name               = "iam_for_lambda"
  assume_role_policy = "${data.aws_iam_policy_document.policy.json}"
}

resource "aws_lambda_function" "lambda" {
   function_name = "${var.name}-${var.name-suffix}-lambda"

   filename         = "${var.jar}"
   source_code_hash = "${base64sha256(file(var.jar))}"

   role    = "${aws_iam_role.iam_for_lambda.arn}"
   handler = "net.allocsoc.mediaplayeraws.Handler::handleRequest"

   runtime = "java8"
   timeout = 30
   memory_size = 256
}

## lambda logging

resource "aws_cloudwatch_log_group" "example" {
  name              = "/aws/lambda/${aws_lambda_function.lambda.function_name}"
  retention_in_days = 14
}

resource "aws_iam_policy" "lambda_logging" {
  name = "lambda_logging"
  path = "/"
  description = "IAM policy for logging from a lambda"

  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": [
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:PutLogEvents"
      ],
      "Resource": "arn:aws:logs:*:*:*",
      "Effect": "Allow"
    }
  ]
}
EOF
}

resource "aws_iam_role_policy_attachment" "lambda_logs" {
  role = "${aws_iam_role.iam_for_lambda.name}"
  policy_arn = "${aws_iam_policy.lambda_logging.arn}"
}

## Lambda access to s3
resource "aws_iam_policy" "lambda_s3" {
  name = "lambda_s3"
  path = "/"
  description = "IAM policy for access s3 from media-player lambda"

  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": [
        "s3:*"
      ],
      "Resource": "${aws_s3_bucket.bucket.arn}*",
      "Effect": "Allow"
    }
  ]
}
EOF
}


resource "aws_iam_role_policy_attachment" "lambda_s3" {
  role = "${aws_iam_role.iam_for_lambda.name}"
  policy_arn = "${aws_iam_policy.lambda_s3.arn}"
}

## subnets and vpc default so we don't create new ones

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


#### Application Load Balancer

resource "aws_lb" "lambda-alb" {
  name               = "${var.name}-${var.name-suffix}-lb"
  internal           = false
  load_balancer_type = "application"
  subnets            = ["${aws_default_subnet.default_subnet_az1.id}", "${aws_default_subnet.default_subnet_az2.id}"]
  security_groups    = ["${aws_security_group.alb_sg.id}"]
}

resource "aws_security_group" "alb_sg" {
  name        = "${var.name}-${var.name-suffix}-alb_sg"
  description = "Load Balancer port 80"

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port       = 0
    to_port         = 0
    protocol        = "-1"
    cidr_blocks     = ["0.0.0.0/0"]
  }
}

resource "aws_lb_listener" "alb_listener" {
  load_balancer_arn = "${aws_lb.lambda-alb.arn}"
  port              = "80"
  protocol          = "HTTP"

  default_action {
    target_group_arn = "${aws_lb_target_group.lambda-target-group.arn}"
    type             = "forward"
  }
}

resource "aws_lb_target_group" "lambda-target-group" {
  name        = "${var.name}-alb-group"
  target_type = "lambda"
}

resource "aws_lb_target_group_attachment" "lamdba-target-attachment" {
  target_group_arn  = "${aws_lb_target_group.lambda-target-group.arn}"
  target_id         = "${aws_lambda_function.lambda.arn}"
  depends_on = ["aws_lambda_permission.iam_for_alb"]
}
