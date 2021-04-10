#include <iostream>
#include <fstream>
#include <iomanip>
#include <string>

#include "JavaCC.h"
#include "BugTokenManager.h"
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
#include "Bug.h"

using namespace std;

JJString ReadFileFully() {
	JJString code;
	code = 
"{ c }\n"
;

	return code;
}
static void usage(int argc, char**argv) {
	cerr << "Parser" << " [ in [ out [ err ] ] ]" << endl;
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
		TokenManager *scanner = new BugTokenManager(cs);
#if		defined(JJ8)
		scanner->disable_tracing();
#endif
		Bug parser(scanner);

		parser.EnumerationItem();
     	*output << "Parser :  file parsed successfully." << endl;
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
