def select():
    fin = open('../../voting_data/people/person_dict')
    partyMap = {}
    lines = fin.readlines()
    for line in lines:
        ls = line.split('\t')
        gtid = int(ls[6])
        newid = int(ls[0])
        party = ls[4]
        partyMap[newid] = party
    fin.close()

    fin = open('./res/p.txt')
    fout_d = open('./res/p_d.txt', 'w')
    fout_r = open('./res/p_r.txt', 'w')
    lines = fin.readlines()
    newid = 0
    for line in lines:
#        newid = int(line.split('\t')[0])
        if partyMap[newid] == 'Democrat':
            fout_d.write(line)
        elif partyMap[newid] == 'Republican':
            fout_r.write(line)
        newid += 1
    fin.close()
    fout_d.close()
    fout_r.close()

if __name__ == '__main__':
    select()

