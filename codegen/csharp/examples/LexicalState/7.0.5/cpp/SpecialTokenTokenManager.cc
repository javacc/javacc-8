/* SpecialTokenTokenManager.cc */
#include "SpecialTokenTokenManager.h"
#include "TokenMgrError.h"
static const unsigned long long jjbitVec0[] = {
   0x0ULL, 0x0ULL, 0xffffffffffffffffULL, 0xffffffffffffffffULL
};
static const int jjnextStates[] = {
   0, 2, 3, 4, 0, 3, 5, 6, 
};
static JJChar jjstrLiteralChars_0[] = {0};
static JJChar jjstrLiteralChars_1[] = {0};
static JJChar jjstrLiteralChars_2[] = {0};
static JJChar jjstrLiteralChars_3[] = {0};
static JJChar jjstrLiteralChars_4[] = {0};
static JJChar jjstrLiteralChars_5[] = {0};
static JJChar jjstrLiteralChars_6[] = {0};

static JJChar jjstrLiteralChars_7[] = {0};
static JJChar jjstrLiteralChars_8[] = {0};
static JJChar jjstrLiteralChars_9[] = {0};
static JJChar jjstrLiteralChars_10[] = {0};
static JJChar jjstrLiteralChars_11[] = {0};
static JJChar jjstrLiteralChars_12[] = {0};
static JJChar jjstrLiteralChars_13[] = {0};
static const JJString jjstrLiteralImages[] = {
jjstrLiteralChars_0, 
jjstrLiteralChars_1, 
jjstrLiteralChars_2, 
jjstrLiteralChars_3, 
jjstrLiteralChars_4, 
jjstrLiteralChars_5, 
jjstrLiteralChars_6, 
jjstrLiteralChars_7, 
jjstrLiteralChars_8, 
jjstrLiteralChars_9, 
jjstrLiteralChars_10, 
jjstrLiteralChars_11, 
jjstrLiteralChars_12, 
jjstrLiteralChars_13, 
};

/** Lexer state names. */
static const JJChar lexStateNames_arr_0[] = 
{0x44, 0x45, 0x46, 0x41, 0x55, 0x4c, 0x54, 0};
static const JJChar lexStateNames_arr_1[] = 
{0x4c, 0x69, 0x6e, 0x65, 0x73, 0x43, 0x6f, 0x6d, 0x6d, 0x65, 0x6e, 0x74, 0};
static const JJChar lexStateNames_arr_2[] = 
{0x4c, 0x69, 0x6e, 0x65, 0x43, 0x6f, 0x6d, 0x6d, 0x65, 0x6e, 0x74, 0};
static const JJString lexStateNames[] = {
lexStateNames_arr_0, 
lexStateNames_arr_1, 
lexStateNames_arr_2, 
};

/** Lex State array. */
static const int jjnewLexState[] = {
   -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, 2, 0, -1, 
};
static const unsigned long long jjtoToken[] = {
   0x2001ULL, 
};
static const unsigned long long jjtoSkip[] = {
   0x19feULL, 
};
static const unsigned long long jjtoSpecial[] = {
   0x1800ULL, 
};
  int linesCommentCount = 0;
  void  SpecialTokenTokenManager::setDebugStream(FILE *ds){ debugStream = ds; }

 int SpecialTokenTokenManager::jjStopStringLiteralDfa_0(int pos, unsigned long long active0){
   switch (pos)
   {
      default :
         return -1;
   }
}

int  SpecialTokenTokenManager::jjStartNfa_0(int pos, unsigned long long active0){
   return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
}

 int  SpecialTokenTokenManager::jjStopAtPos(int pos, int kind){
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}

 int  SpecialTokenTokenManager::jjMoveStringLiteralDfa0_0(){
   switch(curChar)
   {
      case 45:
         return jjMoveStringLiteralDfa1_0(0x800ULL);
      case 47:
         return jjMoveStringLiteralDfa1_0(0x20ULL);
      default :
         return jjMoveNfa_0(0, 0);
   }
}

 int  SpecialTokenTokenManager::jjMoveStringLiteralDfa1_0(unsigned long long active0){
   if (input_stream->endOfInput()) {
      jjStopStringLiteralDfa_0(0, active0);
      return 1;
   }
   curChar = input_stream->readChar();
   switch(curChar)
   {
      case 42:
         if ((active0 & 0x20ULL) != 0L)
            return jjStopAtPos(1, 5);
         break;
      case 45:
         if ((active0 & 0x800ULL) != 0L)
            return jjStopAtPos(1, 11);
         break;
      default :
         break;
   }
   return jjStartNfa_0(0, active0);
}

