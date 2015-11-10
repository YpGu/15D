import matplotlib.pyplot as plt

def aver():
    p = {}
    fin = open('./p')
    lines = fin.readlines()
    newid = 0
    for line in lines:
        p[newid] = float(line)
        newid += 1
    fin.close()

    party = {}
    fin = open('../../../voting_data/people/person_dict')
    lines = fin.readlines()
    for line in lines:
        newid = int(line.split('\t')[0])
        party[newid] = line.split('\t')[4]
    fin.close()

    # for plot
    xs = []; ysr = []; ysd = []

    fin = open('../../../voting_data/people/person_year')
    fout = open('./idp_year', 'w')
    lines = fin.readlines()
    for line in lines:
        year = line.split('\t')[0]
        aver_r = 0; num_r = 0
        aver_d = 0; num_d = 0
        ls = line.split('\t')[1].split(',')
        for newid in ls:
            newid = int(newid)
            if newid not in party:
                continue
            if party[newid] == 'Republican':
                aver_r += p[newid]
                num_r += 1
            elif party[newid] == 'Democrat':
                aver_d += p[newid]
                num_d += 1
        if num_r != 0:
            aver_r = aver_r / float(num_r)
        if num_d != 0:
            aver_d = aver_d / float(num_d)
        print year, aver_r, aver_d, num_r, num_d

        if aver_r > aver_d:
            xs.append(year); ysr.append(aver_r); ysd.append(aver_d)
            newline = str(year) + '\t' + str(aver_r) + '\t' + str(aver_d) + '\t' + str(num_r) + '\t' + str(num_d) + '\n'
            fout.write(newline)
        else:
            xs.append(year); ysr.append(-aver_r); ysd.append(-aver_d)
            newline = str(year) + '\t' + str(-aver_r) + '\t' + str(-aver_d) + '\t' + str(num_r) + '\t' + str(num_d) + '\n'
            fout.write(newline)
    fin.close()
    fout.close()

    '''
    # plot
    fig = plt.figure()
    plt.scatter(xs, ysr, c = ['r' for u in ysr])
    plt.scatter(xs, ysd, c = ['b' for u in ysd])
    fig.savefig('1.png')
    '''

if __name__ == '__main__':
    aver()

