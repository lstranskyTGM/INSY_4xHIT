# WS04_M1

Verfasser: **Leonhard Stransky, 4AHIT**

Datum: **02.12.2023**

## Projektbeschreibung:

Es sollen NoSQL Datenbanksysteme getestet werden.

2 Datenbanktypen aus den folgenden auswählen:
- Key-Value-Datenbanken 
- Spaltenorientierte Datenbanken 
- Multi-Value-Datenbanken 
- Multi-Modell-Datenbank 
- Wide Table Datenbanken 

## InfluxDB:

### InfluxDB Overview
InfluxDB is a specialized open-source time series database developed by InfluxData. It's well-suited for storing and querying time-stamped data such as metrics, events, and analytics, making it ideal for monitoring systems, IoT applications, and real-time analytics.

### Key Features
1. Time Series Data: Optimized for handling and fast querying of time-stamped data.
2. Scalability: Suitable for high performance and large data volumes.
3. Data Model: Combines tags (for efficient querying) and fields (for data values).
4. InfluxQL: A SQL-like query language specifically designed for time series data.
5. Retention Policies: Automatic management of the data lifecycle.
6. Native HTTP API: Facilitates easy integration with various programming environments.

### Placement in NoSQL Systems
InfluxDB is uniquely positioned as a specialized time series database within the NoSQL landscape. It doesn't fit neatly into traditional NoSQL categories but shows similarities with Wide-Table databases due to its handling of large, variable data volumes. InfluxDB's specialization in time series data sets it apart from generic NoSQL database types.

## Creating Container:

```bash
# Docker pull:
docker pull influxdb:2.0

# Docker run:
docker run -d --name=influxdb2 \
    -p 8086:8086 \
    # -v influxdb_data:/var/lib/influxdb2 \
    # -v $PWD/config:/etc/influxdb2 \
    # -e DOCKER_INFLUXDB_INIT_MODE=setup \
    # -e DOCKER_INFLUXDB_INIT_USERNAME=my-user \
    # -e DOCKER_INFLUXDB_INIT_PASSWORD=my-password \
    # -e DOCKER_INFLUXDB_INIT_ORG=my-org \
    # -e DOCKER_INFLUXDB_INIT_BUCKET=my-bucket \
    influxdb:2.0

# user: lstransky
# password: influxdbroot
# reset password: influx user password -n [name] -t [token]
```

## Adding Data:

### InfluxDB CLI:

```bash
# Bash
docker exec -it influxdb2 bash
```

```bash
# In the influxDB cli

use my-bucket
insert myMeasurement,tagKey=tagValue fieldKey="fieldValue" 1652123456
```

### HTTP API with cURL:

```bash
curl -i -XPOST 'http://localhost:8086/api/v2/write?org=my-org&bucket=my-bucket&precision=s' \
--header 'Authorization: Token YOURTOKEN' \
--data-raw 'myMeasurement,tagKey=tagValue fieldKey=fieldValue 1652123456' 
```

### Library in Python:

```py
from influxdb_client import InfluxDBClient, Point, WriteOptions

# Function to create an InfluxDB client
def create_influxdb_client(url, token, org):
    return InfluxDBClient(url=url, token=token, org=org)

# Function to write data to InfluxDB
def write_data_to_influxdb(client, bucket, measurement, tags, fields):
    write_api = client.write_api(write_options=WriteOptions(batch_size=1))
    point = Point(measurement)
    for tag_key, tag_value in tags.items():
        point.tag(tag_key, tag_value)
    for field_key, field_value in fields.items():
        point.field(field_key, field_value)
    write_api.write(bucket=bucket, record=point)
    write_api.close()

# Example usage
if __name__ == '__main__':
    INFLUXDB_URL = "http://localhost:8086"
    INFLUXDB_TOKEN = "YOURTOKEN"
    INFLUXDB_ORG = "my-org"
    INFLUXDB_BUCKET = "my-bucket"

    # Create a client
    client = create_influxdb_client(INFLUXDB_URL, INFLUXDB_TOKEN, INFLUXDB_ORG)

    # Example data
    measurement = "myMeasurement"
    tags = {
        "tagKey": "tagValue"
    }
    fields = {
        "fieldKey": "fieldValue"
    }

    # Write data
    write_data_to_influxdb(client, INFLUXDB_BUCKET, measurement, tags, fields)
```

### Telegraf:

