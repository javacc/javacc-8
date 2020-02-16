#pragma once
#include "JavaCC.h"
#include "JavaParserConstants.h"

class MyToken : public Token {
public:
	MyToken(int kind, JJString image) {
		this->kind = kind;
		this->image = image;
	}
	static const int realKind = GT;
	
  static Token* newToken(int ofKind, JJString tokenImage)
  {
    return new MyToken(ofKind, tokenImage);
  }
	
private:
	int			kind;
	JJString	image;
};
