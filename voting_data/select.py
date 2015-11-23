# parse xml data files 

import fnmatch
import os
import xml.etree.ElementTree as ET

def transform(filename, newname, pos):
    fout = open('./all/' + newname, 'w')
    print 'processing ' + newname
    tree = ET.parse(filename)
    root = tree.getroot()
    for f in root.findall('voter'):
        voter_id = f.attrib['id']
        voter_vote = f.attrib['value']
        pos[voter_vote] = 0
        newline = voter_id + '\t' + voter_vote + '\n'
        fout.write(newline)
    fout.close()
    print 'done'

def findfiles():
    matches = []
    for root, dirnames, filenames in os.walk('./'):
        for filename in fnmatch.filter(filenames, '*.xml'):
            matches.append(os.path.join(root, filename))

    pos = {}
    for filename in matches:
        ts = filename.split('/')
        newname = ts[1] + '_' + ts[3] + '_' + ts[4]
        transform(filename, newname, pos)
#        gu = raw_input()
    print pos

if __name__ == '__main__':
    findfiles()

