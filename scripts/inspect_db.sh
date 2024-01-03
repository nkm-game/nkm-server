#!/bin/bash

DB_CONTAINER_NAME=nkmactorserver-db-next-1
DB_NAME=nkm_db
DB_USER=root
DB_PASSWORD=nkm_db_pass

docker exec -it $DB_CONTAINER_NAME mysql -u $DB_USER --password=$DB_PASSWORD $DB_NAME

