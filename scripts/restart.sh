#!/bin/bash

set -e

TOKEN_ADMIN="c6fc6a59033d1144fec5a565cb2b6796"
ADDRESS="http://localhost:30251"
NAMESPACE=default-1
DEPLOY=jenkins

echo "Restarting jenkins..."
curl -v -XPOST $ADDRESS/safeRestart -u 'admin:'$TOKEN_ADMIN
kubectl logs -f --tail=50 -n $NAMESPACE deploy/$DEPLOY
echo "===================================================="
echo "Jenkins restarted... tailling logs again..."
echo "===================================================="
sleep 2
kubectl logs -f --tail=100 -n $NAMESPACE deploy/$DEPLOY
