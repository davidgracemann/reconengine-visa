# Tools

This directory contains utility tools for development and monitoring.

## AKHQ - Kafka Web UI

AKHQ is a lightweight web UI for managing and monitoring Kafka clusters.

### Setup

1. Ensure your Kafka broker is running locally on `localhost:9092`.

> run the following command in your terminal for kafka server

```bash
bin\windows\kafka-server-start.bat config\server.properties
```

2. Run AKHQ with:

    ```bash
    cd tools
    java \
    -Dmicronaut.config.files=./application.yml \
    -Dmicronaut.server.port=8081 \
    -jar akhq-0.25.1-all.jar
    ```

3. Access the web UI at: [http://localhost:8081](http://localhost:8081)

### Features

-   Browse Kafka topics, partitions, and messages.
-   View consumer groups and lag.
-   Produce and consume messages interactively.
-   Monitor cluster health and configurations.

### Notes

-   Change the `--server.port` if `8081` is occupied.
-   This tool is for development and monitoring only; do not use in production without proper security.

---

Place any other dev utilities or monitoring tools here for easy access.

````

Let me know if you want it more detailed or tailored!

```

```
````
