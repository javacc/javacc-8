#ifndef JAVACC_MYTOKENCONSTANTS_H
#define JAVACC_MYTOKENCONSTANTS_H

#include "JavaCC.h"

namespace BAR {
	namespace BAZ {
		/** End of File. */
		const  int _EOF = 0;
		/** RegularExpression Id. */
		const  int NL = 1;
		/** RegularExpression Id. */
		const  int LF = 2;
		/** RegularExpression Id. */
		const  int LBRACE = 3;
		/** RegularExpression Id. */
		const  int RBRACE = 4;

		/** Literal token image. */
		static const JJChar tokenImage_0[] =
		{ 0x3c, 0x45, 0x4f, 0x46, 0x3e, 0 };
		static const JJChar tokenImage_1[] =
		{ 0x3c, 0x4e, 0x4c, 0x3e, 0 };
		static const JJChar tokenImage_2[] =
		{ 0x3c, 0x4c, 0x46, 0x3e, 0 };
		static const JJChar tokenImage_3[] =
		{ 0x3c, 0x4c, 0x42, 0x52, 0x41, 0x43, 0x45, 0x3e, 0 };
		static const JJChar tokenImage_4[] =
		{ 0x3c, 0x52, 0x42, 0x52, 0x41, 0x43, 0x45, 0x3e, 0 };
		static const JJChar* const tokenImages[] = {
	  tokenImage_0,
	  tokenImage_1,
	  tokenImage_2,
	  tokenImage_3,
	  tokenImage_4,
		};

		/** Literal token label. */
		static const JJChar tokenLabel_0[] =
		{ 0x3c, 0x45, 0x4f, 0x46, 0x3e, 0 };
		static const JJChar tokenLabel_1[] =
		{ 0x3c, 0x4e, 0x4c, 0x3e, 0 };
		static const JJChar tokenLabel_2[] =
		{ 0x3c, 0x4c, 0x46, 0x3e, 0 };
		static const JJChar tokenLabel_3[] =
		{ 0x3c, 0x4c, 0x42, 0x52, 0x41, 0x43, 0x45, 0x3e, 0 };
		static const JJChar tokenLabel_4[] =
		{ 0x3c, 0x52, 0x42, 0x52, 0x41, 0x43, 0x45, 0x3e, 0 };
		static const JJChar* const tokenLabels[] = {
	  tokenLabel_0,
	  tokenLabel_1,
	  tokenLabel_2,
	  tokenLabel_3,
	  tokenLabel_4,
		};
	}
}
#endif
