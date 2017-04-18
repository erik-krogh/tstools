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
`Main.java` includes comments describing how to run **TSInfer** and **TSEvolve**

Experiments
-----
In the main package alongside Main.java is also a couple of classes that run experiments, these are: 
`PerformanceTest`, `PrecisionTest`, `TSCheckMissesCounter` and `UsefullnessTest`. 
They are configured as an IntelliJ run configuration, and can thus be executed by selecting and running the appropriate run configuration in the top right corner of IntelliJ.
 
Mapping the tables from the [paper](http://cs.au.dk/~amoeller/papers/tstools/paper.pdf) to classes in the implementation:  
**Table 1:** `PerformanceTest` for the left part, and `PrecisionTest` for the right part.     
**Table 2:** `TSCheckMissesCounter`  
**Table 3:** `UsefulnessTest`   
**Table 4:** `UsefulnessTest` (comment in the part starting with `Map<String, Pair<DeclarationType, DeclarationType>> toCompare`)  
**Table 5/6:** Manual by running TSEvolve (from `Main`).


Performance
-----
These tools can be very hungry for RAM, for the biggest benchmarks it is recommended to have at least 16GB of RAM and an SSD, or 32GB of RAM. 
If your computer doesn't have that amount of RAM, it is recommended to only execute the smaller libraries. 

Some benchmarks that should run fine on most computers are: `async`, `Backbone`, `Hammer`, `Handlebars`, `jasmine`, `Knockout`, `moment`, and `underscore`. 