//============================================================================
// Name        : SPLC.cpp
// Author      : 
// Version     :
// Copyright   : Your copyright notice
// Description : Hello World in C++, Ansi-style
//============================================================================

#include <iostream>
#include <fstream>
#include <iomanip>
#include <iostream>
#include <string>
#include <stdlib.h>
#include "SPLParser.h"
using namespace std;

#include "JavaCC.h"
#include "SPLParserTokenManager.h"
#include "ParseException.h"
#include "DefaultCharStream.h"

JJString ReadFileFully(char *file_name) {
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

int main(int argc, char**argv) {
	JJString s = ReadFileFully(argv[1]);
	try {
		CharStream *stream = new DefaultCharStream(s.c_str(), s.size() - 1, 1, 1);
		TokenManager *scanner = new SPLParserTokenManager(stream);
		SPLParser parser(scanner);
		parser.CompilationUnit();
		Node* n = parser.rootNode();
	} catch (const ParseException& e) {

	}
	return 0;
}