Telegraf is an open-source server agent optimized for efficiency and ease of use. It's designed for:
- Data Collection: Gathers metrics and events from databases, systems, IoT sensors.
- Plugins: Offers extensive plugins for various data formats and destinations.
- Uses: Ideal for monitoring and real-time analytics.
- Functionality: Enables seamless data aggregation and transformation.

```toml
# Global Agent Configuration
[agent]
    # Collect data every 30 seconds
    interval = "30s"                  
    # Round collection times to the nearest interval
    round_interval = true             
    # Number of metrics to send at once
    metric_batch_size = 1000          
    # Maximum number of metrics in buffer
    metric_buffer_limit = 10000       
    # Interval for flushing metrics to the output
    flush_interval = "30s"            

# Input Plugins - CPU and Memory

## CPU Input Plugin - Collects basic CPU metrics
[[inputs.cpu]]
    # Collect aggregate CPU metrics only
    percpu = false                   

## Memory Input Plugin - Collects basic memory metrics
[[inputs.mem]]
    # Default settings are sufficient

# Output Plugin - InfluxDB v2.x

## InfluxDB v2.x Output Plugin - Where to send metrics
[[outputs.influxdb_v2]]
    # InfluxDB URL
    urls = ["http://localhost:8086"]  
    # Authentication token
    token = "YOURTOKEN"               
    # InfluxDB organization
    organization = "my-org"           
    # Destination bucket
    bucket = "my-bucket"              
```

```yaml
version: '3'

services:
  influxdb:
    image: influxdb:2.6-alpine
    env_file:
      - influxv2.env
    volumes:
      # Mount for influxdb data directory and configuration
      - influxdbv2:/var/lib/influxdb2:rw
    ports:
      - "8086:8086"
  telegraf:
    image: telegraf:1.25-alpine
    depends_on:
      - influxdb
    volumes:
      # Mount for telegraf config
      - ./telegraf/mytelegraf.conf:/etc/telegraf/telegraf.conf:ro
    env_file:
      - influxv2.env

volumes:
  influxdbv2:
```

### Arduino:

```c++
#include <InfluxDbClient.h>
#include <InfluxDbCloud.h>

// InfluxDB server URL, e.g., http://localhost:8086
const char* INFLUXDB_URL = "http://<host-ip>:8086";
// InfluxDB database name
const char* INFLUXDB_DB_NAME = "your_database";
// InfluxDB authentication token
const char* INFLUXDB_TOKEN = "your_token";
// InfluxDB organization
const char* INFLUXDB_ORG = "your_org";
// InfluxDB bucket
const char* INFLUXDB_BUCKET = "your_bucket";

// WiFi credentials
const char* WIFI_SSID = "your_ssid";
const char* WIFI_PASSWORD = "your_password";

// Define InfluxDB client instance
InfluxDBClient client(INFLUXDB_URL, INFLUXDB_ORG, INFLUXDB_BUCKET, INFLUXDB_TOKEN);

void setup() {
  // Start serial communication
  Serial.begin(115200);

  // Connect to WiFi
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  while (WiFi.status() != WL_CONNECTED) {
    delay(100);
    Serial.print(".");
  }
  Serial.println("Connected to WiFi");

  // Set InfluxDB client parameters
  client.setWriteOptions(WriteOptions().writePrecision(WritePrecision::S));
}

void loop() {
  // Create a point with measurement name
  Point sensor("weather");

  // Get some data (replace these with actual sensor readings)
  float temperature = 24.5; // Example temperature reading
  float humidity = 48.0;     // Example humidity reading

  // Add fields to the point
  sensor.addField("temperature", temperature);
  sensor.addField("humidity", humidity);

  // Write point to InfluxDB
  if (!client.writePoint(sensor)) {
    Serial.print("InfluxDB write failed: ");
    Serial.println(client.getLastErrorMessage());
  }

  // Wait 10 seconds before sending next point
  delay(10000);
}
```

## Uploading Template:

Github:
https://github.com/influxdata/community-templates#templates

```bash
docker exec -it influxdb2 influx apply --file https://raw.githubusercontent.com/influxdata/community-templates/master/earthquake_usgs/earthquake_usgs_template.yml --org your_org_name --token YourAuthToken
```

## Quellen:

[1] Docker Image: https://hub.docker.com/_/influxdb 

[2] Influxdb Website: https://www.influxdata.com/ 

[3] Github Templates: https://github.com/influxdata/community-templates#templates

[4] Earthquakes_Sample: https://raw.githubusercontent.com/influxdata/community-templates/master/earthquake_usgs/earthquake_usgs_template.yml 












