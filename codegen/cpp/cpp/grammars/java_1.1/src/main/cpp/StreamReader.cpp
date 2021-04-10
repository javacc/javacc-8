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

using namespace std;
StreamReader::StreamReader(std::istream& is) : is(is) {

}
StreamReader::~StreamReader() {

}
size_t StreamReader::read(JJChar * buffer, int offset, size_t len) {
	is.read(buffer + offset, len);
	return is.gcount();
}
bool StreamReader::endOfInput() {
	return is.eof();
}