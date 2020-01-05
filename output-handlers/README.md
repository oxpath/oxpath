# OXPath Output Handlers

This directory contains OXPath output handlers.
  * `oxpath-output-xml` for the XML serialisation of the OXPath output.
  * `oxpath-output-json` for the JSON serialisation of the OXPath output.
  * `oxpath-output-recstream` for streaming basic records from the OXPath output.
  * `oxpath-output-recstream-csv` and `oxpath-output-recstream-jdbc` for streaming records identified by `oxpath-output-recstream` into the CSV format and relational DB, respectively.
  * `oxpath-output-relation` to serialise entities in the OXPath output into a denormalised relation.
  * `oxpath-output-hierarchy-csv` and `oxpath-output-hierarchy-jdbc` use the relation from `oxpath-output-relation` and serialise it into the CSV format and relational database, respectively.
