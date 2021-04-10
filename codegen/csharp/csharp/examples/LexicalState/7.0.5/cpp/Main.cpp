#include <iostream>
#include <fstream>
#include <iomanip>
#include <iostream>
#include <string>
#include <stdlib.h>
#include "SpecialToken.h"

#include "JavaCC.h"
#include "SpecialTokenTokenManager.h"
#include "ParseException.h"
#include "StreamReader.h"
#include "CharStream.h"
using namespace std;

JJString ReadFileFully() {
	JJString code;
	code =
"AA\n"
"/* kk */\n"
"DD -- ccc\n"
"BB\n"
"BB\n"
;
	return code;
}
static void usage(int argc, char**argv) {
	cerr << "IDL" << " [ spl in out err ]" << endl;
}
int main(int argc, char**argv) {
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
				cs = new CharStream(sr);
			}
			else {
				cerr << "cannot open spl or in or out or err file" << endl;
				return 8;
			}
		} else
		if (argc == 1) {
			JJString s = ReadFileFully();
			*output << s << endl;
			cs = new CharStream(s.c_str(), s.size() - 1, 1, 1);
		}
		else {
			usage(argc, argv);
			return 0;
		}
		TokenManager *scanner = new SpecialTokenTokenManager(cs);
		SpecialToken parser(scanner);
		parser.Input();
     	*output << "IDL Parser Version 0.1:  IDL file parsed successfully." << endl;
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
/*
public static void main(String args[]) {
    IDLParser parser;
    if (args.length == 0) {
      System.out.println("IDL Parser Version 0.1:  Reading from standard input . . .");
      parser = new IDLParser(System.in);
    } else if (args.length == 1) {
      System.out.println("IDL Parser Version 0.1:  Reading from file " + args[0] + " . . .");
      try {
        parser = new IDLParser(new java.io.FileInputStream(args[0]));
      } catch (java.io.FileNotFoundException e) {
        System.out.println("IDL Parser Version 0.1:  File " + args[0] + " not found.");
        return;
      }
    } else {
      System.out.println("IDL Parser Version 0.1:  Usage is one of:");
      System.out.println("         java IDLParser < inputfile");
      System.out.println("OR");
      System.out.println("         java IDLParser inputfile");
      return;
    }
    try {
      parser.specification();
      System.out.println("IDL Parser Version 0.1:  IDL file parsed successfully.");
    } catch (ParseException e) {
      System.out.println("IDL Parser Version 0.1:  Encountered errors during parse.");
    }
  }
  */
