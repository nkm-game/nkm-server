## Dev guide

### Development flow
- Frequently after some changes to the code `=>` run unit tests, they are really fast
- Before commit `=>` run all tests

> [!IMPORTANT]
> In order to run tests, a database is needed.

### Running tests or starting the app for the first time

A mysql / mariadb database needs to run with parameters specified in `application.conf`.

You can start the database with docker:
```bash
docker compose -f .\docker-compose.dev.yml up db
```

> [!Note]
> You should consider installing a native database for development for better performance.
>
> Integration tests will be slower otherwise.
