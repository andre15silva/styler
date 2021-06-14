from core import *
import model_store

def main():
    if len(sys.argv) != 4:
        logger.info('Bad number of arguments. Usage:\n$ download_or_train_model.py $PROJECT $WORKING_BRANCH $COMMIT_ID')
        return
        
    repo_slug = sys.argv[1]
    dataset = repo_slug.split("/")[0] + "-" + repo_slug.split("/")[1]
    working_branch = sys.argv[2]
    commit_id = sys.argv[3]

    dir_files_to_repair = get_real_dataset_dir(dataset)
    checkstyle_xml = os.path.join(dir_files_to_repair, 'checkstyle.xml')

    for protocol in ['random', 'three_grams']:
        model = model_store.get_model(dataset, protocol, checkstyle_xml)
        if model == None:
            logger.info(f'There is no model for {dataset} with protocol {protocol} available for the given checkstyle_xml.')
            train(dataset, protocol)
        else:
            logger.info(f'Found model for {dataset} with protocol {protocol} available for the given checkstyle_xml.')


def train(dataset, protocol):
    cmd = f'./styler_training_{protocol}_upload.sh {dataset}'
    process = subprocess.Popen(cmd.split(" "), stdout=subprocess.PIPE)
    return process.communicate()[0]

if __name__ == "__main__":
    main()
