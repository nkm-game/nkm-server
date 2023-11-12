import glob
import os
from pathlib import Path

def create_bundle(paths, bundle_name):
    # Concatenate all file data
    all_data = ""
    for path in paths:
        # Recursively search for .scala files and read each one
        for filename in glob.glob(path, recursive=True):
            normalized_filename = filename.replace(os.sep, '/')
            with open(filename, 'r', encoding='utf-8') as file:
                # Prepend the file name and append a newline after the file content
                all_data += f"// {normalized_filename}\n{file.read()}\n"

    # Write the concatenated data to a file
    Path("out/").mkdir(parents=True, exist_ok=True)

    with open(f"out/{bundle_name}", 'w', encoding='utf-8') as output_file:
        output_file.write(all_data)
    print(f"Data has been written to {bundle_name}")

create_bundle(['../src/main/scala/com/tosware/nkm/models/game/abilities/**/*.scala'], "abilities_code.txt")
create_bundle(['../src/main/scala/com/tosware/nkm/models/game/effects/**/*.scala'], "effects_code.txt")
create_bundle(['../src/test/scala/unit/abilities/**/*.scala'], "abilities_test_code.txt")
create_bundle(['../src/test/scala/unit/effects/**/*.scala'], "effects_test_code.txt")

