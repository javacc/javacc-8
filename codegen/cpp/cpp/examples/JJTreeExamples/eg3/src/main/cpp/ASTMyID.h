/*
 * ASTMyID.h
 *
 *  Created on: 28 mars 2014
 *      Author: FrancisANDRE
 */

#ifndef ASTMYID_H_
#define ASTMYID_H_
#include "ParserTree.h"

namespace EG3 {

class ASTMyID : public Node {
private:
	JAVACC_STRING_TYPE name;
public:
	ASTMyID(int i);
	ASTMyID(Parser *p, int id);
	virtual ~ASTMyID();

	void setName(JAVACC_STRING_TYPE image);
	JAVACC_STRING_TYPE toString() const;
};

} /* namespace @NAMESPACE@ */

#endif /* ASTMYID_H_ */
