#include <fstream>
#include <iomanip>
#include <iostream>
#include <string>
#include <stdlib.h>

#include "JavaParserConstants.h"
#include "CharStream.h"
#include "JavaParser.h"
#include "JavaParserTokenManager.h"
#include "ParseException.h"
#include "StreamReader.h"
#include "DefaultCharStream.h"
#include "MyErrorHandler.h"

using namespace java::parser;
using namespace std;

JAVACC_STRING_TYPE ReadFileFully() {
  JAVACC_STRING_TYPE code;
  code =
	  "package test;\n"
	  "public class HelloWorld {\n"
	  "	  public static void main(String[] args) {\n"
	  "	    System.out.println(); \n"
	  "	  }\n"
	  "}\n";
  return code;
}
static void usage(int argc, char** argv) {
}

int main(int argc, char **argv) {
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
				cs = new DefaultCharStream(sr);
			}
			else {
				cerr << "cannot open spl or in or out or err file" << endl;
				return 8;
			}
		} else
		if (argc == 2) {
			spl.open(argv[1]);
			sr = new StreamReader(spl);
			cs = new DefaultCharStream(sr);
		} else
		if (argc == 1) {
			JJString s = ReadFileFully();
			cout << s << endl;
			cs = new DefaultCharStream(s.c_str(), s.size() - 1, 1, 1);
		}
		else {
			usage(argc, argv);
			return 0;
		}

		TokenManager *scanner = new JavaParserTokenManager(cs);
		JavaParser parser(scanner);
	    parser.setErrorHandler(new MyErrorHandler());
	    parser.CompilationUnit();
	    Node *root = (Node*)parser.jjtree.peekNode();
		if (root) {
			JAVACC_STRING_TYPE buffer;
#if WIDE_CHAR
			//root->dumpToBuffer(L" ", L"\n", &buffer);
			//wcout << buffer << "\n";
			root->dump(L" ");
#else
			root->dumpToBuffer(" ", "\n", &buffer);
			printf("%s\n", buffer.c_str());
#endif
		}
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
