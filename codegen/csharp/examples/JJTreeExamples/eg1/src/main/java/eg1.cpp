#include <fstream>
#include <iomanip>
#include <iostream>
#include <string>
#include <stdlib.h>
#include "JavaCC.h"
#include "Parser.h"
#include "ParseException.h"
#include "ParserTokenManager.h"
#include "DefaultCharStream.h"

using namespace std;
using namespace EG1;

JAVACC_STRING_TYPE ReadFileFully(char *file_name) {
	return "(1 + 2) * (a + b);\n";
}

int main(int argc, char** argv) {
	JAVACC_STRING_TYPE s = ReadFileFully(argv[1]);
	try {
		CharStream *stream = new DefaultCharStream(s.c_str(), s.size() - 1, 1, 1);
		TokenManager *scanner = new ParserTokenManager(stream);
		Parser parser(scanner);
		Node* n = parser.Start();
		n->dump("");
		cout << "Thank you." << endl;
	} catch (const ParseException& e) {

	}
	return 0;
}

