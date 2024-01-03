#!/bin/bash

echo INFO: PLEASE DO NOT RESTORE THE DB WHEN THE SERVER IS RUNNING!!!

DB_CONTAINER_NAME=nkmactorserver-db-next-1
DB_NAME=nkm_db
DB_USER=root
DB_PASSWORD=nkm_db_pass

S3_BUCKET_NAME=nkm-game-bucket

# Local directory to store the backup file
LOCAL_BACKUP_DIR="/tmp/db_backups"
mkdir -p $LOCAL_BACKUP_DIR

# Getting the latest backup file name from S3
LATEST_BACKUP_FILE=$(aws s3 ls s3://$S3_BUCKET_NAME/ | sort | tail -n 1 | awk '{print $4}')

# Download the latest backup file
aws s3 cp s3://$S3_BUCKET_NAME/$LATEST_BACKUP_FILE $LOCAL_BACKUP_DIR/$LATEST_BACKUP_FILE

# Restore the database
cat $LOCAL_BACKUP_DIR/$LATEST_BACKUP_FILE | docker exec -i $DB_CONTAINER_NAME mysql -u $DB_USER --password=$DB_PASSWORD $DB_NAME

# Optional: Remove the backup file from local system
rm $LOCAL_BACKUP_DIR/$LATEST_BACKUP_FILE

