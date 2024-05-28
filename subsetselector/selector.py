import textdistance
import os
import heapq
import uuid
import sys
import concurrent.futures
import shutil



def read_file(file_path):
    with open(file_path, 'r') as file:
        return file.read()

def calculate_distance(file1, file2):
    content1 = read_file(file1)
    content2 = read_file(file2)
    return textdistance.damerau_levenshtein(content1, content2)

def safe_copy2(src, dst):
    base, extension = os.path.splitext(dst)

    while os.path.exists(dst):
        dst = base + '/' + str(uuid.uuid4()) + ".desk"

    shutil.copy2(src, dst)



inputdir = sys.argv[1] # /mnt/c/Users/omer_/Desktop/gensamples/positive/desk/wminput
sourcedir = sys.argv[2] # /mnt/c/Users/omer_/Desktop/gensamples/positive/desk/generated/dataset
targetdir = sys.argv[3] # /mnt/c/Users/omer_/Desktop/gensamples/positive/desk/subset


print("copying closest files of " + inputdir +  " from " + sourcedir + " to " + targetdir)

files_inputdir = [os.path.join(inputdir, file) for file in os.listdir(inputdir)]
files_sourcedir = [os.path.join(sourcedir, file) for file in os.listdir(sourcedir)]

closest_files = {}

def calculate_closest_files(i):
    distances = []
    for j in range(len(files_sourcedir)):
        distance = calculate_distance(files_inputdir[i], files_sourcedir[j])
        if len(distances) < 50: 
            heapq.heappush(distances, (-distance, files_sourcedir[j]))
        else:
            heapq.heappushpop(distances, (-distance, files_sourcedir[j]))
    return files_inputdir[i], [files_sourcedir for _, files_sourcedir in distances]

with concurrent.futures.ProcessPoolExecutor() as executor:
    for input_file, closest in executor.map(calculate_closest_files, range(len(files_inputdir))):
        closest_files[input_file] = closest

subfolderno = 0
for file, closest in closest_files.items():
    path = os.path.join(targetdir, "subdir_" + str(subfolderno)) 
    os.mkdir(path) 
    subfolderno += 1
    for close_file in closest:
        safe_copy2(close_file, path)
