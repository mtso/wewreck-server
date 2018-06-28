#!/bin/sh
set -e
this_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
temp_dir=$(mktemp -d 2>/dev/null || mktemp -d -t 'schema_utilities-temp-dir')
rm -rf "$temp_dir"
git clone --depth 1 --branch master "ssh://git@bitbucket.devops.wepay-inc.com:7999/api-team/schema_utilities.git" "$temp_dir"
bash "$temp_dir/setup.sh"
mv -f "$temp_dir/update.sh" "$this_dir/update.sh"
rm -rf "$this_dir/utilities"
mv "$temp_dir/utilities" "$this_dir/utilities"
mv "$temp_dir/README.md" "$this_dir/README.md"
mv "$temp_dir/Dockerfile" "$this_dir/Dockerfile"
rm -rf "$temp_dir"
