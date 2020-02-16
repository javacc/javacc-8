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
};