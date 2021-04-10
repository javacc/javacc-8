#include <iostream>
#include <fstream>
#include <iomanip>
#include <iostream>
#include <string>
#include <stdlib.h>

#include "Matcher.h"
#include "ParseException.h"
#include "StreamReader.h"
#include "MatcherTokenManager.h"
#include "DefaultCharStream.h"


using namespace std;
using namespace Basic;

JJString ReadFileFully() {
	JJString code;
	code = "{{}}\n";

	return code;
}
static void usage(int argc, char**argv) {
	cerr << "IDL" << " [ spl in out err ]" << endl;
}
int main(int argc, char**argv) {
	istream*	input = &cin;
	ostream*	output = &cout;
	ostream*	error = &cerr;
	ifstream	ifs;
	ofstream	ofs;
	ofstream	efs;
	StreamReader*	sr = nullptr;
	CharStream *	cs = nullptr;

	try {
		if (argc == 4) {
			ifs.open(argv[2]);
			ofs.open(argv[3]);
			efs.open(argv[4]);
			if (ifs.is_open() && ofs.is_open() && efs.is_open()) {
				input = &ifs;	output = &ofs;	error = &efs;
				sr = new StreamReader(ifs);
				cs = new DefaultCharStream(sr);
			}
			else {
				cerr << "cannot open spl or in or out or err file" << endl;
				return 8;
			}
		}
		else
			if (argc == 1) {
				JJString s = ReadFileFully();
				*output << s << endl;
				cs = new DefaultCharStream(s, 1, 1);
			}
			else {
				usage(argc, argv);
				return 0;
			}
		TokenManager *scanner = new ::MatcherTokenManager(cs);
		MyToken head;
		Matcher parser(scanner);
		parser.Input();
		*output << "IDL Parser Version 0.1:  IDL file parsed successfully." << endl;
	}
	catch (const ParseException& e) {
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
