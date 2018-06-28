FROM artifactory.devops.wepay-inc.com/wepay/schema-utils-python-node:1.0.1

ARG working_dir="./endpoints"
ARG source_dir="."
ARG json_only="false"

# Copy the repo to the container.
ADD / /var/repo/

# Set PWD for RUN
WORKDIR /var/repo/

# Perform schema expandion.
RUN python3 "utilities/expand.py" "$working_dir" "$source_dir"

# Perform schema and example validation.
RUN python3 "utilities/validate.py" "$working_dir/$source_dir" "--validate-json" DEBUG

# Generate the HTML files.
RUN [ "$json_only" == "--json-only" ] || \
	python3 \
		"./utilities/generate.py" \
		"$working_dir" \
		"$source_dir" \
		"wehack" \
		"./utilities/templates/template.nunjucks" \
		"./utilities/templates/index.html" \
		"DEBUG"
