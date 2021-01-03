#ifndef MY_ERROR_HANDLER
#define MY_ERROR_HANDLER

#include "ParserErrorHandler.h"

namespace java { namespace parser {

class MyErrorHandler: public ParserErrorHandler {
	virtual void handleUnexpectedToken(int expectedKind, const JJString& expectedImage, const JJString& expectedLabel, const Token* actual, JavaParser* parser) { }
	virtual void handleParseError(const Token* last, const Token* unexpected, const JJString& production, JavaParser* parser) { }

};

} }
#endif