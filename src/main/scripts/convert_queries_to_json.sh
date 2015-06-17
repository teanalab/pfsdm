#!/usr/bin/env bash
# Script for converting tsv queries files like the ones from
# resources/CV-queries to json format accepted by Galago. Removes leading and
# trailing whitespace and dots inside queries.

# Usage example: ./convert_queries_to_json.sh pfsdm ../resources/CV-queries/INEX_LD.cv1.of.5.queries
# Output is written to standard output

usage="usage: $0 operator input_file"
operator=${1:?$usage}
input_file=${2:?$usage}

echo '{"queries":['

awk -F $'\t' '{ gsub(/[\.,\?'\''()]|^[[:space:]]*|[[:space:]]*$/, "", $2); gsub(/-/, " ", $2); print "{\"number\": \""$1"\", \"text\": \"#'$operator'("$2")\"}," }' $input_file

echo ']}'
