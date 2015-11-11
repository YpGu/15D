import glob
def everyYear():
    allFiles = glob.glob('../all/*')
    yearMap = {}
    for f in allFiles:
        filename = f.split('/')[-1]
        year = int(filename.split('_')[0])
        if year in yearMap:
            yearMap[year] += 1
        else:
            yearMap[year] = 1

    fout = open('./num_of_bills_in_each_year.txt', 'w')
    for i in range(1,115):
        newline = str(i) + '\t' + str(yearMap[i]) + '\n'
        fout.write(newline)
    fout.close()

def every10Year():
    allFiles = glob.glob('../all/*')
    yearMap = {}
    for f in allFiles:
        filename = f.split('/')[-1]
        year = int(filename.split('_')[0])/10
        if year in yearMap:
            yearMap[year] += 1
        else:
            yearMap[year] = 1

    fout = open('./num_of_bills_in_each_year_10.txt', 'w')
    for i in range(12):
        newline = str(i) + '\t' + str(yearMap[i]) + '\n'
        fout.write(newline)
    fout.close()

if __name__ == '__main__':
#    everyYear()
    every10Year()

