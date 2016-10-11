TSInfer and TSEvolve
=================

**TSInfer** is a tool for automatically inferring TypeScript declaration files for JavaScript libraries. 
  
**TSEvolve** reports differences between two versions of a library, by comparing the JavaScript implementations of the two versions. 

Installation
------------
The tools have been tested on 64 bit Windows and Linux, but should also work on Mac OS X. 

 - Make sure [node.js](http://nodejs.org/), [Chrome](https://www.google.com/chrome/), [jdk 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html), and [Intellij IDEA](https://www.jetbrains.com/idea/download/) are installed. 
 - Download and unpack this repository
 - Run the below two commands in the unpacked repository
    - `npm install`
    - `node node_modules/typescript/bin/tsc.js --module commonjs node_modules/ts-type-reader/src/*.ts`

Usage
-----

The tools do not have a command line interface, instead they are executed by opening the repository as an IntelliJ project, 
and writing the source code that does the intended task.  

The file `src\dk\webbies\tscreate\main\Main.java` is the main starting point to start using **TSInfer** and **TSEvolve**, 
`Main.java` includes comments describing how to 