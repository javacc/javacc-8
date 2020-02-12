/*
 * Boolean.h
 *
 *  Created on: 28 déc. 2016
 *      Author: FrancisANDRE
 */

#pragma once

#include "Node.h"

class Boolean : public Node {
public:
	Boolean(bool value = false);
	virtual ~Boolean();

	Boolean* operator||(const Boolean& value);
	Boolean* operator&&(const Boolean& value);
	operator bool() const { return boolean; }

private:
	bool boolean = false;
};


