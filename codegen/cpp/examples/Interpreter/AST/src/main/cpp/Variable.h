/*
 * Variable.h
 *
 *  Created on: 28 déc. 2016
 *      Author: FrancisANDRE
 */

#pragma once

#include <string>

#include "Node.h"
using namespace std;

class Variable : public Node {
public:
	Variable(int id);
	virtual ~Variable();

private:
	string name;
};


