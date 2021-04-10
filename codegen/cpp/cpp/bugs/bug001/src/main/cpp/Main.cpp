#include <iostream>
#include <fstream>
#include <iomanip>
#include <string>

#include "JavaCC.h"
#include "ComplexLineCommentTokenManager.h"
#include "ParseException.h"
#include "StreamReader.h"
#if !defined(JJ8) && !defined(JJ7)
#define JJ8
#endif
#if		defined(JJ8)
#include "DefaultCharStream.h"
#define CHARSTREAM DefaultCharStream
#elif 	defined(JJ7)
#include "CharStream.h"
#define CHARSTREAM CharStream
#endif
#include "ComplexLineComment.h"

using namespace std;

JJString ReadFileFully() {
	JJString code;
	code = 
#if 1
//"-- 32-bit.\n"
//"-- 32- -bit.\n"
//"-- 32- -bit-\n"
//"-- ddd -- Mod\n"
//"-- no-de --\n"
" M DEFINITIONS ::=\n"
"BEGIN\n"
"IMPORTS -- n-o --;\n"
"END\n"
;
#endif

	return code;
}
static void usage(int argc, char**argv) {
	cerr << "ComplexLineComment" << " [ in [ out [ err ] ] ]" << endl;
}
int main(int argc, char**argv) {
	istream*	input  = &cin;
	ostream*	output = &cout;
	ostream*	error  = &cerr;
	ifstream	ifs;
	ofstream	ofs;
	ofstream	efs;
	StreamReader*	sr = nullptr;
	CharStream *	cs = nullptr;

	try {
		if (argc > 1) {
			switch(argc) {
				case 4: efs.open(argv[3]);
				case 3: ofs.open(argv[2]);
				case 2: ifs.open(argv[1], ifstream::binary);
			}
			if (ifs.is_open()) {
				input = &ifs;
				sr = new StreamReader(ifs);
				cs = new CHARSTREAM(sr);
			}
			else {
				cerr << "cannot open in file" << endl;
				return 8;
			}
			if (ofs.is_open()) {
				output = &ofs;
			}
			if (efs.is_open()) {
				error = &efs;
			}
		} else
		if (argc == 1) {
			JJString s = ReadFileFully();
			*output << s << endl;
			cs = new CHARSTREAM(s.c_str(), s.size() - 1, 1, 1);
		}
		else {
			usage(argc, argv);
			return 0;
		}
		ComplexLineCommentTokenManager *scanner = new ComplexLineCommentTokenManager(cs);
		scanner->disable_tracing();
		ComplexLineComment parser(scanner);

		parser.Input();
     	*output << "Parser Version 0.1:  file parsed successfully." << endl;
	} catch (const ParseException& e) {
		clog << e.expectedTokenSequences << endl;
	}
	catch (...) {

	}
	if (ifs.is_open()) ifs.close();
	if (ofs.is_open()) ofs.close();
	if (efs.is_open()) efs.close();
	if (cs) delete cs;
	if (sr) delete sr;

	return 0;
}
#if 0
package clc;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;

public class ComplexLineComment {

  public static void main(String args[]) throws FileNotFoundException {
    InputStream input = System.in;
    PrintStream output = System.out;
    PrintStream error = System.err;
    InputStream prevInput = null;
    PrintStream prevOutput = null;
    PrintStream prevError = null;
    if (args.length == 3) {
    	prevInput = input;   	input = new FileInputStream(args[0]);
    	prevOutput = output;   	output = new PrintStream(args[1]);
    	prevError = error;   	error  = new PrintStream(args[2]);
       	System.setIn(input);
       	System.setOut(output);
       	System.setErr(error);
    }
    try {
	    ComplexLineComment parser = new ComplexLineComment(System.in);
    	parser.Input();
    } catch (Exception e) {
      	error.println(e.getMessage());
    } finally {
        if (prevInput != null)  System.setIn(prevInput);
        if (prevOutput != null) System.setOut(prevOutput);
        if (prevError != null)  System.setErr(prevError);
    }
  }

}

#endif