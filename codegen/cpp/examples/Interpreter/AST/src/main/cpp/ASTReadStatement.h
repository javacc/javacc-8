/* Generated By:JJTree: Do not edit this line. ASTReadStatement.h Version 8.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=true,NODE_PREFIX=AST,NODE_EXTENDS=MyNode,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
#pragma once

#include "Node.h"
using namespace std;


class ASTReadStatement : public Node {
public: 
  ASTReadStatement(int id);
  virtual ~ASTReadStatement();
  virtual void interpret();
  friend class SPLParser;

private:
	string name;

};

/* JavaCC - OriginalChecksum=ef4b584f32e27fe2f246071870b7eb5a (do not edit this line) */
