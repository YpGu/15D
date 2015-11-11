def lp():
    pd = {}
    fin = open('../people/person_dict')
    lines = fin.readlines()
    for line in lines:
        pd[line.split('\t')[4]] = 0
    fin.close()
    
    fout = open('./all_parties.txt', 'w')
    for p in pd:
        if p == '':
            continue
        fout.write(p + '\n')
    fout.close()

if __name__ == '__main__':
    lp()
