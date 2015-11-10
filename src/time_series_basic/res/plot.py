import matplotlib.pyplot as plt

# plot the average idp of R/D's in each congress 
def plotidp():
	xsr = []; xsd = []; ysr = []; ysd = []
	fin = open('./idp_year.txt')
	lines = fin.readlines()
	for line in lines:
		ls = line.split('\t')
		year = 1790 + int(ls[0]) * 2
		aver_r = float(ls[1])
		aver_d = float(ls[2])
		if aver_r != 0:
			xsr.append(year)
			ysr.append(aver_r)
		if aver_d != 0:
			xsd.append(year)
			ysd.append(aver_d)
	fin.close()

	fig = plt.figure()
	line_r = plt.scatter(xsr, ysr, c = ['r' for u in ysr], label = 'Line 1')
	line_d = plt.scatter(xsd, ysd, c = ['b' for u in ysd], label = 'Line 2')
	plt.xlabel('Year')
	plt.ylabel('Average Ideal Point')
	plt.xlim([1789,2020])
	plt.legend([line_r, line_d], ['Republican', 'Democrat'], loc = 2)
	plt.show()
#	fig.savefig('idp_each_year_no_constraint.png')


# plot the number of R/D's in each congress
def plotnum():
	xs = []; ysr = []; ysd = []; ystotal = []
	fin = open('./idp_year.txt')
	lines = fin.readlines()
	for line in lines:
		ls = line.split('\t')
		year = 1790 + int(ls[0]) * 2
		aver_r = float(ls[3])
		aver_d = float(ls[4])
		xs.append(year)
		ysr.append(aver_r)
		ysd.append(aver_d)
		ystotal.append(aver_r + aver_d)
	fin.close()

	fig = plt.figure()
	line_r = plt.scatter(xs, ysr, c = ['r' for u in ysr], alpha = 1)
	line_d = plt.scatter(xs, ysd, c = ['b' for u in ysd], alpha = 1)
	line_g = plt.scatter(xs, ystotal, c = ['g' for u in ystotal], alpha = 0.3)
	plt.xlim([1789,2020])
	plt.ylim([-10,600])
	plt.xlabel('Year')
	plt.ylabel('Number of Lawmakers')
	plt.legend([line_r, line_d, line_g], ['Republican', 'Democrat', 'Sum'], loc = 2)
	plt.show()
#	fig.savefig('idp_num_each_year_no_constraint.png')


if __name__ == '__main__':
	plotidp()
#	plotnum()

