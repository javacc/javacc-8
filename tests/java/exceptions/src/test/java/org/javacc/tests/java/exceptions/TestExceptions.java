package org.javacc.tests.java.exceptions;

import static org.fest.reflect.core.Reflection.constructor;
import static org.fest.reflect.core.Reflection.type;
import static org.fest.reflect.core.Reflection.method;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
	private static final String TARGET = "targetbis";
	private static final String CLASSES = "classes";

	private static final String GENERATED_SOURCES = "generated-sources";
	private static final String JAVACC = "javacc";
	private static File source_directory = new File(".");
	private static File output_directory = new File(".");
	private static File output_classes = new File(".");

	static {
		output_classes = new File(output_classes, TARGET);
		output_classes = new File(output_classes, CLASSES);

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
				if (f.getName().endsWith(pattern)) {
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
		output_classes.mkdirs();
		String[] args = new String[7];
		args[0] = "-s";
		args[1] = output_directory.getCanonicalPath();
		args[2] = "-cp";
		args[3] = output_classes.getCanonicalPath();
		args[4] = "-d";
		args[5] = output_classes.getCanonicalPath();
		for (File file : files) {
			args[6] = file.getCanonicalPath();
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

	// @Test
	// public void test() {
	public static void main(String[] args) throws Exception {
		TestExceptions te = new TestExceptions();
		te.java();
		URLClassLoader classLoader = null;
		try {
			URL[] url = { output_classes.toURI().toURL() };

			classLoader = new URLClassLoader(url);
			Class<?> parserClass = classLoader.loadClass("Parser");
			Method meth = parserClass.getMethod("main", String[].class);
			String[] params = new String[0]; // init params accordingly
			meth.invoke(null, (Object) params); // static method doesn't have an instance

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
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
