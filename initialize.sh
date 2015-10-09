#!/bin/bash
git submodule update --init --recursive;
cd lib/jsnap/;
npm install;
cd ../../;
cd lib/ts-type-reader/;
npm install;
node_modules/typescript/bin/tsc --module commonjs src/*.ts;
cd ../../;