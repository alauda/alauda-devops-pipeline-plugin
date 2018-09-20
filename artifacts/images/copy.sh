#!/bin/sh
if [ "$JENKINS_HOME" == "" ]; then
  JENKINS_HOME="/var/jenkins_home"  
fi;

if [ ! -d $JENKINS_HOME/plugins ]; then
  mkdir -p $JENKINS_HOME/plugins/
  echo "create plugin dir"
fi;

echo "copying plugins..."
for i in $(ls /plugin | grep -E '.*\..pi'); do
  name="${i/.jpi/}"
  name="${name/.hpi/}"
  echo "installing $i..."
  rm -rf "$JENKINS_HOME/plugins/$name" "$JENKINS_HOME/plugins/$i" || true
  cp /plugin/$i $JENKINS_HOME/plugins/
done
ls -ahl $JENKINS_HOME/plugins/