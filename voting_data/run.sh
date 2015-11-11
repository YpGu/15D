#!/bin/bash

for i in `seq 1 114`; do 
	if [ ! -d ./"$i" ]; then 
		mkdir "$i"
	fi
	cd ./"$i"
	rsync -avz --delete --delete-excluded --exclude **/text-versions/ \
		govtrack.us::govtrackdata/congress/"$i"/votes .
	cd ..
done

#rsync -avz --delete --delete-excluded --exclude **/text-versions/ \
#govtrack.us::govtrackdata/congress/113/bills .
