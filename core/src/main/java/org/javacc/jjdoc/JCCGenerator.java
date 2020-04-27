package org.javacc.jjdoc;

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.javacc.parser.CppCodeProduction;
import org.javacc.parser.Expansion;
import org.javacc.parser.JavaCodeProduction;
import org.javacc.parser.Lookahead;
import org.javacc.parser.NonTerminal;
import org.javacc.parser.NormalProduction;
import org.javacc.parser.RegularExpression;
import org.javacc.parser.Token;
import org.javacc.parser.TokenProduction;

public class JCCGenerator implements Generator {

	private final JJDocContext context;
	private PrintStream ostr;

	public JCCGenerator(JJDocContext context) {
		this.context = context;
	}

	private void println() {
		ostr.println();
	}

	private void println(String s) {
		ostr.println(s);
	}
    private void print(int i) {
    	ostr.print(i);
	}

    private void println(int i) {
    	ostr.println(i);
	}


	@Override
	public void text(String s) {
	    if (!((s.length() == 1) && ((s.charAt(0) == '\n') || (s.charAt(0) == '\r')))) {
	        print(s);
	     }
	}

	@Override
	public void print(String s) {
		ostr.print(s);
	}

	@Override
	public void documentStart() {
		ostr = create_output_stream();
		println("PARSER_BEGIN(ChangeMe)");
		println("PARSER_END(ChangeMe)");
		println();
	}

	@Override
	public void documentEnd() {
		ostr.close();
	}

	@Override
	public void specialTokens(String s) {

	}

	@Override
	public void handleTokenProduction(TokenProduction tp) {
		if (tp.firstToken == null)
			return;
		int line = 1;
		for(Token token = tp.firstToken; token != tp.lastToken; token = token.next) {
			if (token.beginLine > line ) {
				println();
				line = token.beginLine;
			}
			print(token.toString());
		}
		println();
		println("}");
	}

	@Override
	public void nonterminalsStart() {
	}

	@Override
	public void nonterminalsEnd() {
	}

	@Override
	public void tokensStart() {
	}

	@Override
	public void tokensEnd() {
	}

	@Override
	public void javacode(JavaCodeProduction jp) {
	}

	@Override
	public void cppcode(CppCodeProduction cp) {
	}

    @Override
    public void lookAheadStart(Lookahead l) {
    	if (l.isExplicit()) {
    		print("LOOKAHEAD(");
    		print(l.getAmount());
    		print(") ");
    	}
    }

	@Override
    public void lookAheadEnd(Lookahead l) {
    }
  
	@Override
	public void productionStart(NormalProduction np) {
		print("void ");
		println(np.getLhs() + "() : {} {");
	}

	@Override
	public void productionEnd(NormalProduction np) {
		println();
		println("}");
	}

	@Override
	public void expansionStart(Expansion e, boolean first) {
		print("  ");
	}

	@Override
	public void expansionEnd(Expansion e, boolean first) {
	}

	@Override
	public void nonTerminalStart(NonTerminal nt) {
	}

	@Override
	public void nonTerminalEnd(NonTerminal nt) {
		print("()");
	}

	@Override
	public void reStart(RegularExpression re) {
	}

	@Override
	public void reEnd(RegularExpression re) {
	}

	@Override
	public void debug(String message) {

	}

	@Override
	public void info(String message) {

	}

	@Override
	public void warn(String message) {

	}

	@Override
	public void error(String message) {

	}

	protected PrintStream create_output_stream() {
		PrintStream ps;
		
		if (context.getOutputFile().equals("")) {
			if (JJDocGlobals.input_file.equals("standard input")) {
				return System.out;
			} else {
				String ext = ".bnf";
				int i = JJDocGlobals.input_file.lastIndexOf('.');
				if (i == -1) {
					JJDocGlobals.output_file = JJDocGlobals.input_file + ext;
				} else {
					String suffix = JJDocGlobals.input_file.substring(i);
					if (suffix.equals(ext)) {
						JJDocGlobals.output_file = JJDocGlobals.input_file + ext;
					} else {
						JJDocGlobals.output_file = JJDocGlobals.input_file.substring(0, i) + ext;
					}
				}
			}
		} else {
			JJDocGlobals.output_file = context.getOutputFile();
		}
		try {
			ostr = new java.io.PrintStream(JJDocGlobals.output_file);
		} catch (java.io.IOException e) {
			error("JJDoc: can't open output stream on file " + JJDocGlobals.output_file + ".  Using standard output.");
			ostr = System.out;
		}

		return ostr;
	}
}
