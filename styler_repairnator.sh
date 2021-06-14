#! /bin/bash
set -e

PROJECT=$1
PARSED_PROJECT=$(echo $PROJECT | tr \/ -)
WORKING_BRANCH=$2
COMMIT_ID=$3

export SNIC_HOST=$4
export SNIC_USERNAME=$5
export SNIC_PASSWORD=$6
export SNIC_PATH=$7

OUTPUT_FILE=$8

source ./env/bin/activate

# Setup SSH for model store
echo "Setup ssh for $SNIC_HOST"
mkdir -p /root/.ssh
ssh-keyscan $SNIC_HOST >> /root/.ssh/known_hosts
echo

cd python

# Collect data for training and repair
python collector/collect_violations.py $PROJECT $WORKING_BRANCH $COMMIT_ID

# Download or train the models
python download_or_train_models.py $PROJECT $WORKING_BRANCH $COMMIT_ID

# Repair PR
python styler.py repair True $PARSED_PROJECT

# Collect repaired files
python collect_repairs.py $PARSED_PROJECT $OUTPUT_FILE
