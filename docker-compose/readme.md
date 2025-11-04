### Контейнер с Kafka

1. Requires [docker](https://docs.docker.com/get-docker/) and [compose](https://docs.docker.com/compose/install/)
2. Parameterized using variables in the [`.env`](.env) file (тут его нет)
3. Up the project using command:
```
docker compose up -d
```
down:
```
docker compose down -v
```

### Что такое nginx?
`Nginx` (произносится как `engine-x`) — обратный прокси-сервер с открытым исходным кодом для протоколов HTTP, HTTPS, SMTP, POP3 и IMAP, а также балансировщик нагрузки, HTTP-кэш и веб-сервер (исходный сервер). Проект nginx изначально был ориентирован на высокую степень параллелизма, производительность и низкое потребление памяти. Nginx лицензируется по двухпунктной лицензии, аналогичной BSD, и работает на Linux, вариантах BSD, Mac OS X, Solaris, AIX, HP-UX, а также на других дистрибутивах *nix. Также существует портированная версия для Microsoft Windows.
https://hub.docker.com/_/nginx

