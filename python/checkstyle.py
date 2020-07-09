#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""A basic python wrapper for checkstyle
    """

import os
import xml.etree.ElementTree as ET
import subprocess
import sys

from core import *

dir_path = os.path.dirname(os.path.realpath(__file__))
_CHECKSTYLE_JAR = os.path.join(dir_path, "../", "jars/checkstyle-8.0-all.jar")


def check(checkstyle_file_path, file_path, checkstyle_jar=_CHECKSTYLE_JAR, only_targeted=False, only_java=False):
    """
    Run checkstyle on the dir
    """
    cmd = "java -jar {} -f xml -c {} {}".format(checkstyle_jar, checkstyle_file_path, file_path)
    process = subprocess.Popen(cmd.split(" "), stdout=subprocess.PIPE)
    output = process.communicate()[0]
    # deletion of non xml strings
    if ( process.returncode > 0):
        output = b''.join(output.split(b'</checkstyle>')[0:-1]) + b'</checkstyle>'
    # parsing
    output = parse_res(output, only_targeted=only_targeted, only_java=only_java)
    return (output, process.returncode)

def parse_res(output, only_targeted=False, only_java=False):
    """
    Parse the reults from XML to a dict
    """
    output_parsed = dict()
    try:
        xml_output = ET.fromstring(output)
        for elem_file in xml_output.getchildren():
            if not only_java or elem_file.attrib['name'].endswith('.java'):
                output_parsed[elem_file.attrib['name']] = dict()
                output_parsed[elem_file.attrib['name']]['errors'] = list()
                for elem_error in elem_file.getchildren():
                    if ( elem_error.tag == 'error' ):
                        if only_targeted:
                            if is_error_targeted(elem_error.attrib):
                                output_parsed[elem_file.attrib['name']]['errors'].append(elem_error.attrib)
                        else:
                            output_parsed[elem_file.attrib['name']]['errors'].append(elem_error.attrib)
    except Exception as err:
        print(err)
        return None
    return output_parsed

def parse_file(file_path, only_targeted=False):
    with open(file_path) as f:
        file_content = f.read()
    return parse_res(file_content, only_targeted=only_targeted)

if __name__ == "__main__":
    if sys.argv[1] == "cs":
        checkstyle_path = "./test_corpora/commons-lang/checkstyle.xml"
        file_path = "./ugly/java-design-patterns/Target.java ./ugly/java-design-patterns/Client.java"
        if ( len(sys.argv) >= 4):
            checkstyle_path = sys.argv[2]
            file_path = sys.argv[3]

        (output_raw, errorcode) = check(checkstyle_path, file_path)
        print(output_raw, errorcode)
    elif sys.argv[1] == "check":
        out, n = check(sys.argv[2], sys.argv[3], only_targeted=True, only_java=True)
        json_pp(out)
        json_pp({file_name:len(content['errors']) for file_name, content in out.items() })
        json_pp(len([file_name for file_name, content in out.items() if len(content['errors']) == 0]))
