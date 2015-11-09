pid = 0
fout = open('person_dict', 'w')
allParties = {}

fin = open('legislators-current.csv')
lines = fin.readlines()
for i in range(1,len(lines)):
    ls = lines[i].split(',')
    last_name = ls[0]
    first_name = ls[1]
    state = ls[5]
    party = ls[7] 
    allParties[party] = 0
    ocp = ls[4]
    gtID = ls[23]
    newline = str(pid) + '\t' + first_name + '\t' + last_name + '\t' + state + '\t' + party + '\t' + ocp + '\t' + gtID + '\n'
    pid += 1
    fout.write(newline)
fin.close()

fin = open('legislators-historic.csv')
lines = fin.readlines()
for i in range(1,len(lines)):
    ls = lines[i].split(',')
    last_name = ls[0]
    first_name = ls[1]
    state = ls[5]
    party = ls[7] 
    allParties[party] = 0
    ocp = ls[4]
    gtID = ls[23]
    newline = str(pid) + '\t' + first_name + '\t' + last_name + '\t' + state + '\t' + party + '\t' + ocp + '\t' + gtID + '\n'
    pid += 1
    fout.write(newline)
fin.close()

print len(allParties)
for p in allParties:
    print p
fout.close()