int SpecialTokenTokenManager::jjMoveNfa_0(int startState, int curPos){
   int startsAt = 0;
   jjnewStateCnt = 2;
   int i = 1;
   jjstateSet[0] = startState;
   int kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         unsigned long long l = 1ULL << curChar;
         (void)l;
         do
         {
            switch(jjstateSet[--i])
            {
               case 1:
                  if ((0x3ff000000000000ULL & l) == 0L)
                     break;
                  kind = 13;
                  jjstateSet[jjnewStateCnt++] = 1;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         unsigned long long l = 1ULL << (curChar & 077);
         (void)l;
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
               case 1:
                  if ((0x7fffffe07fffffeULL & l) == 0L)
                     break;
                  if (kind > 13)
                     kind = 13;
                  { jjCheckNAdd(1); }
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int i2 = (curChar & 0xff) >> 6;
         unsigned long long l2 = 1ULL << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt), (jjnewStateCnt = startsAt), (i == (startsAt = 2 - startsAt)))
         return curPos;
      if (input_stream->endOfInput()) { return curPos; }
      curChar = input_stream->readChar();
   }
}

 int  SpecialTokenTokenManager::jjMoveStringLiteralDfa0_2(){
   return jjMoveNfa_2(7, 0);
}

int SpecialTokenTokenManager::jjMoveNfa_2(int startState, int curPos){
   int startsAt = 0;
   jjnewStateCnt = 7;
   int i = 1;
   jjstateSet[0] = startState;
   int kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         unsigned long long l = 1ULL << curChar;
         (void)l;
         do
         {
            switch(jjstateSet[--i])
            {
               case 7:
                  if ((0xffffdfffffffdbffULL & l) != 0L)
                  {
                     if (kind > 12)
                        kind = 12;
                     { jjCheckNAddStates(0, 3); }
                  }
                  else if ((0x2400ULL & l) != 0L)
                  {
                     if (kind > 12)
                        kind = 12;
                  }
                  else if (curChar == 45)
                     { jjCheckNAddStates(4, 7); }
                  if (curChar == 13)
                     { jjCheckNAdd(1); }
                  break;
               case 0:
                  if ((0xffffdfffffffdbffULL & l) == 0L)
                     break;
                  kind = 12;
                  { jjCheckNAddStates(0, 3); }
                  break;
               case 1:
                  if (curChar == 10 && kind > 12)
                     kind = 12;
                  break;
               case 2:
               case 5:
                  if (curChar == 13)
                     { jjCheckNAdd(1); }
                  break;
               case 3:
                  if ((0x2400ULL & l) != 0L && kind > 12)
                     kind = 12;
                  break;
               case 4:
                  if (curChar == 45)
                     { jjCheckNAddStates(4, 7); }
                  break;
               case 6:
                  if (curChar == 45 && kind > 12)
                     kind = 12;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         unsigned long long l = 1ULL << (curChar & 077);
         (void)l;
         do
         {
            switch(jjstateSet[--i])
            {
               case 7:
               case 0:
                  kind = 12;
                  { jjCheckNAddStates(0, 3); }
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int i2 = (curChar & 0xff) >> 6;
         unsigned long long l2 = 1ULL << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 7:
               case 0:
                  if ((jjbitVec0[i2] & l2) == 0L)
                     break;
                  if (kind > 12)
                     kind = 12;
                  { jjCheckNAddStates(0, 3); }
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt), (jjnewStateCnt = startsAt), (i == (startsAt = 7 - startsAt)))
         return curPos;
      if (input_stream->endOfInput()) { return curPos; }
      curChar = input_stream->readChar();
   }
}

 int  SpecialTokenTokenManager::jjMoveStringLiteralDfa0_1(){
   switch(curChar)
   {
      case 42:
         return jjMoveStringLiteralDfa1_1(0x80ULL);
      case 47:
         return jjMoveStringLiteralDfa1_1(0x40ULL);
      default :
         return 1;
   }
}

 int  SpecialTokenTokenManager::jjMoveStringLiteralDfa1_1(unsigned long long active0){
   if (input_stream->endOfInput()) {
      return 1;
   }
   curChar = input_stream->readChar();
   switch(curChar)
   {
      case 42:
         if ((active0 & 0x40ULL) != 0L)
            return jjStopAtPos(1, 6);
         break;
      case 47:
         if ((active0 & 0x80ULL) != 0L)
            return jjStopAtPos(1, 7);
         break;
      default :
         return 2;
   }
   return 2;
}

/** Token literal values. */

Token * SpecialTokenTokenManager::jjFillToken(){
   Token *t;
   JJString curTokenImage;
   int beginLine   = -1;
   int endLine     = -1;
   int beginColumn = -1;
   int endColumn   = -1;
   if (jjmatchedPos < 0)
   {
       curTokenImage = image.c_str();
   if (input_stream->getTrackLineColumn()) {
      beginLine = endLine = input_stream->getEndLine();
      beginColumn = endColumn = input_stream->getEndColumn();
   }
   }
   else
   {
      JJString im = jjstrLiteralImages[jjmatchedKind];
      curTokenImage = (im.length() == 0) ? input_stream->GetImage() : im;
   if (input_stream->getTrackLineColumn()) {
      beginLine = input_stream->getBeginLine();
      beginColumn = input_stream->getBeginColumn();
      endLine = input_stream->getEndLine();
      endColumn = input_stream->getEndColumn();
   }
   }
   t = Token::newToken(jjmatchedKind);
   t->kind = jjmatchedKind;
   t->image = curTokenImage;
   t->specialToken = nullptr;
   t->next = nullptr;

   if (input_stream->getTrackLineColumn()) {
   t->beginLine = beginLine;
   t->endLine = endLine;
   t->beginColumn = beginColumn;
   t->endColumn = endColumn;
   }

   return t;
}
const int defaultLexState = 0;
/** Get the next Token. */

Token * SpecialTokenTokenManager::getNextToken(){
  Token *specialToken = nullptr;
  Token *matchedToken = nullptr;
  int curPos = 0;

  for (;;)
  {
   EOFLoop: 
   if (input_stream->endOfInput())
   {
      jjmatchedKind = 0;
      jjmatchedPos = -1;
      matchedToken = jjFillToken();
      matchedToken->specialToken = specialToken;
      return matchedToken;
   }
   curChar = input_stream->BeginToken();
   image = jjimage;
   image.clear();
   jjimageLen = 0;

   switch(curLexState)
   {
     case 0:
       { input_stream->backup(0);
          while (curChar <= 32 && (0x100002600ULL & (1ULL << curChar)) != 0L)
       {
       if (input_stream->endOfInput()) { goto EOFLoop; }
       curChar = input_stream->BeginToken();
       }
       }
       jjmatchedKind = 0x7fffffff;
       jjmatchedPos = 0;
       curPos = jjMoveStringLiteralDfa0_0();
       break;
     case 1:
       jjmatchedKind = 0x7fffffff;
       jjmatchedPos = 0;
       curPos = jjMoveStringLiteralDfa0_1();
       if (jjmatchedPos == 0 && jjmatchedKind > 8)
       {
          jjmatchedKind = 8;
       }
       break;
     case 2:
       jjmatchedKind = 12;
       jjmatchedPos = -1;
       curPos = 0;
       curPos = jjMoveStringLiteralDfa0_2();
       break;
   }
     if (jjmatchedKind != 0x7fffffff)
     {
        if (jjmatchedPos + 1 < curPos)
           input_stream->backup(curPos - jjmatchedPos - 1);
        if ((jjtoToken[jjmatchedKind >> 6] & (1ULL << (jjmatchedKind & 077))) != 0L)
        {
           matchedToken = jjFillToken();
           matchedToken->specialToken = specialToken;
       if (jjnewLexState[jjmatchedKind] != -1)
         curLexState = jjnewLexState[jjmatchedKind];
           return matchedToken;
        }
        else
        {
           if ((jjtoSpecial[jjmatchedKind >> 6] & (1ULL << (jjmatchedKind & 077))) != 0L)
           {
              matchedToken = jjFillToken();
              if (specialToken == nullptr)
                 specialToken = matchedToken;
              else
              {
                 matchedToken->specialToken = specialToken;
                 specialToken = (specialToken->next = matchedToken);
              }
              SkipLexicalActions(matchedToken);
           }
           else
              SkipLexicalActions(nullptr);
         if (jjnewLexState[jjmatchedKind] != -1)
           curLexState = jjnewLexState[jjmatchedKind];
           goto EOFLoop;
        }
     }
     int error_line = input_stream->getEndLine();
     int error_column = input_stream->getEndColumn();
     JJString error_after;
     bool EOFSeen = false;
     if (input_stream->endOfInput()) {
        EOFSeen = true;
        error_after = curPos <= 1 ? EMPTY : input_stream->GetImage();
        if (curChar == '\n' || curChar == '\r') {
           error_line++;
           error_column = 0;
        }
        else
           error_column++;
     }
     if (!EOFSeen) {
        error_after = curPos <= 1 ? EMPTY : input_stream->GetImage();
     }
     errorHandler->lexicalError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, this);
  }
}


