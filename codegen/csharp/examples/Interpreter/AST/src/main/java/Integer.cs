/*
 * Integer.cpp
 *
 *  Created on: 28 d�c. 2016
 *      Author: FrancisANDRE
 */

#include "Integer.h"
#include "SPLParserConstants.h"

Integer::Integer(int value) : Node(INT), integer(value) {
}

Integer::~Integer() {
}

Integer* Integer::operator+(const Integer& value) const {
	return new Integer(integer + value.integer);
}
Integer* Integer::operator-(const Integer& value) const {
	return new Integer(integer - value.integer);
}
Integer* Integer::operator*(const Integer& value) const {
	return new Integer(integer * value.integer);
}
Integer* Integer::operator/(const Integer& value) const {
	return new Integer(integer / value.integer);
}
Integer* Integer::operator%(const Integer& value) const {
	return new Integer(integer % value.integer);
}
bool Integer::operator<(const Integer& value) const {
	return integer < value.integer;
}
bool Integer::operator<=(const Integer& value) const {
	return integer <= value.integer;
}
bool Integer::operator==(const Integer& value) const {
	return integer == value.integer;
}
bool Integer::operator>=(const Integer& value) const {
	return integer >= value.integer;
}
bool Integer::operator>(const Integer& value) const {
	return integer > value.integer;
}
