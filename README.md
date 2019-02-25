# media-player-aws

### Api

```
cd api/

sbt test

sbt assembly
```

### Infrastructe

media-player-aws uses some infrastructure to work in the cloud, for provising the infrastructure we use terraform

```
cd infrastructure/

terraform init

terraform plan

terraform apply
```


### Emacs

1. Install `ensime` as a plugin

```
cat < <EOF > ~/.sbt/1.0/plugins/plugins.sbt
addSbtPlugin("org.ensime" % "sbt-ensime" % "2.5.1")
EOF
```

2. Install emacs ensime plugin
