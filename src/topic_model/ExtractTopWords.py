# extract top words from the result of topic model 
# input: p_zw 
def extract(option):
    wordMap = {}
    inputDir = ''
    if option == 1:
        inputDir = './word_dict.txt'
    elif option == 2:
        inputDir = './word_dict_subject.txt'
    fin = open(inputDir)
    lines = fin.readlines()
    for line in lines:
        word, wordid = line.split('\t')
        wordMap[int(wordid)] = word
    fin.close()

    fin = open('./res/plsa_zw.txt')
    fout = open('./res/topwords.txt', 'w')
    lines = fin.readlines()
    lineid = 0
    for line in lines:
        fout.write('--- ' + str(lineid) + ' ---\n')
        lineid += 1
        ls = line.split('\t')
        fls = {i:float(ls[i]) for i in range(len(ls))}
        icount = 0
        for i in sorted(fls.items(), key = lambda x: x[1], reverse = True):
            if icount == 30:
                fout.write('\n')
                break
            #print i[0], wordMap[i[0]], i[1]
            newline = wordMap[i[0]] + '\t' + str(i[1]) + '\n'
            fout.write(newline)
            icount += 1
    fin.close()
    fout.close()

def find_max(ls):
    K = len(ls)
    maxV = -100000000; maxK = -1;
    for i in range(0, K):
        v = float(ls[i])
        if v > maxV:
            maxV = v
            maxK = i
    return maxK

# generate statistics for each year's topic 
# input: p_dz 
def eachYear():
    SOFT = True
    HARD = True

    yearMap = {}
    fin = open('./bill_dict.txt')
    lines = fin.readlines()
    for line in lines:
        billName = line.split('\t')[0]
        billID = int(line.split('\t')[1])
        year = billName.split('all/')[1].split('/')[0]
        yearMap[billID] = year
    fin.close()

    year2topic_s = {}
    year2numbills_s = {}
    if SOFT:
        fin = open('./res/plsa_dz.txt')
        lines = fin.readlines()
        K = len(lines[0].split('\t'))
        lineID = 0
        for line in lines:
            year = yearMap[lineID]
            if year not in year2numbills_s:
                year2numbills_s[year] = 1.0
            else:
                year2numbills_s[year] += 1.0
            ls = line.split('\t')
            if year not in year2topic_s:
                year2topic_s[year] = {}
                for k in range(K):
                    year2topic_s[year][k] = 0
            for k in range(K):
                year2topic_s[year][k] += float(ls[k])
            lineID += 1
        fin.close()

        fout = open('./res/year_stat_soft.txt', 'w')
        for y in range(104, 115):
            y = str(y)
            newline = y
            for k in range(K):
                newline = newline + '\t' + str(year2topic_s[y][k] / year2numbills_s[y]) 
            newline = newline + '\n'
            fout.write(newline)
        fout.close()

    year2topic_h = {}
    year2numbills_h = {}
    if HARD:
        fin = open('./res/plsa_dz.txt')
        lines = fin.readlines()
        K = len(lines[0].split('\t'))
        lineID = 0
        for line in lines:
            year = yearMap[lineID]
            if year not in year2numbills_h:
                year2numbills_h[year] = 1.0
            else:
                year2numbills_h[year] += 1.0
            ls = line.split('\t')
            K = len(ls)
            if year not in year2topic_h:
                year2topic_h[year] = {}
                for k in range(K):
                    year2topic_h[year][k] = 0.0
            k = find_max(ls)
            year2topic_h[year][k] += 1.0
            lineID += 1
        fin.close()

        fout = open('./res/year_stat_hard.txt', 'w')
        for y in range(104, 115):
            y = str(y)
            newline = y
            for k in range(K):
                newline = newline + '\t' + str(year2topic_h[y][k] / year2numbills_h[y]) 
            newline = newline + '\n'
            fout.write(newline)
        fout.close()


if __name__ == '__main__':
#    extract(2)
    eachYear()

