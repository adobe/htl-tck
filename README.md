Adobe Sightly Technology Compatibility Kit
====

The Adobe Sightly TCK provides a suite of tests meant to certify that a Sightly implementation conforms with the published language specification available at https://github.com/Adobe-Marketing-Cloud/sightly-spec/blob/master/SPECIFICATION.md.

## How To

### Building the package
To build the TCK just run the following command:

```bash
mvn clean install
```

This will result in two artifacts being built:

1. `io.sightly.tck-<version>.jar`
2. `io.sightly.tck-<version>-standalone.jar`

### Extracting and deploying the test scripts
The test files used by the TCK can be extracted using the following commands:

```bash
# extracts the test files in the current directory:
java -jar io.sightly.tck-<version>-standalone.jar --extract 

# extracts the test files in a specified folder
java -jar io.sightly.tck-<version>-standalone.jar --extract path/to/folder
```

The extracted files are organised as follows:

```bash
testfiles/
├── definitions # contains JSON files describing the tests
├── output      # contains the expected output markup
└── scripts     # contains the test scripts
```

The TCK assumes the scripts are available at some predefined URLs. The URL at which a test can be found is composed from `/sightlytck`, to which the relative path of script file from `testfiles/scripts` is added.

Assuming we have the following structure in `testfiles`:

```bash
testfiles/
└── scripts
    └── exprlang
        └── operators
            └── operators.html
```

the URL at which the output of the `operators.html` script is expected to be found is `<severURL>/sightlytck/exprlang/operators.html`.

### Running the TCK
The Sightly TCK can be run standalone or as a Maven artifact. Both modes assume that you have a server running where you have deployed the testing scripts.

#### Run the TCK as part of the `test` Maven build phase
Add the TCK as a `test` dependency to your `pom.xml` file:

```xml
<!-- testing dependencies -->
<dependency>
    <groupId>com.adobe.granite</groupId>
    <artifactId>io.sightly.tck</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

In the `build` section of your `pom.xml` file add the following:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <configuration>
                <dependenciesToScan>
                    <dependency>com.adobe.granite:io.sightly.tck</dependency>
                </dependenciesToScan>
            </configuration>
        </plugin>
    </plugins>
</build>
```

The following Java properties need to be set for successfully Running the TCK:
```
io.sightly.tck.serverURL=<server root URL>
io.sightly.tck.user=<Basic authentication user>
io.sightly.tck.pass=<Basic authentication password>
```

#### Run the TCK in standalone mode
For this mode you need the `io.sightly.tck-<version>-standalone.jar` artifact.

The standalone mode assumes that you have deployed the test scripts on your platform.

The following commands run the TCK:

```bash
# run the TCK
java -jar io.sightly.tck-<version>-standalone.jar --url http://www.example.com

# run the TCK on a server that requires Basic authentication
java -jar io.sightly.tck-<version>-standalone.jar --url http://www.example.com --authUser user --authPass pass
```

## Versioning
The TCK artifacts use a semantic versioning scheme - `MAJOR.MINOR.PATCH`:

* `MAJOR.MINOR` - identify the specification version for which the TCK was built
* `PATCH` - identifies the version of the TCK artifact for the corresponding specification version


