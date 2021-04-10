package test;
import java.io.Reader;
import java.io.StringReader;

public class Main {

	public static void main(String argv[]) throws ParseException {
		String code = "{ iso org(3) dod(6) }";
		Reader reader = new StringReader(code);
		Parser parser = new Parser(reader);
		parser.DefinitiveIdentifier();
	}

}
