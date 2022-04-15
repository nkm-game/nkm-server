## Running tests or starting app for the first time

You need to have mysql running with parameters specified in `application.conf`.

You can run mysql with `docker-compose -f .\docker-compose.dev.yml up db`,
although you should consider installing native mysql (or mariadb) for better performance (run tests a bit faster).
