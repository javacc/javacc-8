int main(int argc, char** argv) {
	return 0;
}
#if 0
package org.dojo.jsl.parser;

import java.io.*;
import java.util.*;

public class EcmaScript {

	public static void main(String args[]){
		EcmaScript parser;
		if(args.length == 0){
			System.out.println("EcmaScript Parser:  Reading from standard input . . .");
			parser = new EcmaScript(System.in);
		} else if(args.length == 1){
			System.out.println("EcmaScript Parser:  Reading from file " + args[0] + " . . .");
			try {
				parser = new EcmaScript(new FileInputStream(args[0]), "UTF-8");
			} catch(java.io.FileNotFoundException e){
				System.out.println("EcmaScript Parser:  File " + args[0] + " not found.");
				return;
			}
		} else {
			System.out.println("EcmaScript Parser:  Usage is one of:");
			System.out.println("         EcmaScript < inputfile");
			System.out.println("OR");
			System.out.println("         EcmaScript inputfile");
			return;
		}
		try {
			SimpleNode n = parser.Program();
			System.out.println("EcmaScript parser:  EcmaScript program parsed successfully.");
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			System.out.println("EcmaScript parser:  Encountered errors during parse.");
		}
	}

	void jjtreeOpenNodeScope(Node n){
		Token t = getToken(1);
		if(t != null){
			((SimpleNode) n).setBeginToken(t);
		}
	}

	void jjtreeCloseNodeScope(Node n){
		Token t = getToken(0);
		if(t != null){
			((SimpleNode) n).setEndToken(t);
		}
	}
}

#endif