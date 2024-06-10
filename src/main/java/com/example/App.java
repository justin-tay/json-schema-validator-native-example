package com.example;

import java.util.Set;

import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.regex.JDKRegularExpressionFactory;

/**
 * Application.
 */
public class App {
	public static void main(String[] args) {
		JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
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
		SchemaValidatorsConfig config = SchemaValidatorsConfig.builder()
				.regularExpressionFactory(JDKRegularExpressionFactory.getInstance()).build();
		JsonSchema schema = factory.getSchema(schemaData, config);
		
		String inputData = """
				{
				  "startDate": "10-Jan-2024",
				  "name": "1"
				}
				""";
		Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON, executionContext -> {
			executionContext.getExecutionConfig().setFormatAssertionsEnabled(true);
		});
		System.out.println(messages);
	}
}
