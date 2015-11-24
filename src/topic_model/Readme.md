Classic topic model (plsa) on bills with records between 1993 and 2014.

Voting records (for example): ../../voting_data/all/104_1995_h37 
Bill text (for example): ../../voting_data_text/all/104/bills/hr/hr37/data.xml 

Order:
1. select.py -> findfiles()
2. select.py -> createDict()
3. select.py -> recordEachBill()
4. createWordDict.py 
5. make plsa -> ./plsa
6. ExtractTopWords.py -> extract()
7. ExtractTopWords.py -> eachYear()

