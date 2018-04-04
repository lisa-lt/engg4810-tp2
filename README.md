# Digiscope
This is the repository for the Software portion of the Digiscope project [ENGG4810@UQ 2016]

## Getting Started

### Step 1: Download
Clone the repository to your computer.

### Step 2: IDE Files
Generate the configuration files for your IDE of choice

```
cd engg4810-team06

# If using eclipse
./gradlew eclipse

# If using intellij idea
./gradlew idea
```

### Step 3: Project Import
Import the project into your IDE in the way you typically would.

--------

## Building/Running The Project
Gradle should be used for all this stuff.

```bash
# build the project
./gradlew build

# run the tests
./gradlew test

# run the app
./gradlew runApp
```

Note the use of gradle wrapper. This allows everyone to build the project the same irrespective what system they are running to build the program, and everyone can build the project without having to first install and configure a specific version of Gradle. Gradle automatically obtains the libraries used from the maven repository.

# Generating the executable
./gradlew clean
./gradlew launch4j

To find the executable:

cd engg4810-team06
cd build
cd launch4j

Find Digiscope.exe

# Generating the installer
Compile Digiscope.nsi
