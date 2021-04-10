#include <fstream>
#include <iomanip>
#include <iostream>
#include <string>
#include <stdlib.h>
#include "Parser.h"
#include "ParseException.h"
#include "ParserTree.h"
#include "ParserTokenManager.h"
#include "DefaultCharStream.h"

using namespace std;
using namespace EG2;

JAVACC_STRING_TYPE ReadFileFully(char *file_name) {
	return "1 + 2 * (a + b);\n";//s;
}

int main(int argc, char** argv) {
	cout << "Reading from standard input..." << endl;
	JAVACC_STRING_TYPE s = ReadFileFully(argv[1]);
	try {
	CharStream *stream = new DefaultCharStream(s.c_str(), s.size() - 1, 1, 1);
	TokenManager *scanner = new ParserTokenManager(stream);
	Parser parser(scanner);
	ASTStart* n = parser.Start();
	n->dump("");
	cout << "Thank you." << endl;
	} catch (const ParseException& e) {

	}
	return 0;
}

