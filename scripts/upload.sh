#!/bin/sh
# current script needs env.sh to setup some environment variables, see also example file below:
# export JENKINS_USER=admin
# export JENKINS_TOKEN=11aa342e9a392f64cc7c56548e17fe6d09
# export JENKINS_URL=http://jenkins.com
#
# export REMOTE_USER=193151e7-48e9-402c-a608-a10301a29a72
# export REMOTE_TOKEN=274e8a79536e8a34f596a5a5d17d3e46
# export REMOTE_URL=http://jenkins.cn

if [ "$JENKINS_USER" == "" ]; then
    export JENKINS_USER=admin
fi

if [ ! -f "$(dirname "${BASH_SOURCE[0]}")/env.sh" ]; then
    echo 'we need the env.sh to setup vars'
    exit -1
fi

source $(dirname "${BASH_SOURCE[0]}")/env.sh

issuer=$(curl -k -u $JENKINS_USER:$JENKINS_TOKEN $JENKINS_URL/crumbIssuer/api/json -s -o /dev/null -w %{http_code})
if [ "$issuer" == "200" ]; then
    export issuer=$(curl -k -u $JENKINS_USER:$JENKINS_TOKEN $JENKINS_URL/crumbIssuer/api/json -s)
    issuer=$(python -c "import json;import os;issuer=os.getenv('issuer');issuer=json.loads(issuer);print issuer['crumb']")
else
    issuer=""
fi

export target_file=$(dirname "${BASH_SOURCE[0]}")"/../target/alauda-devops-pipeline.hpi"
echo "target file is $target_file"

# support fetch plugin from the remote server
if [ "$#" == "1" ]; then
    if [[ "$1" =~ ^http.* ]]; then
        echo "going to download plugin from remote: $1"

        remote_issuer=$(curl -k -u $REMOTE_USER:$REMOTE_TOKEN $REMOTE_URL/crumbIssuer/api/json -s -o /dev/null -w %{http_code})
        if [ "$remote_issuer" == "200" ]; then
            export remote_issuer=$(curl -k -u $REMOTE_USER:$REMOTE_TOKEN $REMOTE_URL/crumbIssuer/api/json -s)
            remote_issuer=$(python -c "import json;import os;issuer=os.getenv('remote_issuer');issuer=json.loads(issuer);print issuer['crumb']")
        else
            remote_issuer=""
        fi
        curl -k -u $REMOTE_USER:$REMOTE_TOKEN $1 --header "Jenkins-Crumb: $remote_issuer" -o $target_file
    else
        echo "not a correct url: $1"
    fi
fi

curl -k -u $JENKINS_USER:$JENKINS_TOKEN $JENKINS_URL/pluginManager/uploadPlugin -F "name=@$target_file" --header "Jenkins-Crumb: $issuer"
curl -k -u $JENKINS_USER:$JENKINS_TOKEN $JENKINS_URL/restart -X POST --header "Jenkins-Crumb: $issuer"
