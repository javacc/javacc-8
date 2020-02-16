#if 0
import java.io.*;

/**
 * Grammar to parse Java version 1.5
 * @author Sreenivasa Viswanadha - Simplified and enhanced for 1.5
 */
public class JavaParser
{
   /**
    * Class to hold modifiers.
    */
   static public final class ModifierSet
   {
     /* Definitions of the bits in the modifiers field.  */
     public static final int PUBLIC = 0x0001;
     public static final int PROTECTED = 0x0002;
     public static final int PRIVATE = 0x0004;
     public static final int ABSTRACT = 0x0008;
     public static final int STATIC = 0x0010;
     public static final int FINAL = 0x0020;
     public static final int SYNCHRONIZED = 0x0040;
     public static final int NATIVE = 0x0080;
     public static final int TRANSIENT = 0x0100;
     public static final int VOLATILE = 0x0200;
     public static final int STRICTFP = 0x1000;

     /** A set of accessors that indicate whether the specified modifier
         is in the set. */

     public boolean isPublic(int modifiers)
     {
       return (modifiers & PUBLIC) != 0;
     }

     public boolean isProtected(int modifiers)
     {
       return (modifiers & PROTECTED) != 0;
     }

     public boolean isPrivate(int modifiers)
     {
       return (modifiers & PRIVATE) != 0;
     }

     public boolean isStatic(int modifiers)
     {
       return (modifiers & STATIC) != 0;
     }

     public boolean isAbstract(int modifiers)
     {
       return (modifiers & ABSTRACT) != 0;
     }

     public boolean isFinal(int modifiers)
     {
       return (modifiers & FINAL) != 0;
     }

     public boolean isNative(int modifiers)
     {
       return (modifiers & NATIVE) != 0;
     }

     public boolean isStrictfp(int modifiers)
     {
       return (modifiers & STRICTFP) != 0;
     }

     public boolean isSynchronized(int modifiers)
     {
       return (modifiers & SYNCHRONIZED) != 0;
     }

     public boolean isTransient(int modifiers)
      {
       return (modifiers & TRANSIENT) != 0;
     }

     public boolean isVolatile(int modifiers)
     {
       return (modifiers & VOLATILE) != 0;
     }

     /**
      * Removes the given modifier.
      */
     static int removeModifier(int modifiers, int mod)
     {
        return modifiers & ~mod;
     }
   }

   public JavaParser(String fileName)
   {
      this(System.in);
      try { ReInit(new FileInputStream(new File(fileName))); }
      catch(Exception e) { e.printStackTrace(); }
   }

  public static void main(String args[]) {
    JavaParser parser;
    if (args.length == 0) {
      System.out.println("Java Parser Version 1.1:  Reading from standard input . . .");
      parser = new JavaParser(System.in);
    } else if (args.length == 1) {
      System.out.println("Java Parser Version 1.1:  Reading from file " + args[0] + " . . .");
      try {
        parser = new JavaParser(new java.io.FileInputStream(args[0]));
      } catch (java.io.FileNotFoundException e) {
        System.out.println("Java Parser Version 1.1:  File " + args[0] + " not found.");
        return;
      }
    } else {
      System.out.println("Java Parser Version 1.1:  Usage is one of:");
      System.out.println("         java JavaParser < inputfile");
      System.out.println("OR");
      System.out.println("         java JavaParser inputfile");
      return;
    }
    try {
      parser.CompilationUnit();
      System.out.println("Java Parser Version 1.1:  Java program parsed successfully.");
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      System.out.println("Java Parser Version 1.1:  Encountered errors during parse.");
    }
  }

}
#endif
int main(int argc, const char* argv) {
	return 0;
}