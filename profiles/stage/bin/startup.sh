#bash

echo "Setting environment variable"
./setenv.sh

echo "Running java application"
java -jar /"$APP"/prodhub-0.0.1-SNAPSHOT.jar
