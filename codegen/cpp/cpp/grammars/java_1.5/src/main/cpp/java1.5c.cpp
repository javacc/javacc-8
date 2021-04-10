#include <iostream>
#include <fstream>
#include <string>
#include "JavaParser.h"

#include "JavaCC.h"
#include "JavaParserTokenManager.h"
#include "ParseException.h"
#include "StreamReader.h"
#include "Token.h"
#include "DefaultCharStream.h"


using namespace std;

JJString ReadFileFully() {
	JJString code;

	code =
		"import java.util.*;\n"

		"public class JavaGenerics\n"
		"{\n"
		"	private Map<String, List> map;\n"
		"\n"
		"	public void jriat()\n"
		"	{\n"
		"		int i = 1 >>> 2;\n"
		"		int j = 3 >> 4;\n"
		"		boolean b = 5 > 6;\n"
		"   }\n"
		"}\n";

	return code;
}
static void usage(int argc, const char** argv) {
}
using namespace Java;

int main(int argc, const char** argv) {
	istream*	input  = &cin;
	ostream*	output = &cout;
	ostream*	error  = &cerr;
	ifstream	ifs;
	ofstream	ofs;
	ofstream	efs;
	StreamReader*	sr = nullptr;
	CharStream *	cs = nullptr;

	try {
		if (argc == 4) {
			ifs.open(argv[1]);
			ofs.open(argv[2]);
			efs.open(argv[3]);
			if (ifs.is_open() && ofs.is_open() && efs.is_open()) {
				input = &ifs;	output = &ofs;	error = &efs;
				sr = new StreamReader(ifs);
				cs = new DefaultCharStream(sr);
			}
			else {
				cerr << "cannot open in or out or err file" << endl;
				return 8;
			}
		} else
		if (argc == 1) {
			JJString s = ReadFileFully();
			cout << s << endl;
			cs = new DefaultCharStream(s.c_str(), s.size() - 1, 1, 1);
		}
		else {
			usage(argc, argv);
			return 4;
		}

		TokenManager *scanner = new JavaParserTokenManager(cs);
		JavaParser parser(scanner);
		ofs << "parsing ";
		parser.CompilationUnit();
		ofs << "parsed" << endl;
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
