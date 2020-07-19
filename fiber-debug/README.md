# Fiber Debug Project

### Build

```shell script
mvn clean install docker:build
```

### Run

```shell script
docker run --rm -p 1337:1337 -p 5005:5005 graversen.io/fiber-debug:0.0.1
```