/*
 * MyErrorHandler.cpp
 *
 *  Created on: 12 avr. 2014
 *      Author: FrancisANDRE
 */

#include "MyErrorHandler.h"

namespace EG4 {

MyErrorHandler::MyErrorHandler() {
}

MyErrorHandler::~MyErrorHandler() {
}
void MyErrorHandler::unexpectedToken(const JJString& expectedImage, const JJString& expectedLabel, const JJString& actualImage, const JJString& actualLabel, const Token* actualToken) {
}
void MyErrorHandler::parseError(const Token* last, const Token* unexpected, const JJSimpleString& production) {
}
void MyErrorHandler::otherError(const JJString& message) {
}

} /* namespace EG4 */
