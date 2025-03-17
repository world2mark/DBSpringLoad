# Java Spring Workload Processor

## assuming laptop Path
/Users/markzlamal/Documents/rosa-2025-01

## ssh-copy-jar.sh

scp -i "zlamal-key-pair.pem" /Users/markzlamal/Documents/DBSpringLoad/target/dbspringload-0.0.1-SNAPSHOT.jar ec2-user@**ip-address**:/home/ec2-user

## ssh-copy-certs.sh

scp -i "zlamal-key-pair.pem" -r zone-2 ec2-user@**ip-address**:/home/ec2-user

## ssh-to-workload.sh

ssh -i "zlamal-key-pair.pem" ec2-user@**ip-address**

## Install OpenJDK 21 on AWS Linux

sudo dnf install java-21-amazon-corretto

## Build app

./mvnw install
./mvnw clean install

## Run App

Example using zone-2 (auracoda DB server)

java -jar dbspringload-0.0.1-SNAPSHOT.jar \
--spring.datasource.url=jdbc:postgresql://zone-2.auracoda.com:26257/dbspringload \
--spring.datasource.hikari.data-source-properties.sslcert=zone-2/certs/client.root.crt \
--spring.datasource.hikari.data-source-properties.sslkey=zone-2/certs/client.root.der.key \
--spring.datasource.hikari.data-source-properties.sslrootcert=zone-2/certs/ca.crt \
--spring.datasource.hikari.maximum-pool-size=1024


## Convert crt to DER format

openssl pkcs8 -topk8 -inform PEM -outform DER -in client.root.key -out client.root.der.key -nocrypt

sudo keytool -import -alias crdb-key -keystore $JAVA_HOME/lib/security/cacerts -file zone-2/certs/server-cert.der

### Actual JAR execution
java -jar dbspringload-0.0.1-SNAPSHOT.jar \
--spring.datasource.url=jdbc:postgresql://**crdb-server.com**:26257/dbspringload \
--spring.datasource.hikari.data-source-properties.sslcert=zone-2/certs/client.root.crt \
--spring.datasource.hikari.data-source-properties.sslkey=zone-2/certs/client.root.der.key \
--spring.datasource.hikari.data-source-properties.sslrootcert=zone-2/certs/ca.crt \
--spring.datasource.hikari.maximum-pool-size=1024



## CRDB YCSB test (EC2) (~6200)

./cockroach workload init ycsb "postgresql://uid:pw@**crdb-server.com**:26257/ycsb"

./cockroach workload run ycsb --duration=6m --concurrency=8 'postgresql://uid:pw@**crdb-server.com**:26257/ycsb?sslmode=verify-full&sslrootcert=../zone-2/certs/ca.crt'

### Equivalence test (zone-2) (~4700) - zone-1 is problematic
cockroach workload run ycsb --duration=6m --concurrency=8 'postgresql://uid:pw@local-cluster-user-access.**namespace**.svc.cluster.local:26257/ycsb?sslmode=verify-full&sslrootcert=cockroach-certs/ca.crt'


## CURL Commands

curl localhost:8080/workload/populateDB?rowCount=1000

curl localhost:8080/workload/start

curl localhost:8080/workload/list

curl localhost:8080/home

curl "localhost:8080/insert-read/run-workload?duration=10&threads=1&count=-1"

## Simulating transaction retries (TODO)

https://www.cockroachlabs.com/docs/stable/transaction-retry-error-example

SET inject_retry_errors_enabled = 'true';


## Build image

2025-03-17 - WORKS!

oc new-build --binary --name dbspringload

oc start-build dbspringload --from-dir=.

oc new-app dbspringload

- Need to create the certs folder (mount + actual certs)
- Need to update the environment variables
- Apply the local LB and not the public LB
