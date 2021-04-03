#include <fstream>
#include <iomanip>
#include <iostream>
#include <string>
#include <stdlib.h>
#include "Parser.h"
#include "EG4DumpVisitor.h"
#include "ParseException.h"
#include "MyErrorHandler.h"
#include "ParserTokenManager.h"
#include "DefaultCharStream.h"

using namespace std;
using namespace EG4;

JAVACC_STRING_TYPE ReadFileFully(char *file_name) {
	return "(1 + 2) * (a + b);\n";
}

int main(int argc, char** argv) {
	cout << "Reading from standard input..." << endl;
	JAVACC_STRING_TYPE s = ReadFileFully(argv[1]);
	try {
		CharStream *stream = new DefaultCharStream(s.c_str(), s.size() - 1, 1, 1);
		TokenManager *scanner = new ParserTokenManager(stream);
		Parser parser(scanner);
		parser.setErrorHandler(new MyErrorHandler());
		ASTStart* n = parser.Start();
		EG4DumpVisitor eg4dv;
		eg4dv.visit(n, NULL);
		cout << "Thank you." << endl;
	} catch (const ParseException& e) {

	}
	return 0;
}