void  SpecialTokenTokenManager::SkipLexicalActions(Token *matchedToken){
   switch(jjmatchedKind)
   {
      case 5 : {
         image.append(input_stream->GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1)));
                                                            linesCommentCount = 1 ;
         break;
       }
      case 6 : {
         image.append(input_stream->GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1)));
                                            linesCommentCount += 1 ;
         break;
       }
      case 7 : {
         image.append(input_stream->GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1)));
                                            linesCommentCount -= 1; SwitchTo( linesCommentCount==0 ? DEFAULT : LinesComment ) ;
         break;
       }
      default :
         break;
   }
}
  /** Reinitialise parser. */
  void SpecialTokenTokenManager::ReInit(JAVACC_CHARSTREAM *stream, int lexState) {
    clear();
    jjmatchedPos = jjnewStateCnt = 0;
    curLexState = lexState;
    input_stream = stream;
    ReInitRounds();
    debugStream = stdout; // init
    SwitchTo(lexState);
    errorHandler = new TokenManagerErrorHandler();
  }

  void SpecialTokenTokenManager::ReInitRounds() {
    int i;
    jjround = 0x80000001;
    for (i = 7; i-- > 0;)
      jjrounds[i] = 0x80000000;
  }

  /** Switch to specified lex state. */
  void SpecialTokenTokenManager::SwitchTo(int lexState) {
    if (lexState >= 3 || lexState < 0) {
      JJString message;
#ifdef WIDE_CHAR
      message += L"Error: Ignoring invalid lexical state : ";
      message += lexState; message += L". State unchanged.";
#else
      message += "Error: Ignoring invalid lexical state : ";
      message += lexState; message += ". State unchanged.";
#endif
      throw new TokenMgrError(message, INVALID_LEXICAL_STATE);
    } else
      curLexState = lexState;
  }

  /** Constructor. */
  SpecialTokenTokenManager::SpecialTokenTokenManager (JAVACC_CHARSTREAM *stream, int lexState)
  {
    input_stream = nullptr;
    ReInit(stream, lexState);
  }

  // Destructor
  SpecialTokenTokenManager::~SpecialTokenTokenManager () {
    clear();
  }

  // clear
  void SpecialTokenTokenManager::clear() {
    //Since input_stream was generated outside of TokenManager
    //TokenManager should not take care of deleting it
    //if (input_stream) delete input_stream;
    if (errorHandler) delete errorHandler, errorHandler = nullptr;    
  }


