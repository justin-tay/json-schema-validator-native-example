package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.AbsoluteIri;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.OutputFormat;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.regex.ECMAScriptRegularExpressionFactory;
import com.networknt.schema.regex.GraalJSRegularExpressionFactory;
import com.networknt.schema.regex.JDKRegularExpressionFactory;
import com.networknt.schema.regex.JoniRegularExpressionFactory;
import com.networknt.schema.regex.RegularExpressionFactory;
import com.networknt.schema.resource.DefaultSchemaLoader;
import com.networknt.schema.serialization.JsonMapperFactory;
import com.networknt.schema.serialization.YamlMapperFactory;
import com.networknt.schema.utils.Classes;

/**
 * Application.
 */
public class App {
    private static final boolean JONI_PRESENT = Classes.isPresent("org.joni.Regex",
            ECMAScriptRegularExpressionFactory.class.getClassLoader());
    private static final boolean GRAALJS_PRESENT = Classes.isPresent("com.oracle.truffle.js.parser.GraalJSEvaluator",
            ECMAScriptRegularExpressionFactory.class.getClassLoader());

    public static RegularExpressionFactory getRegularExpressionFactory() {
        if (GRAALJS_PRESENT) {
            return GraalJSRegularExpressionFactory.getInstance();
        } else if (JONI_PRESENT) {
            return JoniRegularExpressionFactory.getInstance();
        } else {
            return JDKRegularExpressionFactory.getInstance();
        }
    }

    public static String getArgument(String[] args, String argument) {
        for (int x = 0; x < args.length; x++) {
            if (argument.equals(args[x]) && (args.length > x + 1)) {
                return args[x + 1];
            }
        }
        return null;
    }

    public static JsonSchema getSchema(String[] args) {
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder()
                .regularExpressionFactory(getRegularExpressionFactory()).build();

        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        String schema = getArgument(args, "-schema");
        if (schema == null) {
            // Default example
            String schemaData = """
                    {
                      "type": "object",
                      "properties": {
                        "startDate": {
                          "format": "date-time"
                        },
                        "name": {
                          "pattern": "[a-zA-Z ]+"
                        }
                      }
                    }
                    """;
            return factory.getSchema(schemaData, config);
        } else {
            return factory.getSchema(SchemaLocation.of(schema), config);
        }
    }

    public static JsonNode getInput(String[] args) throws IOException {
        String input = getArgument(args, "-input");
        if (input == null) {
            String inputData = """
                    {
                      "startDate": "10-Jan-2024",
                      "name": "1"
                    }
                    """;
            return JsonMapperFactory.getInstance().readTree(inputData);
        } else {
            DefaultSchemaLoader schemaLoader = new DefaultSchemaLoader(Collections.emptyList(),
                    Collections.emptyList());
            try (InputStream inputStream = schemaLoader.getSchema(AbsoluteIri.of(input)).getInputStream()) {
                if (input.endsWith(".yaml") || input.endsWith(".yml")) {
                    return YamlMapperFactory.getInstance().readTree(inputStream);
                } else {
                    return JsonMapperFactory.getInstance().readTree(inputStream);
                }
            }
        }
    }

    public static OutputFormat<?> getOutput(String[] args) {
        String output = getArgument(args, "-output");
        if ("hierarchical".equals(output)) {
            return OutputFormat.HIERARCHICAL;
        } else if ("list".equals(output)) {
            return OutputFormat.LIST;
        } else if ("flag".equals(output)) {
            return OutputFormat.FLAG;
        } else if ("boolean".equals(output)) {
            return OutputFormat.BOOLEAN;
        }
        return OutputFormat.DEFAULT;
    }

    public static void main(String[] args) throws IOException {
        JsonSchema schema = getSchema(args);
        JsonNode inputNode = getInput(args);
        OutputFormat<?> outputFormat = getOutput(args);
        Object result = schema.validate(inputNode, outputFormat, executionContext -> {
            executionContext.getExecutionConfig().setFormatAssertionsEnabled(true);
            executionContext.getExecutionConfig().setDebugEnabled(true);
        });
        if (result instanceof Collection<?> collection) {
            collection.forEach(System.out::println);
        } else {
            System.out.println(result);
        }
    }
}
