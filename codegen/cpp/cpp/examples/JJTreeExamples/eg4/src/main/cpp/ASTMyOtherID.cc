/*
 * ASTMyOtherID.cpp
 *
 *  Created on: 27 mars 2014
 *      Author: FrancisANDRE
 */

#include "ASTMyOtherID.h"
#include "ParserVisitor.h"

namespace EG4 {

ASTMyOtherID::ASTMyOtherID(int id) : Node(id) {
}
ASTMyOtherID::ASTMyOtherID(Parser *p, int id) : Node(p, id){
}
ASTMyOtherID::~ASTMyOtherID() {
}

void
ASTMyOtherID::setName(JAVACC_STRING_TYPE image) {
	name = image;
}


JAVACC_STRING_TYPE
ASTMyOtherID::toString() const {
  return "Identifier: " + name;
}
/** Accept the visitor. **/
void*
ASTMyOtherID::jjtAccept(ParserVisitor *visitor, void * data) const {
  return visitor->visit(this, data);
}


} /* namespace EG4 */

