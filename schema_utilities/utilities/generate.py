import logging

def _build_raml_manifest(directory: str) -> str:
	"""
	Recursively walks a directory and returns the list of RAML files in it.
	"""
	def _raise(x):
		# Stupid function to force os.walk() to complain if it's passed a non-directory.
		raise x
	import os
	raml_manifest = []
	for dir_name, sub_dir_list, file_list in os.walk(directory, onerror=_raise):
		for file_name in file_list:
			if file_name.endswith('.raml'):
				raml_manifest.append(os.path.join(dir_name, file_name))
	return raml_manifest


def _generate_raml_html(raml_file_path: str, template_file_path: str, output_path: str):
	"""
	Generates HTML from a given RAML file and Nunjucks template.
	"""
	import subprocess
	logging.info('Generating endpoint HTML: %s\n\t(RAML source: %s)' % (output_path, raml_file_path))
	if subprocess.run([
		'raml2html',
		'--input', raml_file_path,
		'--template', template_file_path,
		'--output', output_path
	]).returncode != 0:
		raise RuntimeError('Bad RAML file (%s)' % raml_file_path)


def _generate_index_html(title: str, sub_pages: list, template_file_path: str, output_path: str):
	import re
	from os.path import relpath, dirname
	logging.info('Generating index HTML: ' + output_path)
	with open(template_file_path, 'r') as template:
		with open(output_path, 'w') as target:
			target.write(
				template.read().format(
					title=title,
					html_page_links='\n'.join([
						'<li><a href=\"./{page_path}\">{page_base}</a></li>'.format(
							page_path=relpath(page, dirname(output_path)),
							page_base=re.sub('/endpoint\.html$', '', page))
								for page in sub_pages
						])))


if __name__ == '__main__':
	import sys
	from os.path import normpath
	# Parse command line options
	endpoints_dir = normpath(sys.argv[1])
	filtered_dir = normpath(sys.argv[2])
	repo_name = sys.argv[3]
	raml_html_template_path = sys.argv[4]
	index_html_template_path = sys.argv[5]
	logging_level = sys.argv[6] if len(sys.argv) > 5 else "DEBUG"
	# Setup logging
	try:
		logging.basicConfig(level=getattr(logging, logging_level.upper()))
	except AttributeError:
		logging.error('Unknown logging level (%s).' % logging_level)
		sys.exit(1)
	# Get down to business!
	generated_raml_html_pages = []
	for raml_file_path in _build_raml_manifest(directory=normpath(endpoints_dir + '/' + filtered_dir)):
		generated_page_path = raml_file_path.replace('.raml', '.html')
		_generate_raml_html(
			raml_file_path=raml_file_path,
			template_file_path=raml_html_template_path,
			output_path=generated_page_path)
		generated_raml_html_pages.append(generated_page_path)
	generated_raml_html_pages.sort()
	_generate_index_html(
		title=repo_name,
		sub_pages=generated_raml_html_pages,
		template_file_path=index_html_template_path,
		output_path=normpath(endpoints_dir + '/../index.html'))
