#!/usr/bin/env bash
# Script will convert all queries files from resources/CV-queries to
# resources/CV-queries-json using convert_queries_to_json.sh with methods from
# METHODS variable below.

METHODS=( prms mlm sdm fieldedsdm pfsdm )
DIR=$( cd "$(dirname "${BASH_SOURCE[0]}" )" && pwd)
ORIG="$DIR/../resources/CV-queries"
JSON="$DIR/../resources/CV-queries-json"
mkdir -p $JSON
for method in "${METHODS[@]}"
do
    mkdir -p $JSON/$method
    for queries in $ORIG/*
    do
        queries_base=$(basename "$queries")
        $DIR/convert_queries_to_json.sh $method $queries > $JSON/$method/${queries_base%.queries}.json
    done
done
