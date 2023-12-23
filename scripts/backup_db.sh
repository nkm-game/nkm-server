#!/bin/bash

DB_CONTAINER_NAME=nkmactorserver-db-next-1
DB_NAME=nkm_db
DB_USER=root
DB_PASSWORD=nkm_db_pass

S3_BUCKET_NAME=nkm-game-bucket

BACKUP_FILENAME="backup_$(date +'%Y%m%d_%H%M%S').sql"

docker exec $DB_CONTAINER_NAME /usr/bin/mysqldump -u $DB_USER --password=$DB_PASSWORD $DB_NAME > $BACKUP_FILENAME

/root/.local/bin/aws s3 cp $BACKUP_FILENAME s3://$S3_BUCKET_NAME/$BACKUP_FILENAME

rm $BACKUP_FILENAME

