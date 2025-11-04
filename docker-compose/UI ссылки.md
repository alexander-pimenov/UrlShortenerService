## Ссылки для доступа

### Kafka UI
http://localhost:8078/

### DBeaver Postgres
url: jdbc:postgresql://${POSTGRES_HOST:localhost:6432}/${POSTGRES_DB:nsix}?currentSchema=${POSTGRES_DB_SCHEMA:nsix}&prepareThreshold=0&autoReconnect=true&targetServerType=master
username: ${POSTGRES_USER:postgres}
password: ${POSTGRES_PASSWORD:postgres}
