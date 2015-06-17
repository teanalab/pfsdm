#!/usr/bin/env bash
usage="usage: $0 operator input_file"
operator=${1:?$usage}
input_file=${2:?$usage}

echo '{"queries":['

awk -F $'\t' '{ gsub(/\.|^[[:space:]]*|[[:space:]]*$/, "", $2); print "{\"number\": \""$1"\", \"text\": \"#'$operator'("$2")\"}," }' $input_file

echo ']}'
