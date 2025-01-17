package com.bakdata.conquery.models.preproc;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.bakdata.conquery.models.common.Range;
import com.bakdata.conquery.models.exceptions.validators.ExistingFile;
import com.bakdata.conquery.models.preproc.outputs.AutoOutput;
import com.bakdata.conquery.models.preproc.outputs.Output;
import com.bakdata.conquery.models.types.MajorTypeId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import groovy.lang.GroovyShell;
import io.dropwizard.validation.ValidationMethod;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import lombok.Data;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

@Data
public class Input implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private static final String[] AUTO_IMPORTS = Stream.of(
			LocalDate.class,
			Range.class
		).map(Class::getName).toArray(String[]::new);

	@NotNull @ExistingFile
	private File sourceFile;
	private String filter;
	@Valid
	private AutoOutput autoOutput;
	@NotNull @Valid
	private Output primary;
	@Valid
	private Output[] output;
	
	@JsonIgnore
	private transient GroovyPredicate script;

	@JsonIgnore
	@ValidationMethod(message="Each column requires a unique name")
	public boolean isEachNameUnique() {
		return IntStream
			.range(0, this.getWidth())
			.mapToObj(this::getColumnDescription)
			.map(ColumnDescription::getName)
			.distinct()
			.count()
			== this.getWidth();
	}

	@JsonIgnore
	@ValidationMethod(message = "Outputs must not be empty")
	public boolean isOutputsNotEmpty() {
		return checkAutoOutput() || (output != null && output.length > 0);
	}

	@JsonIgnore
	public boolean checkAutoOutput() {
		return autoOutput != null;
	}

	@JsonIgnore
	@ValidationMethod(message="The primary column must be of type STRING")
	public boolean isPrimaryString() {
		return primary.getResultType()==MajorTypeId.STRING;
	}
	
	public boolean filter(String[] row) {
		if(filter == null) {
			return true;
		}

		if(script==null) {
			try {
				CompilerConfiguration config = new CompilerConfiguration();
				config.addCompilationCustomizers(new ImportCustomizer().addImports(AUTO_IMPORTS));
				config.setScriptBaseClass(GroovyPredicate.class.getName());
				GroovyShell groovy = new GroovyShell(config);

				script = (GroovyPredicate) groovy.parse(filter);
			} catch(Exception|Error e) {
				throw new RuntimeException("Failed to compile filter '" + filter + "'", e);
			}
		}

		script.setRow(row);
		return script.run();
	}

	@JsonIgnore
	public int getWidth() {
		return checkAutoOutput()? autoOutput.getWidth() : getOutput().length;
	}

	public ColumnDescription getColumnDescription(int i) {
		return checkAutoOutput()? autoOutput.getColumnDescription(i) : output[i].getColumnDescription();
	}

	private Object2IntArrayMap<String> headers;

	@JsonIgnore
	public void setHeaders(Object2IntArrayMap<String> headersMap) {
		this.headers = Objects.requireNonNull(headersMap, "Headers may not be null.");

		primary.setHeaders(headersMap);

		for (Output op : output) {
			op.setHeaders(headersMap);
		}
	}

	public static Object2IntArrayMap<String> buildHeadersMap(String[] headers) {
		final Object2IntArrayMap<String> headersMap = new Object2IntArrayMap<>();
		headersMap.defaultReturnValue(-1);

		// TODO: 18.12.2019 consider pulling this up.
		for (int index = 0; index < headers.length; index++) {
			headersMap.put(headers[index], index);
		}
		return headersMap;
	}
}
