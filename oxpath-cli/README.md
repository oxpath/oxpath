# OXPath CLI 1.0.2
It implements the command line interface for OXPath 2.2.0.

After the packaging phase, an executable jar file [`oxpath-cli.jar`](https://sourceforge.net/projects/oxpath/files/oxpath-cli/1.0.2/oxpath-cli.jar/download) can be found in the target directory.
Current version can only be executed on Linux.

## Execution

### Usage

`java -jar oxpath-cli.jar` or `java -jar oxpath-cli.jar -h` outputs the following:

```bash
usage: oxpath-cli [options]

 -autocomplete                           select first autocomplete
                                         suggestion (disabled by default)
 -conf <arg>                             configuration file
 -d <display number>                     the display port to use in the
                                         XVFB mode, e.g., :0 (not
                                         specified by default)
 -exe <file>                             a custom executable path for
                                         Firefox (not specified by
                                         default)
 -f <xml,json,rscsv,rsjdbc,hcsv,hjdbc>   output extracted data in a
                                         specified format("xml" is
                                         default). rscsv and rsjdbc stream
                                         basic records into CSV or
                                         relational database,
                                         respectively. hcsv and hjdbc
                                         serialise hierarchy of entities
                                         into a single denormalised
                                         relation in the form of CSV or
                                         into the relational database
 -h                                      print this help message
 -hents <string>                         list of attributes to be
                                         extracted in a form a/b,c/d
 -ht <integer>                           specify the browser window height
                                         (default is 800)
 -img                                    enable browser images download
                                         (disabled by default)
 -jdbcpar <XML file>                     XML file with JDBC parameters,
                                         such as db.driver, db.url,
                                         db.user, db.password,
                                         db.schema.schema-name ("public"
                                         is default),
                                         db.schema.table-name,
                                         db.override, db.batch-size
 -jsonarr                                use array for values
 -lat <float>                            Latitude (not specified by
                                         default)
 -log <file>                             log4j configuration file. If not
                                         provided, a default one is
                                         applied
 -lon <float>                            Longitude (not specified by
                                         default)
 -mval                                   allow multiple values per
                                         attribute (disabled by default)
 -o <file>                               the path of the output file in
                                         case of XML, CSV or JSON output
                                         handler (not specified by
                                         default, output into the default
                                         console)
 -pl                                     enable browser's plugins, e.g.,
                                         flash, java (disabled by default)
 -q <file>                               file containing the oxpath
                                         expression to evaluate
 -rsattrs <string>                       list of attributes to be
                                         extracted in a form a,b,c,d
 -rsent <string>                         name of the entity to be
                                         extracted
 -v                                      print product version and exit
 -var <file>                             file containing a variable
                                         dictionary (Java Properties
                                         format) for an oxpath template
 -wh <integer>                           specify the browser window width
                                         (default is 1280)
 -xmlcd                                  If XML mode is enabled, the
                                         attribute's value is wrapped into
                                         CDATA sections, useful when the
                                         output contains not allowed XML
                                         characters (disabled by default)
 -xvfb                                   Firefox running in the X virtual
                                         framebuffer (disabled by default)
```

### Examples

#### XML

XML output can be generated with the following parameters:

`java -jar oxpath-cli.jar -q <oxpath_expression> -mval -f xml -o <output_file>`

#### JSON

Example of the JSON output parameters:

`java -jar oxpath-cli.jar -q <oxpath_expression> -mval -f json -jsonarr -o <output_file>`

It is important to set the flag `-jsonarr` if `-mval` (multiple values per attribute) is set.
It will use array structures for values of attributes.

#### CSV

There are two approaches to CSV serialisation.
The first one outputs an entity specified by the parameter `-rsent` along with its attributes in `-rsattrs`.

`java -jar oxpath-cli.jar -q <oxpath_expression> -mval -f rscsv -rsent journal -rsattrs name,url -o <output_file>`

The second approach outputs entities specified by the parameter `-hents`.
It is a list of relative paths within the OXPath output tree which set a relation between entities to be serialised in a form of denormalised relation.
The first path of the top entity is relative to the root, while each subsequent path is relative to the preceding entity.

`java -jar oxpath-cli.jar -q <oxpath_expression> -mval -f hcsv -hents main,journals/journal -o <output_file>`

#### Relational Database

There are two possible approaches to transferring the output to the database.
The first approach outputs a basic record as in the first approach of the CSV output.

`java -jar oxpath-cli.jar -q <oxpath_expression> -mval -f rsjdbc -rsent journal -rsattrs name,url -jdbcpar <db_parameters>`

The second approach saves data in the database as in the second approach of CSV serialisation.

`java -jar oxpath-cli.jar -q <oxpath_expression> -mval -f hjdbc -hents main,journals/journal -jdbcpar <db_parameters>`

#### Wrapper and Database Parameters

Example of an OXPath expression:
```xml
doc("http://dblp.dagstuhl.de/db/journals/?pos=1")
  //header[@class~="headline"]
  :<main>
  [
    .:<title=string(./h1)>
    :<journals>
    [./..//div[@class="hide-body"]//a
      :<journal>[.:<name=string(.)>:<url=string(@href)>]
    ]
  ]
```

Database parameters are in the following form:
```xml
<diadem>
  <db>
    <driver>...</driver>
    <url>...</url>
    <user>...</user>
    <password>...</password>
    <schema>
      <schema-name>...</schema-name>
      <table-name>...</table-name>
    </schema>
    <override>...</override>
    <batch-size>...</batch-size>
  </db>
</diadem>
```

`override` is either `true` or `false`.
If it is `true`, the table will be dropped and created again just before sending data.
If it is `false`, the data will be appended to the table.
Regardless of the value of `override`, both schema and table will be always created in case they are not found in the database.

`batch-size` is an integer value that defines a minimal set of records to be commited at once into the database.