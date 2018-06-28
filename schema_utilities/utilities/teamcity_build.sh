#!/bin/sh
# Set flag so that we can see which commands are being run.
set -x
# Set flag so that we don't continue running this script if a command fails.
set -e

source_dir=${1-.}
publishing_branch="gh-pages"
git fetch origin "$publishing_branch"
root_dir=$(git rev-parse --show-toplevel)
this_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
orig_commit_hash=$(git rev-parse HEAD)

temp_dir="$root_dir/.temp_dir"
# Generate the files.
bash "$this_dir/process_schemas.sh" "$temp_dir" "$source_dir"
# Remove the non-HTML files.
find "$temp_dir" -type f -not -name "*.html" -delete
# Checkout the publishing branch.
git checkout "$publishing_branch"
git pull origin "$publishing_branch"
cp -r "$temp_dir/endpoints" "$root_dir"
cp "$temp_dir/index.html" "$root_dir"
# Cleanup.
rm -r "$temp_dir"
# Add all the generated HTML files to our publishing branch.
find "$root_dir/endpoints" -name "*.html" -exec git add "{}" \;
git add "$root_dir/index.html"
git add "$root_dir/README.md"
# Check if we have files to commit
has_files_to_commit=$(git status --short --untracked-files=no)
if [ ${#has_files_to_commit} -ne 0 ]
then
	# Commit and push, like a boss!
	git commit -m "Documentation update for commit $orig_commit_hash"
	git push --force origin "$publishing_branch"
fi
# Check out the original branch.
git checkout -
