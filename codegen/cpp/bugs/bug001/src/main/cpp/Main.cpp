#include <iostream>
#include <fstream>
#include <iomanip>
#include <string>

#include "JavaCC.h"
#include "TyperTokenManager.h"
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
#include "Typer.h"

using namespace std;
using namespace ASN1::Typer;

JJString ReadFileFully() {
	JJString code;
	code = 
#if 1
//"-- 32-bit.\n"
//"-- 32- -bit.\n"
//"-- 32- -bit-\n"
//"-- ddd -- Mod\n"
//"-- no-de --\n"
" M DEFINITIONS ::=\n"
"BEGIN\n"
"IMPORTS -- n-o --;\n"
"END\n"
;
#else
"\r\n"
"-- OK: Everything is fine\r\n"
"\r\n"
"-- iso.org.dod.internet.private.enterprise (1.3.6.1.4.1)\r\n"
"-- .spelio.software.asn1c.test (9363.1.5.1)\r\n"
"-- .10\r\n"
"\r\n"
"ModuleTestInt-10\r\n"
"	{ iso org(3) dod(6) internet (1) private(4) enterprise(1)\r\n"
"		spelio(9363) software(1) asn1c(5) test(1) 10 }\r\n"
"	DEFINITIONS ::=\r\n"
"BEGIN\r\n"
"\r\n"
"	alpha INTEGER ::= 1\r\n"

"	Type1 ::= INTEGER { alpha(2) }\r\n"
"	Type2 ::= INTEGER { alpha(3), beta(alpha) }\r\n"
"	gamma Type2 ::= beta	-- equals 1 --\r\n"
"	delta Type2 ::= alpha	-- equals 3 --\r\n"
"\r\n"

"	/*\r\n"
"	 *  The following are for post-fix checking by the check_fixer.\r\n"
"	 * It will be able to pick-up these values if the file is parseable,\r\n"
"	 * even if it contains some semantic errors.\r\n"
"	 */\r\n"
"\r\n"
"	check-gamma INTEGER ::= 1	-- check value\r\n"
"	check-delta INTEGER ::= 3	-- check value\r\n"
"\r\n"

"END\r\n"
#endif

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
		TyperTokenManager *scanner = new TyperTokenManager(cs);
		scanner->disable_tracing();
		Typer parser(scanner);

		parser.ModuleDefinitionList();
     	*output << "Parser Version 0.1:  file parsed successfully." << endl;
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
