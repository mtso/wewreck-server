# This script validates that the contents of a given directory adhere to WePay's
# documentation repo format. This consists of following...
#    1) All files conform to our naming conventions.
#    2) All JSON files are valid JSON.
#    3) All JSON-schema files are valid JSON-schema.
#    4) All example files conform to their corresponding JSON-schema.

import json
import os
import re
import logging

def validate_filenames(directory, formats):
	'''
	Iterates over all of the files in the given directory (recusively) and
	validates that each filename conforms to the given 'formats' based on its
	file extention. The param 'formats' is a dictionary of file extentions to
	a regex that describes the valid name format for that file type.
	'''
	for dir_name, sub_dir_list, file_list in os.walk(directory, onerror=_raise):
		for file_name in file_list:
			file_ext = os.path.splitext(file_name)[1]
			try:
				if not re.match(formats[file_ext], file_name):
					raise ValueError(
						'Badly named \'%s\' file (%s); must be in the format of \'%s\'.'
						% (file_ext, os.path.join(dir_name, file_name), formats[file_ext]))
			except KeyError as e:
				pass

def build_json_schema_manifest(directory):
	'''
	Builds a manifest dictionary. Each key is a filepath to a JSON-schema file.
	The key value is a list of filepaths to examples to validate against the
	key's schema.

	Note 1: Schemas and examples are only matched against eachother when they
	        are in the same directory.
	Note 2: Schemas must follow the filename convention of '^.*\.schema\.json$'
	Note 3: Examples must follow the filename convention of '^.*\.example\.\d+\.json$'
	Note 4: If an example file is found, its corresponding schema key will be
	        created automatically.
	Note 5: If a schema file is found, and no corresponding example files are
	        found, its value will be an empty list.

	Example return data:
		{
			"foo/bar.schema.json": [ "foo/bar.example.1.json", "foo/bar.example.2.json" ]
			"buz/biz.schema.json": []
			"baz/boz/bez.schema.json" : [ "baz/boz/bez.example.1.json" ]
		}
	'''
	json_schema_manifest = {}
	for dir_name, sub_dir_list, file_list in os.walk(directory, onerror=_raise):
		for file_name in file_list:
			# Check if we have found an example file.
			name_parts = re.search('^(.*\.)example\.[a-zA-Z_\d]+(\.json)$', file_name)
			if name_parts:
				schema_filepath = os.path.normpath(
					os.path.join(
						dir_name,
						name_parts.group(1) + 'schema' + name_parts.group(2)))
				# Associate the example file with its schema file.
				json_schema_manifest.setdefault(
					schema_filepath,
					[]).append(
						os.path.normpath(os.path.join(dir_name, file_name)))
			else:
				# Check if we have found a schema file.
				name_parts = re.search('^(.*\.)schema(\.json)$', file_name)
				if name_parts:
					schema_filepath = os.path.normpath(os.path.join(dir_name, file_name))
					# Create the key if it doesn't exist.
					json_schema_manifest.setdefault(schema_filepath, [])
	return json_schema_manifest

def build_raml_manifest(directory):
	raml_manifest = []
	for dir_name, sub_dir_list, file_list in os.walk(directory, onerror=_raise):
		for file_name in file_list:
			if file_name.endswith('.raml'):
				raml_manifest.append(os.path.normpath(os.path.join(dir_name, file_name)))
	return raml_manifest

def validate_raml(raml_manifest):
	import subprocess
	for raml_filepath in raml_manifest:
		try:
			logging.info('Validating RAML file    (%s)' % raml_filepath)
			dev_null = open('/dev/null', 'wb')
			if subprocess.run([ 'which', 'raml2html' ], stdout=dev_null).returncode != 0:
				raise RuntimeError('Missing raml2html installation! "npm install raml2html"')
			if subprocess.run([
				'raml2html',
				'--input', raml_filepath
			], stdout=dev_null).returncode != 0:
				raise RuntimeError('Bad RAML file (%s)' % raml_filepath)
		except Exception as e:
			logging.error('RAML file failed validation (%s).\n' % raml_filepath)
			raise

