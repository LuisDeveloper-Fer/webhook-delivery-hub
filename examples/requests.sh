#!/usr/bin/env bash
set -euo pipefail
BASE="${BASE:-http://localhost:8080}"
curl -i -X POST "$BASE/api/events" -H 'Content-Type: application/json' --data '{"type":"payment.approved","message":"fictional-payment"}'
curl -i "$BASE/api/deliveries"
