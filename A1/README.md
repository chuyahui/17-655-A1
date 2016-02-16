# Assignment 1

This is the "read me" documentation for assignment 1. It explains how to execute the three systems and contains a guide to the organization of source code.

## How to execute the systems

_In short_

On Windows(x64):
```
Unzip the zip file, then double click the A1UI.exe inside the distribution folder
```

On OS X:
```
chmod +x distribution/runSystem*.sh
./distribution/runSystemA.sh
./distribution/runSystemB.sh
./distribution/runSystemC.sh
```

_In detail_

The three systems (system A, B and C) are already built and are placed in the `distribution` folder. See `systemA.jar`, `systemB.jar` and `systemC.jar`. All three files are executable java jars that would expect one argument when you run it: the absolute file path of the `distribution` folder. The folder contains the input files (already there) and will be used to output the result.

In addition, to avoid run time issues, the `distribution` folder also contains Java runtimes for `Mac OS X` and `Windows(x64)`.

Assuming `$P` is the file path to the distribution folder.
To run the system manually on Windows (x64):
```
distribution/jre/Windows/bin/java.exe -jar distribution/systemA.jar $P
distribution/jre/Windows/bin/java.exe -jar distribution/systemB.jar $P
distribution/jre/Windows/bin/java.exe -jar distribution/systemC.jar $P
```

To run the system manually on Mac OS X:
```
distribution/jre/osx/Contents/Home/bin/java -jar distribution/systemA.jar $P
distribution/jre/osx/Contents/Home/bin/java -jar distribution/systemB.jar $P
distribution/jre/osx/Contents/Home/bin/java -jar distribution/systemC.jar $P
```

## Guide to source code

The source code are separated into four folders: `common`, `systemA`, `systemB` and `systemC`.
- `common` contains the filter framework, shared filters and utilities.
- `systemA` contains filter and plumber specific to system A.
- `systemB` contains filter and plumber specific to system B.
- `systemC` contains filter and plumber specific to system C.

For each folder, source code are located under `src/main/java`.

The main class for system A, B and C are:
- `systemA/src/main/java/system/SystemAPlumber.java`
- `systemB/src/main/java/system/SystemBPlumber.java`
- `systemC/src/main/java/system/SystemCPlumber.java`

To get started, the filter frameworks are located in `common/src/main/java/framework` folder.
