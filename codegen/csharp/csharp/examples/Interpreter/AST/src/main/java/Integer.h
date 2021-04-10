/*
 * Integer.h
 *
 *  Created on: 28 déc. 2016
 *      Author: FrancisANDRE
 */

#pragma once

#include "Node.h"

class Integer : public Node {
public:
	Integer(int value = 0);
	virtual ~Integer();

	Integer* operator+(const Integer& value) const;
	Integer* operator-(const Integer& value) const;
	Integer* operator*(const Integer& value) const;
	Integer* operator/(const Integer& value) const;
	Integer* operator%(const Integer& value) const;

	bool operator<  (const Integer& value) const;
	bool operator<= (const Integer& value) const;
	bool operator== (const Integer& value) const;
	bool operator>= (const Integer& value) const;
	bool operator>  (const Integer& value) const;

	operator int() const { return integer; }

private:
	int integer;
};


