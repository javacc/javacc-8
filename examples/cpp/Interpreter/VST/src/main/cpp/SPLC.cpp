#include <iostream>
#include <fstream>
#include <iomanip>
#include <iostream>
#include <string>
#include <stdlib.h>
#include "SPLParser.h"

#include "JavaCC.h"
#include "SPLParserTokenManager.h"
#include "ParseException.h"
#include "DumpVisitor.h"
#include "Interpret.h"
#include "StreamReader.h"
using namespace std;

JJString ReadFileFully() {
	JJString code;
#if 0
	code =
		"int n;\n"
		"int fact;\n"
		"read n;\n"
		"fact = 1;\n"
		"while (n > 1)\n"
		"{\n"
		"		fact = fact * n;\n"
		"		n = n - 1;\n"
		"}\n"
		"write fact;\n";
#else
	code = "int foo;\nfoo = 3 + 4 * 5 + 6;\nwrite foo;\n";
#endif

	return code;
}
static void usage(int argc, char**argv) {
	cerr << "SPL" << " spl [ in out err ]" << endl;
}
int main(int argc, char**argv) {
	istream*	input  = &cin;
	ostream*	output = &cout;
	ostream*	error  = &cerr;
	ifstream	spl;
	ifstream	ifs;
	ofstream	ofs;
	ofstream	efs;
	StreamReader*	sr = nullptr;
	CharStream *	cs = nullptr;

	try {
		if (argc == 5) {
			spl.open(argv[1]);
			ifs.open(argv[2]);
			ofs.open(argv[3]);
			efs.open(argv[4]);
			if (ifs.is_open() && ofs.is_open() && efs.is_open() && spl.is_open()) {
				input = &ifs;	output = &ofs;	error = &efs;
				sr = new StreamReader(spl);
				cs = new CharStream(sr);
			}
			else {
				cerr << "cannot open spl or in or out or err file" << endl;
				return 8;
			}
		} else
		if (argc == 2) {
			JJString s = ReadFileFully();
			cout << s << endl;
			cs = new CharStream(s.c_str(), s.size() - 1, 1, 1);
		}
		else {
			usage(argc, argv);
			return 0;
		}

		SPLParserTokenManager *scanner = new SPLParserTokenManager(cs);
		SPLParser parser(scanner);
		parser.CompilationUnit();
		Node* n = parser.rootNode();
		DumpVisitor dumpVisitor;
		Interpret interpret(*input, *output, *error);
		n->jjtAccept(&interpret, nullptr);
	} catch (const ParseException& e) {
		clog << e.expectedTokenSequences << endl;
	}
	catch (...) {

	}
	if (spl.is_open()) spl.close();
	if (ifs.is_open()) ifs.close();
	if (ofs.is_open()) ofs.close();
	if (efs.is_open()) efs.close();
	if (cs) delete cs;
	if (sr) delete sr;

	return 0;
}
