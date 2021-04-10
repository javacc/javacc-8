/* SpecialToken.cc */
#include "SpecialToken.h"
#include "TokenMgrError.h"
  unsigned int jj_la1_0[] = {
0x2000,};

  /** Constructor with user supplied TokenManager. */



void SpecialToken::Input() {
    JJEnter<std::function<void()>> jjenter([this]() {trace_call  ("Input"); });
    JJExit <std::function<void()>> jjexit ([this]() {trace_return("Input"); });
    try {

      if (!hasError) {
      while (!hasError) {
        if (!hasError) {
        jj_consume_token(Id);
        }
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case Id:{
          ;
          break;
          }
        default:
          jj_la1[0] = jj_gen;
          goto end_label_1;
        }
      }
      end_label_1: ;
      }
      if (!hasError) {
      jj_consume_token(0);
      }
    } catch(...) { }
}


  SpecialToken::SpecialToken(TokenManager *tokenManager){
    head = nullptr;
    ReInit(tokenManager);
}
SpecialToken::~SpecialToken()
{
  clear();
}

void SpecialToken::ReInit(TokenManager* tokenManager){
    clear();
    errorHandler = new ErrorHandler();
    hasError = false;
    token_source = tokenManager;
    head = token = new Token();
    token->kind = 0;
    token->next = nullptr;
    jj_lookingAhead = false;
    jj_rescan = false;
    jj_done = false;
    jj_scanpos = jj_lastpos = nullptr;
    jj_gc = 0;
    jj_kind = -1;
    indent = 0;
    trace = true;
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 1; i++) jj_la1[i] = -1;
  }


void SpecialToken::clear(){
  //Since token manager was generate from outside,
  //parser should not take care of deleting
  //if (token_source) delete token_source;
  if (head) {
    Token *next, *t = head;
    while (t) {
      next = t->next;
      delete t;
      t = next;
    }
  }
  if (errorHandler) {
    delete errorHandler, errorHandler = nullptr;
  }
}


Token * SpecialToken::jj_consume_token(int kind)  {
    Token *oldToken;
    if ((oldToken = token)->next != nullptr) token = token->next;
    else token = token->next = token_source->getNextToken();
    jj_ntk = -1;
    if (token->kind == kind) {
      jj_gen++;
      trace_token(token, "");
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    JJString image = kind >= 0 ? tokenImage[kind] : tokenImage[0];
    errorHandler->handleUnexpectedToken(kind, image.substr(1, image.size() - 2), getToken(1), this);
    hasError = true;
    return token;
  }


/** Get the next Token. */

Token * SpecialToken::getNextToken(){
    if (token->next != nullptr) token = token->next;
    else token = token->next = token_source->getNextToken();
    jj_ntk = -1;
    jj_gen++;
      trace_token(token, " (in getNextToken)");
    return token;
  }

/** Get the specific Token. */

Token * SpecialToken::getToken(int index){
    Token *t = token;
    for (int i = 0; i < index; i++) {
      if (t->next != nullptr) t = t->next;
      else t = t->next = token_source->getNextToken();
    }
    return t;
  }


int SpecialToken::jj_ntk_f(){
    if ((jj_nt=token->next) == nullptr)
      return (jj_ntk = (token->next=token_source->getNextToken())->kind);
    else
      return (jj_ntk = jj_nt->kind);
  }


 void  SpecialToken::parseError()   {
      fprintf(stderr, "Parse error at: %d:%d, after token: %s encountered: %s\n", token->beginLine, token->beginColumn, addUnicodeEscapes(token->image).c_str(), addUnicodeEscapes(getToken(1)->image).c_str());
   }


  bool SpecialToken::trace_enabled()  {
    return trace;
  }


  void SpecialToken::enable_tracing(){
    trace = true;
}


  void SpecialToken::disable_tracing(){
    trace = false;
}


  void SpecialToken::trace_call(const char *s)  {
    if (trace_enabled()) {
      for (int i = 0; i < indent; i++) { printf(" "); }
      printf("Call:   %s\n", s);
    }
    indent = indent + 2;
  }


  void SpecialToken::trace_return(const char *s)  {
    indent = indent - 2;
    if (trace_enabled()) {
      for (int i = 0; i < indent; i++) { printf(" "); }
      printf("Return: %s\n", s);
    }
  }


  void SpecialToken::trace_token(Token *t, const char *where)  {
    if (trace_enabled()) {
      for (int i = 0; i < indent; i++) { printf(" "); }
      printf("Consumed token: <kind: %d(%s), \"%s\"", t->kind, addUnicodeEscapes(tokenImage[t->kind]).c_str(), addUnicodeEscapes(t->image).c_str());
      printf(" at line %d column %d> %s\n", t->beginLine, t->beginColumn, where);
    }
  }


  void SpecialToken::trace_scan(Token *t1, int t2)  {
    if (trace_enabled()) {
      for (int i = 0; i < indent; i++) { printf(" "); }
      printf("Visited token: <Kind: %d(%s), \"%s\"", t1->kind, addUnicodeEscapes(tokenImage[t1->kind]).c_str(), addUnicodeEscapes(t1->image).c_str());
      printf(" at line %d column %d>; Expected token: %s\n", t1->beginLine, t1->beginColumn, addUnicodeEscapes(tokenImage[t2]).c_str());
    }
  }


