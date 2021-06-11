# OpenMediation SubCenter Task

OM-SC-Task is a server for sub center redis control.

**Before started, you should download and run [OM-Dtask](https://github.com/AdTiming/OM-Dtask) first.**

## Usage

### Packaging

You can package using [mvn](https://maven.apache.org/).

```
mvn clean package -Dmaven.test.skip=true
```

After packaging is complete, you can find "om-sc-task.jar" in the directory "target".  
"om-server.jar" is a executable jar, see [springboot](https://spring.io/projects/spring-boot/)

### Configuration

"om-sc-task.conf"

```shell script
## springboot cfg ###
MODE=service
APP_NAME=om-sc-task
#JAVA_HOME=/usr/local/java/jdk
JAVA_OPTS="-Dapp=$APP_NAME\
 -Dapp.dcenter=1\
 -Dapp.dtask=replace_dtask_host\
 -Duser.timezone=UTC\
 -Xmx5g\
 -Xms2g\
 -server"

RUN_ARGS="--spring.profiles.active=prod"
PID_FOLDER=log
LOG_FOLDER=log
LOG_FILENAME=stdout.log
```

### Run

put "om-server.conf" and "om-server.jar" in the same directory.

```
├── on-server.conf
├── on-server.jar
└── log
```

```shell script
mkdir -p log
./om-sc-task.jar start
```

### Logs

```shell script
tail -f log/stdout.log
```

### Stop

```shell script
./om-sc-task.jar stop
```

### Restart

```shell script
./om-sc-task.jar restart
```


