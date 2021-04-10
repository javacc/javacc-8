#ifndef MY_ERROR_HANDLER
#define MY_ERROR_HANDLER

#include "ParserErrorHandler.h"

namespace java { namespace parser {

class MyErrorHandler: public ParserErrorHandler {
public:
	MyErrorHandler() {}
	virtual ~MyErrorHandler() {}
	virtual void unexpectedToken(const JJString& expectedImage, const JJString& expectedLabel, const JJString& actualImage, const JJString& actualLabel, const Token* actualToken){}
	virtual void parseError(const Token* last, const Token* unexpected, const JJSimpleString& production){}
	virtual void otherError(const JJString& message){}

};

} }
#endif