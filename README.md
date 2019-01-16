# infozilla
The infoZilla tool

## Quickstart
Run the tool against a sample bug report:

```
gradle run --args="demo/demo0001.txt"
```

## Building
Building the infoZilla tool requires `gradle` 4 or newer. Run `gradle tasks` for an overview.

## Usage
```
Usage: infozilla [-clps] [--charset=<inputCharset>] FILE...
      FILE...              File(s) to process.
      --charset=<inputCharset>
                           Character Set of Input (default=ISO-8859-1)
  -c, --with-source-code   Process and extract source code regions (default=true)
  -l, --with-lists         Process and extract lists (default=true)
  -p, --with-patches       Process and extract patches (default=true)
  -s, --with-stacktraces   Process and extract stacktraces (default=true)
```

## Citing
If you like the tool and find it useful, feel free to cite the original research work:
```
@conference {972,
	title = {Extracting structural information from bug reports},
	booktitle = {Proceedings of the 2008 international workshop on Mining software repositories  - MSR {\textquoteright}08},
	year = {2008},
	month = {05/2008},
	pages = {27-30},
	publisher = {ACM Press},
	organization = {ACM Press},
	address = {New York, New York, USA},
	abstract = {In software engineering experiments, the description of bug reports is typically treated as natural language text, although it often contains stack traces, source code, and patches. Neglecting such structural elements is a loss of valuable information; structure usually leads to a better performance of machine learning approaches. In this paper, we present a tool called infoZilla that detects structural elements from bug reports with near perfect accuracy and allows us to extract them. We anticipate that infoZilla can be used to leverage data from bug reports at a different granularity level that can facilitate interesting research in the future.},
	keywords = {bug reports, eclipse, enumerations, infozilla, natural language, patches, source code, stack trace},
	isbn = {9781605580241},
	doi = {10.1145/1370750.1370757},
	attachments = {https://flosshub.org/sites/flosshub.org/files/p27-bettenburg.pdf},
	author = {Premraj, Rahul and Zimmermann, Thomas and Kim, Sunghun and Bettenburg, Nicolas}
}

```
