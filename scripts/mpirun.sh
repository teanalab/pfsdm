#!/usr/bin/env bash
rank=$OMPI_COMM_WORLD_RANK
COLLECTIONS=(SemSearch_ES ListSearch INEX_LD QALD2)
if [ "$rank" -lt 20 ]
then
    echo "start working at " `hostname` $rank
    export collection=${COLLECTIONS[$((rank/5))]}
    export fold=$((rank%5+1))
    echo $collection.$fold
    bash -c "$1"
else
    echo "no work to do at " `hostname` $rank
fi
