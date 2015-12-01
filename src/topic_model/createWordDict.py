def wordDict():
    fin = open('./CountOfWords_subject.txt')
    fout = open('./word_dict_subject.txt', 'w')
    lines = fin.readlines()
    wordID = 0; 
    for line in lines:
        if int(line.split('\t')[1]) < 3:
            continue
        newline = line.split('\t')[0] + '\t' + str(wordID) + '\n'
        fout.write(newline)
        wordID += 1
    fin.close()
    fout.close()

if __name__ == '__main__':
    wordDict()

