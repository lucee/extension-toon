# TOON Extension for Lucee

Serialize and deserialize [TOON](https://toonformat.dev) (Token-Oriented Object Notation) - an LLM-optimized format that uses ~40-50% fewer tokens than JSON.

Uses the [json-io](https://github.com/jdereg/json-io) library

## Installation

Download the `.lex` file from releases and install via Lucee Admin, or drop it into your server's `deploy` directory.

## Usage

### SerializeTOON

```cfml
result = SerializeTOON( var [, options] )
```

Arguments:

- **var** - CFML data to serialize (struct, array, or any value) (required)
- **options** - Optional struct with serialization options:
  - `prettyPrint` - boolean (default false). Format output for readability

### DeserializeTOON

```cfml
result = DeserializeTOON( source [, options] )
```

Arguments:

- **source** - TOON string content or file path to read from (required)
- **options** - Optional struct with deserialization options:
  - `charset` - string (default UTF-8). Character encoding when reading from file

## Examples

### Basic Serialization

```cfml
data = { name: "Zac", age: 42, tags: [ "lucee", "cfml" ] };

// Serialize to TOON
toon = SerializeTOON( data );

// With pretty print
toon = SerializeTOON( data, { prettyPrint: true } );
```

### Basic Deserialization

```cfml
// From string
toonString = '(name:"Zac" age:42 tags:["lucee" "cfml"])';
data = DeserializeTOON( toonString );

// From file
data = DeserializeTOON( "/path/to/data.toon" );

// With charset option
data = DeserializeTOON( "/path/to/data.toon", { charset: "UTF-16" } );
```

## Why TOON?

TOON is designed for LLM (Large Language Model) communication:

- **40-50% fewer tokens** than JSON
- No quotes around keys
- No commas between elements
- Compact representation

Example comparison:

```json
{"name":"Zac","age":42,"tags":["lucee","cfml"]}
```

```toon
(name:"Zac" age:42 tags:["lucee" "cfml"])
```

## Known Issues

- **Circular references not supported** - Data structures with circular references will cause a StackOverflowError. This is due to a [bug in json-io's ToonWriter](https://github.com/jdereg/json-io) where cycle detection is not applied to Map/Collection types.
- **Currently only for Lucee 7.1.0.21+** due to [OSGI tech debt](https://luceeserver.atlassian.net/browse/LDEV-6044), only fixed in 7.1 

## Dependencies

Uses [json-io](https://github.com/jdereg/json-io) (bundled, Apache 2.0 license).

## License

Apache License 2.0
