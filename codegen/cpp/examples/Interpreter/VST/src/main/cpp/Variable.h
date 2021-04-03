/*
 * Variable.h
 *
 *  Created on: 28 d�c. 2016
 *      Author: FrancisANDRE
 */

#pragma once

#include <string>
using std::string;

#include "Node.h"


class Variable : public Node {
public:
	Variable(int id);
	virtual ~Variable();


private:
	int type;
	string name;
};


