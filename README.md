# OXPath Project

**Latest release of OXPath Project: 1.0.4**<br />

**Documentation (OXPath Project):** [1.0.4 javadocs](https://oxpath.github.io/apidocs/oxpath/1.0.4/javadocs/index.html)<br />
**Continuous Integration:** [![Build Status](https://travis-ci.org/oxpath/oxpath.svg?branch=master)](https://travis-ci.org/oxpath/oxpath)<br />
**License:** [3-Clause BSD License](LICENSE.md)

-----------------------

[OXPath](https://github.com/oxpath/) is a web data extraction tool.
The original version, OXPath 2.0, was provided by the [Diadem Team](http://diadem.cs.ox.ac.uk/).

The first version, OXPath 1.0, can be found at [https://github.com/diadem/OXPath](https://github.com/diadem/OXPath).

The current version supports Linux and OSX platforms.

_[Meltwater](https://www.meltwater.com) uses OXPath to extract millions of documents from 100'000s of sources daily._

## Project Structure

OXPath Project consists of the following modules:
- [OXPath Core](oxpath-core), implementing the core functionality of the OXPath language.
- [WebAPI](webapi), implementing an interface to web browsers (only Firefox 47.0.1 is currently supported).
- [Util](util) contains functionality required for the project.
- [Output Handlers](output-handlers) are a set of modules for serialising the result tree of OXPath into different formats (e.g., XML, JSON, CSV, RDB).
- [OXPath CLI](oxpath-cli) is a command line interface for OXPath.
- [Browser Installer](browser-installer) installs a web browser required by OXPath.

## Installation

The project requires Java 1.7 (or higher).

### Linux
Linux users need to run [Browser Installer](browser-installer), which will install web browser into `.oxpath` in their home directory.

### OSX
Mac users need to install a web browser supported by OXPath (i.e., [Firefox 47.0.1](https://ftp.mozilla.org/pub/firefox/releases/47.0.1/mac/)) and convey OXPath with a configuration file as follows:
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<diadem>
	<webapi>
		<platforms>
			<platform os-type="OSX">
				<home user-home-rel="true">.oxpath</home>
				<browser name="FIREFOX">
					<relpath>firefox_47.0.1</relpath>
					<run-file-path>/Applications/Firefox 47.0.1.app/Contents/MacOS/firefox</run-file-path>
					<display-size-file-relpath>display_size</display-size-file-relpath>
					<download-dir-relpath>download</download-dir-relpath>
				</browser>
			</platform>
		</platforms>
	</webapi>
</diadem>
```

### Installation Into Your Local Repository

The installation of OXPath requires [Maven v.3](https://maven.apache.org/).

All OXPath maven artifacts can be installed with either of the following commands:
`mvn install` (with unit tests) or `mvn install -Dmaven.test.skip=true` (without unit tests).
These commands will also create a binary file `oxpath-cli.jar`, which you can find in the `oxpath-cli/target` directory.

## Binaries

The implementation of the command line interface for OXPath is in the directory [oxpath-cli](oxpath-cli), which can produce an executable binary oxpath-cli.jar.

## Running

Details of running the binary oxpath-cli.jar can be found in [oxpath-cli/README.md](oxpath-cli/README.md).

## Integration

OXPath can be integrated into other maven artifacts with the following dependency statements:
```xml
<dependency>
	<groupId>org.oxpath</groupId>
	<artifactId>oxpath-core</artifactId>
	<version>2.2.1</version>
</dependency>
<dependency>
	<groupId>org.oxpath</groupId>
	<artifactId>webapi</artifactId>
	<version>1.4.1</version>
</dependency>
```

To specify the output handler, which can be used to convert the OXPath output tree, add a relevant dependency statement. All available output handlers can be found in the directory [output-handlers](output-handlers).

An example for the OXPath XML Output Handler:
```xml
<dependency>
	<groupId>org.oxpath</groupId>
	<artifactId>oxpath-output-xml</artifactId>
	<version>1.0.1</version>
</dependency>
```

## Documentation and References

* The Javadoc API documents are available [online](https://oxpath.github.io/api-docs/oxpath-project/1.0.4/javadocs/index.html).
* In the user manual "[Introduction to OXPath](https://arxiv.org/abs/1806.10899)" (2018) you can find a detail description of OXPath with various examples.
* See also Furche et al. "[OXPath: A language for scalable data extraction, automation, and crawling on the deep web](https://dl.acm.org/doi/10.1007/s00778-012-0286-6)" (2013).

## OXPath Syntax Highlighting

The OXPath syntax highlighting, [language-oxpath package](https://github.com/neumannm/language-oxpath), is implemented for [Atom Editor](https://atom.io/) by [Mandy Neumann](https://ir.web.th-koeln.de/people/mandy-neumann/).

## People

### Core Contributors

 * Andrew Sellers, the University of Oxford
 * [Giovanni Grasso](http://www.giovannigrasso.it), the University of Oxford & Meltwater
 * [Tim Furche](http://furche.net/), the University of Oxford & Meltwater
 * [Ruslan Fayzrakhmanov](https://www.cs.ox.ac.uk/people/ruslan.fayzrakhmanov/), the University of Oxford & QuantumBlack (a McKinsey company). _The main contact person for the open source version (ruslan.fayzrakhmanov AT cs.ox.ac.uk)_
 * [Giorgio Orsi](http://orsigiorgio.net), the University of Oxford & Meltwater
 * Christian Schallhart, the University of Oxford

A complete list of authors and contributors is in [CONTRIBUTORS.md](CONTRIBUTORS.md).

### Project Leaders
 * [Georg Gottlob](https://www.dbai.tuwien.ac.at/staff/gottlob/), the University of Oxford & TU Wien
 * [Tim Furche](http://furche.net/), the University of Oxford & Meltwater

## License

Copyright (C) 2016-2019, [OXPath Team](https://github.com/oxpath/).

This project is licensed under the 3-Clause BSD License.
See the top-level file [LICENSE.md](LICENSE.md) and [LICENSE-3RD-PARTY.md](LICENSE-3RD-PARTY.md) (for used third-party software) for details.
