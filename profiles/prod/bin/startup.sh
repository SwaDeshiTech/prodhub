#bash

echo "Setting environment variable"
./setenv.sh
export SPRING_CONFIG_LOCATION=file:$APP/conf/application.yml

echo "Running java application"
java -jar /"$APP"/prodhub-0.0.1-SNAPSHOT.jar
