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

JAVACC_STRING_TYPE ReadFileFully(char *file_name) {
	return "(1 + 2) * (a + b);\n";
}

int main(int argc, char**argv) {
	JAVACC_STRING_TYPE s = ReadFileFully(argv[1]);
	try {
		CharStream *stream = new CharStream(s.c_str(), s.size() - 1, 1, 1);
		SPLParserTokenManager *scanner = new SPLParserTokenManager(stream);
		SPLParser parser(scanner);
		parser.CompilationUnit();
		Node* n = parser.rootNode();
	} catch (const ParseException& e) {

	}
return 0;
}
