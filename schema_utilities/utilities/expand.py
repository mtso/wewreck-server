# Expands JSON-schema references for all the JSON-schema files in a given
# directory.
#
#    python3 utilities/expansion/expand.py <working-dir> <your-schema-dir-relative-to-working-dir>

import logging
import subprocess

# Get the installation directory for globally installed NodeJS modules. We
# need this because we can't require global modules inside NodeJS scripts
# unless we give it a full path.
global_node_root = subprocess.run([ 'npm', 'root', '--quiet', '-g' ], stdout=subprocess.PIPE)
# This is the full path of the module we will be using to expand our JSON
# schema references.
ref_expander_module_path = global_node_root.stdout.decode('utf-8').strip() + '/json-schema-deref'


def _build_json_schema_manifest(directory: str) -> list:
	"""
	Recursively walks a directory and returns the list of JSON schema files in it.
	"""
	def _raise(x):
		# Stupid function to force os.walk() to complain if it's passed a non-directory.
		raise x
	import os
	json_schema_manifest = []
	for dir_name, sub_dir_list, file_list in os.walk(directory, onerror=_raise):
		for file_name in file_list:
			if file_name.endswith('schema.json'):
				schema_filepath = os.path.normpath(os.path.join(dir_name, file_name))
				# Create the key if it doesn't exist.
				json_schema_manifest.append(schema_filepath)
	return json_schema_manifest


def _expand_json_schema_references(json_schema: str, target_filepath: str) -> str:
	"""
	Returns the given JSON-schema with all of its JSON-schema references
	expanded.

	The implementation for this function is an ambimination of nature, please
	look away and think of unicorns and rainbows.
	"""
	import os
	global ref_expander_module_path
	# This is the raw NodeJS script we will be executing to expand the JSON
	# schema references. We dynamically generate it based on the passed in
	# schema and module path.
	expander_script = '''
	var deref = require('%s');
	var fs = require('fs');
	var jsonSchema = %s;
	deref(jsonSchema, function(err, fullSchema) {
		fs.writeFileSync('%s', JSON.stringify(fullSchema, null, '\\t'));
		process.exit();
	});
	''' % (ref_expander_module_path, json_schema, target_filepath)
	# Execute our script. We have to bump up the stack size because very large
	# JSON schemas will otherwise cause stack overflows, really!
	result = subprocess.run(
		[ 'node', '--stack-size=8000' ],
		input=bytes(expander_script, 'utf-8'),
		stdout=subprocess.PIPE)
	if result.returncode != 0:
		raise RuntimeError('Failed to run expander script')


def _expand_json_schema_files(directory: str):
	file_paths = _build_json_schema_manifest(directory)
	for filepath in file_paths:
		json_schema = ''
		logging.info('Expanding (%s)' % (filepath))
		with open(filepath, 'r') as schema_file:
			json_schema = schema_file.read()
		_expand_json_schema_references(json_schema=json_schema, target_filepath=filepath)


if __name__ == '__main__':
	import sys
	import os
	os.chdir(sys.argv[1])
	logging.basicConfig(level='DEBUG')
	_expand_json_schema_files(directory=sys.argv[2])
