#!/bin/sh

# process_schemas.sh [output_dir] [endpoints_sub_dir] [--json-only]

# Set flag so that we can see which commands are being run.
set -x
# Set flag so that we don't continue running this script if a command fails.
set -e

this_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

working_dir="endpoints"
output_dir=$1
source_dir=${2-.}
json_only=$3 # Call with a value of "--json-only" to invoke this option.

tag=$(echo 'process_schemas-wehack' | tr '[:upper:]' '[:lower:]')

# Perform validation for RAML, schemas, and examples.
docker build \
	--file="$this_dir/../Dockerfile" \
	--tag="$tag" \
	--build-arg="working_dir=$working_dir" \
	--build-arg="source_dir=$source_dir" \
	--build-arg="json_only=$json_only" \
	"$this_dir/.."

if [ ! -z "$output_dir" ]
then
	# Create a temporary container from the image where we generated our HTML. This
	# is necessary because 'docker cp' doesn't let you copy files from an image.
	temp_container_id=$(docker create "$tag")
	# Remove the existing output_dir. This may exist from a prior run.
	rm -rf "$output_dir"
	# Copy the Docker container's files (now containing the expanded schemas and
	# generated HTML files) to our own filesystem.
	mkdir -p "$output_dir"
	docker cp "$temp_container_id:var/repo/$working_dir" "$output_dir/$working_dir"
	# Dear future me: Clean up this mess...
	if [ "$json_only" == "--json-only" ]
	then
		find "$output_dir" -type f -not -name "*.json" -exec rm "{}" \;
	else
		docker cp "$temp_container_id:var/repo/index.html" "$output_dir"
	fi
	# ...sincerely, past me.
	docker rm "$temp_container_id"
fi
