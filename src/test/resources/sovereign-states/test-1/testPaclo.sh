#!/bin/bash

baseset_file=$1
baseset_dir=`dirname $baseset_file`

echo $baseset_file

for e in 0.1 0.2 0.3 0.01
do
  echo "e: $e"
  for d in 0.1 0.2 0.3 0.01
  do
    echo "d: $d"
    java -jar ~/research/dev/paclo/target/paclo-0.0.1-SNAPSHOT.jar $e $d ~/research/dev/paclo/src/test/resources/sovereign-states/countries-declarations.owx ~/research/dev/paclo/src/test/resources/sovereign-states/countries.ttl $baseset_file $baseset_dir/result-e$e-d$d.owl > log-e$e-d$d
  done
done
