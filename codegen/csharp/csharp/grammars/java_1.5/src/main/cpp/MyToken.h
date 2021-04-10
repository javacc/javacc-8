#ifndef MY_TOKEN
#define MY_TOKEN
#include "JavaCC.h"
#include "Token.h"
#include "JavaParserConstants.h"

namespace Java {
		class MyToken : public Token {
		public:
			MyToken(int kind, JJString image) : Token(kind, image) {
				this->mykind = kind;
				this->myimage = image;
			}
			int realKind = GT;

			static Token* newToken(int ofKind, JJString tokenImage) {
				return new MyToken(ofKind, tokenImage);
			}

		private:
			int			mykind;
			JJString	myimage;
		};
}
#endif
