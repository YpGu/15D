import fnmatch
import os
import glob
import xml.etree.ElementTree as ET

def readIDtransform(idMap):
    fin = open('./person_dict')
    lines = fin.readlines()
    for line in lines:
        newid = int(line.split('\t')[0])
        gtid = int(line.split('\t')[-1])
        idMap[gtid] = newid
    fin.close()

def getYear(idMap):
    year2people = {}
    fid = 0
    for f in glob.glob('../all/*'):
        if fid%1000 == 0:
            print fid
        year = int(f.split('/')[-1].split('_')[0])
        if year not in year2people:
            year2people[year] = {}

        fin = open(f)
        lines = fin.readlines()
        for line in lines:
            uid = int(line.split('\t')[0])
            vote = line.split('\t')[1].split('\n')[0]
            if vote == 'Yea' or vote == 'Nay':
                year2people[year][uid] = 0
        fin.close()

        fid += 1

    fout = open('./person_year', 'w')
    for y in range(1, 114):
        if y not in year2people:
            continue
        print y, len(year2people[y])
        people_dict = year2people[y]
        if len(year2people[y]) == 0:
            continue
        people = ",".join([str(idMap[u]) for u in people_dict if u in idMap])
        newline = str(y) + '\t' + people + '\n'
        fout.write(newline)
    fout.close()

if __name__ == '__main__':
    idMap ={}
    readIDtransform(idMap)
    getYear(idMap)

