from core import *
import difflib


def get_diff(old, new):
    with open(old, "r") as f:
        old_content = f.readlines()
    with open(new, "r") as f:
        new_content = f.readlines()

    return "".join(difflib.unified_diff(old_content, new_content))


def main():
    if len(sys.argv) != 3:
        logger.info(f'Bad number of arguments. Usage:\n$ collect_repairs.py $PROJECT $OUTPUT_FILE')

    dataset = sys.argv[1]
    output_file = sys.argv[2]

    dir_files_to_repair = os.path.join(get_real_dataset_dir(dataset), f'./1')
    dir_repaired_files = os.path.join(get_styler_repairs(dataset), f'./files-repaired/')

    results = {}

    for f in os.listdir(dir_repaired_files):
        logger.debug(f"Found repair dir for error {f}...")
        original_file_dir = os.path.join(dir_files_to_repair, f)
        repaired_file_dir = os.path.join(dir_repaired_files, f)
        result = {}
        if os.path.exists(original_file_dir):
            try:
                metadata_path = os.path.join(original_file_dir, "metadata.json")
                metadata = open_json(metadata_path)

                original_file_path = metadata['file_name']
                repaired_file_path = os.path.join(repaired_file_dir, original_file_path.split("/")[-1])

                result["errors"] = metadata["errors"]
                result["relative_path"] = original_file_path.split(dataset + "/")[-1]
                result["diff"] = get_diff(original_file_path, repaired_file_path)

                results[f] = result
            except Exception as e:
                logger.error(f"There was an error while reading the repaired file {f}: {e}")

    save_json("./", output_file, results)
    logger.info(f"Collected {len(results)} repairs.")

if __name__ == "__main__":
    main()
