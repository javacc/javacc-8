int main(int argc, char** argv) {
	return 0;
}
#if 0
import java.util.*;

  public class CParser{

    // Hastable for storing typedef types
    private static Set types = new HashSet();

    // Stack for determining when the parser
    // is parsing a typdef definition.
    private static Stack typedefParsingStack = new Stack();

    // Returns true if the given string is
    // a typedef type.
    private static boolean isType(String type){
   	  return types.contains(type);
    }

    // Add a typedef type to those already defined
    private static void addType(String type){
   	  types.add(type);
    }

    // Prints out all the types used in parsing the c source
    private static void printTypes(){
      for (Iterator i = types.iterator(); i.hasNext();) {
        System.out.println(i.next());
      }
    }

    // Run the parser
    public static void main ( String args [ ] ) {
      CParser parser ;

  	  // Hack to include type "special types"
	    types.add("__signed__");
	    types.add("__const");
	    types.add("__inline__");
	    types.add("__signed");

      if(args.length == 0){
        System.out.println("C Parser Version 0.1Alpha:  Reading from standard input . . .");
        parser = new CParser(System.in);
      }
      else if(args.length == 1){
        System.out.println("C Parser Version 0.1Alpha:  Reading from file " + args[0] + " . . ." );
      try {
        parser = new CParser(new java.io.FileInputStream(args[0]));
      }
      catch(java.io.FileNotFoundException e){
        System.out.println("C Parser Version 0.1:  File " + args[0] + " not found.");
        return ;
        }
      }
      else {
        System.out.println("C Parser Version 0.1Alpha:  Usage is one of:");
        System.out.println("         java CParser < inputfile");
        System.out.println("OR");
        System.out.println("         java CParser inputfile");
        return ;
      }
      try {
        parser.TranslationUnit();
        System.out.println("C Parser Version 0.1Alpha:  Java program parsed successfully.");
      }
      catch(ParseException e){
        System.out.println("C Parser Version 0.1Alpha:  Encountered errors during parse.");
        e.printStackTrace();
      }
    }
  }
#endif