#!/bin/sh
set -e

REGION=af-south-1
HOSTED_ZONE_ID="${HOSTED_ZONE_ID}"
RECORD_NAME=api.rwars.steven-webber.com.

IP=$(curl -sf http://169.254.169.254/latest/meta-data/public-ipv4)
if [ -z "$IP" ]; then
  echo "Could not determine public IP from instance metadata" >&2
  exit 1
fi

echo "Updating ${RECORD_NAME} to ${IP}"

aws route53 change-resource-record-sets \
  --region "$REGION" \
  --hosted-zone-id "$HOSTED_ZONE_ID" \
  --change-batch "{\"Changes\":[{\"Action\":\"UPSERT\",\"ResourceRecordSet\":{\"Name\":\"${RECORD_NAME}\",\"Type\":\"A\",\"TTL\":60,\"ResourceRecords\":[{\"Value\":\"${IP}\"}]}}]}"