def validate_schemas_and_examples(json_schema_manifest):
	import jsonschema
	for schema_filepath in json_schema_manifest:
		logging.info('Validating schema file  (%s)' % schema_filepath)
		try:
			schema = json.load(open(schema_filepath, encoding='utf-8'))
			jsonschema.Draft4Validator.check_schema(schema)
		except IOError as e:
			logging.error('Schema file (%s) could not be opened.\n' % schema_filepath)
			raise
		except ValueError as e:
			logging.error('Schema file (%s) is not in JSON format.\n' % schema_filepath)
			raise
		except jsonschema.SchemaError as e:
			logging.error(
				'Schema file (%s) is not valid JSON-schema format.\n'
				% (schema_filepath))
			raise
		for example_filepath in json_schema_manifest[schema_filepath]:
			logging.info('Validating example file (%s)' % example_filepath)
			try:
				jsonschema.validate(json.load(open(example_filepath, encoding='utf-8')), schema)
			except IOError as e:
				logging.error('Example file (%s) could not be opened.\n' % example_filepath)
				raise
			except ValueError as e:
				logging.error(
					'Example file (%s) is not in JSON format.\n'
					% example_filepath)
				raise
			except jsonschema.ValidationError as e:
				logging.error(
					'Example file (%s) failed validation against schema (%s).\n'
					% (example_filepath, schema_filepath))
				raise
			# Unfortunately, the JSON-schema validation routine does not attempt
			# to resolve JSON-schema references until it is validating against
			# an example file. Otherwise, this except clause would have come
			# before we even tried to open the example file.
			except jsonschema.RefResolutionError as e:
				logging.error(
					'Schema file (%s) contains a JSON-schema reference that cannot be resolved.\n'
					% (schema_filepath))
				raise

# Stupid function to force os.walk() to complain if it's passed a non-directory.
def _raise(x):
	raise x

_FILENAME_FORMATS = {
	".json": "(^.*\\.schema\\.json$|^.*\\.example.[a-zA-Z_\\d]+\\.json$)",
	".raml": "^endpoints?.raml$",
	".md": "(^endpoints?.md$|README.md)"
}

if __name__ == '__main__':
	import sys
	VALIDATE_ALL = "--validate-all"
	VALIDATE_JSON = "--validate-json"
	VALIDATE_RAML = "--validate-raml"
	endpoint_dir = sys.argv[1]
	validation_target = sys.argv[2] if len(sys.argv) > 2 else VALIDATE_ALL
	# Validate the target of validation (so meta)
	if validation_target not in [ VALIDATE_ALL, VALIDATE_JSON, VALIDATE_RAML ]:
		logging.error('Unknown validation target (%s).' % validation_target)
		sys.exit(1)
	# Setup logging
	logging_level = sys.argv[3] if len(sys.argv) > 3 else "DEBUG"
	try:
		logging.basicConfig(level=getattr(logging, logging_level.upper()))
	except AttributeError:
		logging.error('Unknown logging level (%s).' % logging_level)
		sys.exit(1)
	# Override log level for the "requests" library as it spams the log output.
	logging.getLogger('requests').setLevel(logging.WARNING)
	try:
		validate_filenames(directory=endpoint_dir, formats=_FILENAME_FORMATS)
		if validation_target == VALIDATE_ALL or validation_target == VALIDATE_JSON:
			validate_schemas_and_examples(json_schema_manifest=build_json_schema_manifest(endpoint_dir))
		if validation_target == VALIDATE_ALL or validation_target == VALIDATE_RAML:
			validate_raml(raml_manifest=build_raml_manifest(directory=endpoint_dir))
	except Exception as e:
		logging.error(e)
		sys.exit(1)
	else:
		logging.info('All good in the hood!')
		sys.exit(0)
