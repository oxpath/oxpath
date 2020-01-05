# Change Log


## OXPath Project 1.0.4 (OXPath Core 2.2.1, Web API 1.4.1, Output Handlers, Util 1.1.1, OXPath CLI 1.0.2, Browser Installer 1.0.0)
_2020-01-05_
 * Web Driver Environment is removed form the project.
 * Browser Installer is introduced to instal Firefox on Linux machine.
 * Support for OSX.


## OXPath Project 1.0.3 (OXPath Core 2.2.0, Web API 1.4.0, Output Handlers, Util 1.1.0, Web Driver Environment 1.0.4, OXPath CLI 1.0.1)
_2017-12-23_
 * Changing the configuration mechanism of Web API and OXPath and making it modular.
 * Refine browser initialisation.
 * Run all tests in the xvfb mode.


## OXPath Project 1.0.2 (OXPath Core 2.1.0, Web API 1.3.0, Output Handlers, Util 1.0.3, Web Driver Environment 1.0.3, OXPath CLI 1.0.0)
_2017-10-12_
 * Version is released under the 3-Clause BSD License by OXPath Team.
 * Original XML output handler is removed from OXPath.
 * Output handlers can be invoked now by the OXPath core in a spirit of callback functions (instead of the original socket-based approach).
 * Added different abstract classes and interfaces for output handlers in the OXPath core (package `uk.ac.ox.cs.diadem.oxpath.output`)
 * Different output handlers (the "output" directory):
   * `oxpath-output-json` for the JSON serialisation of the OXPath output.
   * `oxpath-output-recstream` for streaming basic records from the OXPath output.
   * `oxpath-output-recstream-csv` and `oxpath-output-recstream-jdbc` for streaming records identified by `oxpath-output-recstream` into the CSV format and relational DB, respectively.
   * `oxpath-output-relation` to serialise entities in the OXPath output into a denormalised relation.
   * `oxpath-output-hierarchy-csv` and `oxpath-output-hierarchy-jdbc` use the relation from `oxpath-output-relation` and serialise it into the CSV format and relational database, respectively.
   * `oxpath-output-xml` refined for Unicode characters.
   * Added the class `uk.ac.ox.cs.diadem.oxpath.output.OutputHandlerRouter` to combine different output handlers.
 * Original CLI code removed from the OXPath core and added a new oxpath client `oxpath-cli`, which can output different data formats, such as XML (`oxpath-output-xml`), JSON (`oxpath-output-json`), CSV (`oxpath-output-recstream-csv` and `oxpath-output-hierarchy-csv`), as well as exporting into the relational DB (`oxpath-output-recstream-jdbc` and `oxpath-output-hierarchy-jdbc`).
 * `webapi v.1.2.3`:
   * Selenium 2.43.1 and Firefox 28 replaced by Selenium 2.53.1 and Firefox 47.0.1, respectively.
   * Removed the dependency on lambdaj and hamcrest.
   * Removed htmlunit
 * The `environment` module has been removed.


## OXPath 2.0.1
_2016-09-30_
 * Dependencies between artifacts are corrected and refined
 * Added support for geolocalisation