package org.javacc.tests.java.exceptions;
import static org.fest.reflect.core.Reflection.constructor;
import static org.fest.reflect.core.Reflection.type;
import static org.fest.reflect.core.Reflection.method;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.fest.reflect.reference.TypeRef;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestExceptions {
	private static final String SRC = "src";
	private static final String MAIN = "main";
	private static final String TEST = "test";
	private static final String TARGET = "target";
	private static final String GENERATED_SOURCES = "generated-sources";
	private static final String JAVACC = "javacc";
	private static File source_directory = new File(".");
	private static File output_directory = new File(".");
	private static File output_classes = new File(".");

	static {
		output_classes = new File(output_classes, TARGET);
		output_classes = new File(output_classes, "foo");

		source_directory = new File(source_directory, SRC);
		source_directory = new File(source_directory, MAIN);
		source_directory = new File(source_directory, JAVACC);

		output_directory = new File(output_directory, TARGET);
		output_directory = new File(output_directory, GENERATED_SOURCES);
		output_directory = new File(output_directory, JAVACC);
	}

	public static void search(final String pattern, final File folder, List<File> result) {
		for (final File f : folder.listFiles()) {
			if (f.isDirectory()) {
				search(pattern, f, result);
			}
			if (f.isFile()) {
				if (f.getName().matches(pattern)) {
					result.add(f);
				}
			}
		}
	}

	private List<File> javacc() throws Exception {
		List<String> arguments = new ArrayList<String>();
		arguments.add("-CODE_GENERATOR=Java");
		arguments.add("-OUTPUT_DIRECTORY=" + output_directory.getCanonicalPath());
		arguments.add(new File(source_directory, "Parser.jj").getCanonicalPath());
		// new org.javacc.jjtree.JJTree().main(arguments.toArray(new
		// String[arguments.size()]));
		// arguments.set(arguments.size() - 1, System.getProperty("user.dir") +
		// "/src/gen/SPL.jj");
		org.javacc.parser.Main.mainProgram(arguments.toArray(new String[arguments.size()]));

		List<File> files = new ArrayList<File>();
		search(".java", output_directory, files);
		return files;
	}

	private void java() throws Exception {
		List<File> files = javacc();

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		for (File file : files) {
			String[] args = { "-d", output_classes.getPath(), file.getPath() };
			compiler.run(System.in, System.out, System.err, args);
		}
	}

	@Before
	public void setUp() throws Exception {
		java();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		URLClassLoader classLoader = null;
		try {
			URL[] url = {  output_classes.toURI().toURL() };

			classLoader = new URLClassLoader(url);
			Class<?> parserClass = type("Parser").load();
			Object parser = constructor().withParameterTypes(InputStream.class).in(parserClass).newInstance(System.in);
			
			String[] args = new String[0];
			method("main").withParameterTypes(String[].class).in(parser).invoke(new Object[] { args });	

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} finally {
			if (classLoader != null)
				try {
					classLoader.close();
				} catch (IOException e) {
				}
		}

	}

}
