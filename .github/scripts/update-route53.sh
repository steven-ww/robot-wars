#!/bin/sh
set -e

REGION=af-south-1
HOSTED_ZONE_ID="${HOSTED_ZONE_ID}"
RECORD_NAME=api.rwars.steven-webber.com.

# Prefer IMDSv2 (token required); fall back to IMDSv1 if tokens are disabled.
TOKEN=$(curl -sf -X PUT "http://169.254.169.254/latest/api/token" \
  -H "X-aws-ec2-metadata-token-ttl-seconds: 300" || true)
if [ -n "$TOKEN" ]; then
  IP=$(curl -sf -H "X-aws-ec2-metadata-token: $TOKEN" \
    http://169.254.169.254/latest/meta-data/public-ipv4 || true)
else
  IP=$(curl -sf http://169.254.169.254/latest/meta-data/public-ipv4 || true)
fi

if [ -z "$IP" ]; then
  echo "Could not determine public IP from instance metadata" >&2
  exit 1
fi

echo "Updating ${RECORD_NAME} to ${IP}"

aws route53 change-resource-record-sets \
  --region "$REGION" \
  --hosted-zone-id "$HOSTED_ZONE_ID" \
  --change-batch "{\"Changes\":[{\"Action\":\"UPSERT\",\"ResourceRecordSet\":{\"Name\":\"${RECORD_NAME}\",\"Type\":\"A\",\"TTL\":60,\"ResourceRecords\":[{\"Value\":\"${IP}\"}]}}]}"
