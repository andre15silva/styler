import git
from core import *
import logging
import os
import difflib
import time
from paramiko import SSHClient
from scp import SCPClient
import tempfile
import shutil


def no_diff(old, new):
    with open(old, "r") as f:
        old_content = f.readlines()
    with open(new, "r") as f:
        new_content = f.readlines()
    return sum(1 for _ in difflib.unified_diff(old_content, new_content)) == 0


def cached(name, protocol, checkstyle_xml):
    cache_dir = core_config['MODELS']['cache_dir']
    user = name.split("-")[0]
    project = name.split("-")[1]

    models_path = os.path.join(cache_dir, user, project)
    # Check if there are models
    if os.path.exists(models_path):
        # Get most recent releases first
        releases = sorted(list(map(lambda x: int(x), os.listdir(models_path))), reverse=True)
        for release in releases:
            release_path = os.path.join(models_path, str(release))
            # Check if checkstyle.xml is the same
            stored = os.path.join(release_path, 'checkstyle.xml')
            if os.path.exists(stored) and no_diff(stored, checkstyle_xml):
                # Return model for chosen protocol
                for model in os.scandir(release_path):
                    if model.name.startswith(protocol):
                        return model.path

    return None


def download_and_cache_release(ssh, sftp, release, release_path, name):
    cache_dir = core_config['MODELS']['cache_dir']
    user = name.split("-")[0]
    project = name.split("-")[1]
    models_path = os.path.join(cache_dir, user, project, str(release))
    if os.path.exists(models_path):
        shutil.rmtree(models_path)
    os.makedirs(models_path)

    
    logger.debug(f'Downloading release {release}')
    with SCPClient(ssh.get_transport()) as scp:
        for f in sftp.listdir():
            logger.debug(f'Downloading {f}')
            scp.get(f'{release_path}/{f}', f'{models_path}/{f}')


def get_model(name, protocol, checkstyle_xml):
    """
    Downloads the most recent up-to-date model for a given project and protocol
    """
    # If we have a cached model that fits the criteria, we use it
    if cached(name, protocol, checkstyle_xml):
        return cached(name, protocol, checkstyle_xml)

    # Connect via SSH
    with SSHClient() as ssh:
        ssh.load_system_host_keys()
        host = os.getenv('SNIC_HOST')
        username = os.getenv('SNIC_USERNAME')
        password = os.getenv('SNIC_PASSWORD')
        ssh.connect(host, username=username, password=password)

        with ssh.open_sftp() as sftp:
            # Get models for the project
            store_path = os.getenv('SNIC_PATH')
            user = name.split("-")[0]
            project = name.split("-")[1]

            # Check if store exists
            try:
                filestats = sftp.stat(store_path)
            except FileNotFoundError:
                logger.debug(f'Store remote location does not exist: {store_path}')
                return None

            # Try to find a model that fits the criteria
            sftp.chdir(store_path)
            if user in sftp.listdir() and project in sftp.listdir(f'{store_path}/{user}'):
                sftp.chdir(f'{store_path}/{user}/{project}')
                with tempfile.TemporaryDirectory() as tmpdirname:
                    # Get most recent first
                    releases = sorted(list(map(lambda x: int(x), sftp.listdir())), reverse=True)
                    for release in releases:
                        sftp.chdir(f'./{release}')
                        try:
                            tmplocation = f'{tmpdirname}/checkstyle.xml'
                            sftp.get(f'./checkstyle.xml', tmplocation)
                            if no_diff(tmplocation, checkstyle_xml):
                                for model in sftp.listdir():
                                    if model.startswith(protocol):
                                        release_path = f'{store_path}/{user}/{project}/{release}'
                                        download_and_cache_release(ssh, sftp, release, release_path, name)
                                        return cached(name, protocol, checkstyle_xml)
                            sftp.chdir(f'../')
                        except FileNotFoundError:
                            sftp.chdir(f'../')
                            continue

    # If we get here it means we didn't find a model that fits
    logger.debug(f'There are no stored models that match all criteria for project {name}')
    return None


def upload_model(model_path, checkstyle_path, name):
    """
    Upload model located at path to our persistent storage
    """
    # Connect via SSH
    with SSHClient() as ssh:
        ssh.load_system_host_keys()
        host = os.getenv('SNIC_HOST')
        username = os.getenv('SNIC_USERNAME')
        password = os.getenv('SNIC_PASSWORD')
        ssh.connect(host, username=username, password=password)

        # Upload via SCP
        with SCPClient(ssh.get_transport()) as scp:
            # Compute path
            store_path = os.getenv('SNIC_PATH')
            uid = str(int(time.time()))
            user = name.split("-")[0]
            repo_name = name.split("-")[1]
            proj_path = store_path + f'/{user}/{repo_name}/{uid}/'
            remote_model_path = proj_path + model_path.split("/")[-1]
            remote_checkstyle_path = proj_path + "checkstyle.xml"

            # Create dir
            logger.debug(f'Creating {proj_path} in {host}')
            ssh.exec_command(f'mkdir -p {proj_path}')

            # Upload
            logger.debug(f'Uploading {model_path} to {host}:{remote_model_path}')
            scp.put(model_path, remote_path=remote_model_path)
            logger.debug(f'Uploading {checkstyle_path} to {host}:{remote_checkstyle_path}')
            scp.put(checkstyle_path, remote_path=remote_checkstyle_path)
