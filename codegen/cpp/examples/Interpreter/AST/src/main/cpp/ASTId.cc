/* Generated By:JJTree: Do not edit this line. ASTId.cc Version 8.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=true,NODE_PREFIX=AST,NODE_EXTENDS=MyNode,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
#include "ASTId.h"

  
  ASTId::ASTId(int id) : Node(id) {
  }
  ASTId::~ASTId() {
  }

  void ASTId::interpret()
  {
#ifdef FAE
     stack.push(symtab[name]);
#endif
  }

/* JavaCC - OriginalChecksum=bd3d395d6f064c5196b25f5420f54b9a (do not edit this line) */