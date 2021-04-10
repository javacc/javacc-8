#ifndef PARSING_STACK_H
#define PARSING_STACK_H

#include <iostream>
#include <stack>
#include <set>
#include <string>

static std::stack<bool> typedefParsingStack;
static std::set<std::string> types;

// Returns true if the given string is
// a typedef type.
static bool isType(const std::string& name) {
	 auto search = types.find(name);
	 return search != types.end();
}
// Add a typedef type to those already defined
static void addType(const std::string& type){
	types.insert(type);
}

// Prints out all the types used in parsing the c source
static void printTypes(){
//  for (Iterator i = types.iterator(); i.hasNext();) {
//    std::cout << i.next()) << std::endl;
//  }
}


#endif