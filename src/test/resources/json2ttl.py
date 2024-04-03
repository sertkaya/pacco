#!/usr/bin/python3

import json
import argparse

# Initialize parser
parser = argparse.ArgumentParser()

# Adding optional argument
parser.add_argument("-i", "--Input", help = "Input file")
parser.add_argument("-o", "--Output", help = "Output file")

# Read arguments from command line
args = parser.parse_args()


with open(str(args.Input), "r") as f:
    d = json.load(f)

out = open(str(args.Output), "w")

for x in d:
    s = x.get('s')
    if s.startswith("http"):
        s = "<" + s + ">"
    else:
        s = "\"" + s + "\""
    p = x.get('p')
    if p.startswith("http"):
        p = "<" + p + ">"
    else:
        p = "\"" + p + "\""
    o = x.get('o')
    if o.startswith("http"):
        o = "<" + o + ">"
    else:
        o = "\"" + o + "\""

    out.write(s + " " + p + " " + o + " .\n")
