#pragma once
class ModifierSet {
public:
	static const int PUBLIC = 0x0001;
	static const int PROTECTED = 0x0002;
	static const int PRIVATE = 0x0004;
	static const int ABSTRACT = 0x0008;
	static const int STATIC = 0x0010;
	static const int FINAL = 0x0020;
	static const int SYNCHRONIZED = 0x0040;
	static const int NATIVE = 0x0080;
	static const int TRANSIENT = 0x0100;
	static const int VOLATILE = 0x0200;
	static const int STRICTFP = 0x1000;
	
     static bool isPublic(int modifiers)
     {
       return (modifiers & PUBLIC) != 0;
     }

     static bool isProtected(int modifiers)
     {
       return (modifiers & PROTECTED) != 0;
     }

     static bool isPrivate(int modifiers)
     {
       return (modifiers & PRIVATE) != 0;
     }

     static bool isStatic(int modifiers)
     {
       return (modifiers & STATIC) != 0;
     }

     static bool isAbstract(int modifiers)
     {
       return (modifiers & ABSTRACT) != 0;
     }

     static bool isFinal(int modifiers)
     {
       return (modifiers & FINAL) != 0;
     }

     static bool isNative(int modifiers)
     {
       return (modifiers & NATIVE) != 0;
     }

     static bool isStrictfp(int modifiers)
     {
       return (modifiers & STRICTFP) != 0;
     }

     static bool isSynchronized(int modifiers)
     {
       return (modifiers & SYNCHRONIZED) != 0;
     }

     static bool isTransient(int modifiers)
      {
       return (modifiers & TRANSIENT) != 0;
     }

     static bool isVolatile(int modifiers)
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
	
};