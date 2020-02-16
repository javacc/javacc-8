#pragma once
#include "JavaCC.h"

class MyToken : public Token {
public:
	MyToken(int kind, JJString image) {
		this->kind = kind;
		this->image = image;
	}
	static int realKind;
	
	static Token* newToken(int ofKind, JJString tokenImage) {
		return new MyToken(ofKind, tokenImage);
	}
	
private:
	int			kind;
	JJString	image;
};
