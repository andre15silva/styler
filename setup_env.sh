#! /bin/bash
set -e

CURRENT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )" ROOT_DIR="$(dirname "$CURRENT_DIR")"

if ! command -v python3 &>/dev/null; then
  echo "Python 3 is not installed"
  exit 1
fi

if ! command -v pip3 &>/dev/null; then
  echo "Pip 3 is not installed"
  exit 1
fi

if ! command -v git &>/dev/null; then
  echo "git is not installed"
  exit 1
fi

if ! command -v java &>/dev/null; then
  echo "java is not installed"
  exit 1
fi

echo "Create logs folder"
mkdir -p $CURRENT_DIR/python/logs
echo

echo "Create models folder"
mkdir -p $CURRENT_DIR/python/models
echo

echo "Initing OpenNMT-py submodule"
git submodule init
git submodule update
echo

echo "Installing requirements"
pip3 install virtualenv
virtualenv --python=python3.6 env
source ./env/bin/activate
pip3 install torch==1.6.0
pip3 install -r $CURRENT_DIR/python/requirements.txt
cd $CURRENT_DIR
echo

echo "Done"
echo
