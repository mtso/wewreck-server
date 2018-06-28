# wehack

## Populating Schemas
Put your RAML and JSON-schema files under the 'endpoints' directory.
>See [this page](http://jsonschema.net/#/) for help with creating JSON-schema.

### RAML and JSON-Schema Do's and Don't's
- DO follow the naming convention for RAML files of 'endpoint.raml'.
- DO follow the naming convention for the schema of HTTP requests '{http method}.req.schema.json'.
- DO follow the naming convention for the schema of HTTP responses '{http method}.resp.{http code}.schema.json'.
- DO follow the naming convention for the examples of HTTP requests '{http method}.req.example.{number}.json'.
- DO follow the naming convention for the examples of HTTP responses '{http method}.resp.{http code}.example.{number}.json'.
- DO run `bash utilities/process_schemas.sh` before committing changes, as to prevent bad examples or schemas from being committed.
- DO NOT mix JSON schema into a RAML document, even though RAML lets you do it, use the !include directive instead.

## Dependencies
- [Docker](https://docs.docker.com/docker-for-mac/install/)

## Validation & Generation
Put all of your RAML and JSON-schema files under the `endpoints` directory, then run...

> bash utilities/process_schemas.sh <output_dir>

Warning! If the specified `output_dir` already exists, it will be replaced!

The validation routine ensures that the following things are true...
- All JSON-schema files are valid JSON-schema.
- All RAML files are valid RAML.
- All files in the endpoints directory adhere to the expected file naming convention (see RAML Do's and Don't's).

## Constraints
- JSON-schema references cannot be resolved if the reference points to a JSON key that contains a dot.

## Publishing Generated HTML Files
Aside from the initial setup, the publishing step is be completely owned by the [TeamCity](http://teamcity.devops.wepay-inc.com/overview.html) build and should not be performed by developers.

To set up your TeamCity build...
1) Add TeamCity as a contributor to your GitHub repo (this is done in GitHub via settings).
2) [Add a TeamCity build step that calls teamcity_build.sh](http://confluence.devops.wepay-inc.com:8090/display/AE/TeamCity+Setup+for+Docs+Publishing).
3) Ensure you have an empty "gh-pages" branch for your repository. This branch should contain no files, and does not need to retain the commit history of master. If you do not have a "gh-pages" branch, you can use the following commands to set it up...
> git checkout --orphan gh-pages && git rm -rf . && echo "Branch owned by TeamCity build, do not commit manually" > README.md && git add README.md && git commit -m "Initial commit" && git push --set-upstream origin gh-pages
