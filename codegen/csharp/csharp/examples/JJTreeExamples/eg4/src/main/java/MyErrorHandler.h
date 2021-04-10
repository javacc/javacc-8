/*
 * MyErrorHandler.h
 *
 *  Created on: 12 avr. 2014
 *      Author: FrancisANDRE
 */

#ifndef MYERRORHANDLER_H_
#define MYERRORHANDLER_H_
#include "ParserErrorHandler.h"

namespace EG4 {

class MyErrorHandler : public ParserErrorHandler {
public:
	MyErrorHandler();
	virtual ~MyErrorHandler();
	virtual void unexpectedToken(int expectedKind, const JJString& expectedImage, const JJString& expectedLabel, const Token* actual);
	virtual void parseError(const Token* last, const Token* unexpected, const JJSimpleString& production);
	virtual void otherError(const JJString& message);
};

} /* namespace EG4 */

#endif /* MYERRORHANDLER_H_ */
