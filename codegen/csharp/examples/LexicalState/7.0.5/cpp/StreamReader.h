#ifndef STREAM_READER_H_
#define STREAM_READER_H_

#include <iostream>
#include "JavaCC.h"

class StreamReader : public ReaderStream {
public:
	StreamReader(std::istream& is);
	virtual ~StreamReader();

	virtual size_t read(JJChar* buffer, int offset, size_t len);
	virtual bool   endOfInput();

private:
	std::istream&	is;
};

#endif