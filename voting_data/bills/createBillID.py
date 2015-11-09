import glob
allFiles = glob.glob('../all/*')
billID = 0
billMap = {}
fout = open('./bill_dict', 'w')
for f in allFiles:
    filename = f.split('/')[-1]
    billMap[filename] = billID
    newline = filename + '\t' + str(billID) + '\n'
    fout.write(newline)
    billID += 1
fout.close()

