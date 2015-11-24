# parse xml data files 

import fnmatch
import os
import xml.etree.ElementTree as ET
import sys
import re
reload(sys)
sys.setdefaultencoding('utf-8')
from operator import itemgetter

def divide(c, regex):
    #the regex below is only valid for utf8 coding
    return regex.findall(c)

def update_dict(di, li, stopwords):
    for i in li:
        lli = re.split("-+", i.lower())
        for ii in lli:
            ii = ii.strip(' \t\n\r').lower()
            if len(ii) < 3 or ii.isdigit():
                continue
            elif ii in stopwords:
                continue
            else:
                if ii in di:
                    di[ii] += 1
                else:
                    di[ii] = 1
    return di

def transform(filename):
    # read stop word list 
    stopword_list = {}
    fin = open('./StopWordList.txt')
    lines = fin.readlines()
    for line in lines:
        stopword_list[line.split('\n')[0]] = 0
    fin.close()

    # find all bills' text 
    filenames = {}
    fin = open('./voted_bills.txt')
    lines = fin.readlines()
    for line in lines:
        year, billID = line.split('\t')
        billID = billID.split('\n')[0]
        if billID[0] == 'h':
            filePath = '../../voting_data_text/all/' + year + '/bills/hr/hr' + billID[1:len(billID)] + '/data.xml'
            filenames[filePath] = 0
        elif billID[0] == 's':
            filePath = '../../voting_data_text/all/' + year + '/bills/s/' + billID + '/data.xml'
            filenames[filePath] = 0
    fin.close()

    # parse xml file 
    print 'Number of bills = ' + str(len(filenames))
    fileCount = 0; num_nonexist = 0
    word_dict = {}
    for filename in filenames:
#        print 'Processing ' + filename
        if not os.path.isfile(filename):
            print 'non-exist: ' + filename
            num_nonexist += 1
            continue
        tree = ET.parse(filename)
        root = tree.getroot()
        regex = re.compile("(?x) (?: [\w-]+  | [\x80-\xff]{3} )")
        for f in root.findall('summary'):
            words = divide(f.text, regex)
            word_dict = update_dict(word_dict, words, stopword_list)
        fileCount += 1
    print 'done, non-exist = ' + str(num_nonexist)

    # output 
    fout = open("./CountOfWords.txt", 'w')
    for i in sorted(word_dict.items(), key=itemgetter(1), reverse=True):
        fout.write(str(i[0]) + '\t' + str(i[1]) + '\n')
    fout.close()
 
def findfiles():
    fout = open('voted_bills.txt', 'w')
    for root, dirnames, filenames in os.walk('../../voting_data/all/'):
        for year in range(104, 115):
            for filename in fnmatch.filter(filenames, str(year) + '_*'):
                ts = filename.split('/')
                billname = ts[-1]
                billid = billname.split('_')[-1]
                print billid
                newline = str(year) + '\t' + billid + '\n'
                fout.write(newline)
    fout.close()

if __name__ == '__main__':
#    findfiles()
    transform('../../voting_data_text/all/104/bills/hr/hr37/data.xml')

