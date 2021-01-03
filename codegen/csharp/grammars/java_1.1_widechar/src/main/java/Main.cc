#include <fstream>
#include <iomanip>
#include <iostream>
#include <string>
#include <stdlib.h>
#if		defined(JJ7)
#define CHARSTREAM CharStream
#include "CharStream.h"
#elif	defined(JJ8)
#define CHARSTREAM DefaultCharStream
#include "DefaultCharStream.h"
#endif

#include "JavaParserConstants.h"
#include "JavaParser.h"
#include "JavaParserTokenManager.h"
#include "MyErrorHandler.h"

using namespace java::parser;
using namespace std;

static JJString readHelloWorld() {
	JJString s;
	s =
L"public class HelloWorld\n {\n"
L"	public static void main(String args[]) {\n"
L"		System.out.println(\"Hello World!\"); \n"
L"	}\n"
L"}\n";
	return s;
}
static JJString ReadFileFully(char *file_name) {
  JJString s;
  wifstream fp_in;
  fp_in.open(file_name, ios::in);
  // Very inefficient.
  while (!fp_in.eof()) {
   s += fp_in.get();
  }
  return s;
}

int main(int argc, char **argv) {
	JJString s;
  if (argc < 2)
	s = readHelloWorld();
  else
	s = ReadFileFully(argv[1]);
  std::wcout << s;
  CharStream *stream = new CHARSTREAM(s.c_str(), s.size() - 1, 1, 1);
  JavaParserTokenManager *scanner = new JavaParserTokenManager(stream);
  JavaParser parser(scanner);
  parser.CompilationUnit();
  Node *root = (Node*)parser.jjtree.peekNode();
#if 0
  if (root) {
    JJString buffer; 
   	root->dumpToBuffer(JJSPACE, JAVACC_WIDE(\n), &buffer);
    std::wcout << buffer << std::endl;
    root->dump(JJSPACE);
  }
#endif
}
