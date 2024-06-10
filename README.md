# JSON Schema Validator Native Example

Example to demonstrate the JSON Schema Validator can compile and run as a native binary.

## Building

This project used GraalVM for JDK 21.

The prerequisites and instructions for compiling native images are listed at [Getting Started with GraalVM](https://www.graalvm.org/jdk21/docs/getting-started/).

#### JDK Regular Expressions

The default maven profile only enables the use of JDK Regular Expressions.

```shell
mvn clean
mvn -Pnative native:compile
```

#### GraalJS Regular Expressions

```shell
mvn clean
mvn -Pnative,graaljs native:compile
```

#### Joni Regular Expressions

```shell
mvn clean
mvn -Pnative,joni native:compile
```

## Running

The default will run against a sample schema and input.

```shell
cd target
json-schema-validator-native-example
```

The schema and input can be specified.

```shell
cd target
json-schema-validator-native-example -input file:///d:/input.json -schema file:///d:/schema.json
```