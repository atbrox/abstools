# Targets to compile and test the Antlr-based ABS parser.
# GNU make required for (future versions of) target `tests'


SOURCE   = src/
LIB	 = lib/antlr-3.2.jar:lib/jastadd2.jar
COMPILED = bin/
GENERATED_SRC = gen/
JAVAC	 = javac -sourcepath $(SOURCE):$(GENERATED_SRC) -cp $(LIB):$(COMPILED) -d $(COMPILED)
ANTLR	 = java -cp $(LIB) org.antlr.Tool
JASTADD	 = java -jar lib/jastadd2.jar --o=$(GENERATED_SRC)abs/frontend/ast
#ANTLROPTS =-dfa -nfa
#PARSEROPTS = -tree 
PARSER	 = java -cp $(LIB):$(COMPILED) abs.frontend.parser.ABSParser 
DOTS 	 = `ls gen/abs/frontend/parser/*.dot` 	
TESTS 	 =  statements.abs  trivial.abs boundedbuffer.abs pingpong.abs

# TESTS	= $(wildcard tests/*.abs)

.PHONY : clean clean_java clean_antlr sense tests

# Fix this target with to use variable $(TESTS) and change parser
# to run on all input files, not just one 
# Doing it with a for loop for now. 

beavertests: absparser
	@for file in $(TESTS); do \
	echo $(PARSER) "tests/$${file}" $(PARSEROPTS) ; $(PARSER) "tests/$${file}" $(PARSEROPTS) ;  \
	done ;


tests: absparser
	@for file in $(TESTS); do \
	echo $(PARSER) "tests/$${file}" $(PARSEROPTS) ; $(PARSER) "tests/$${file}" $(PARSEROPTS) ;  \
	done ;


compileast: genastsrc $(COMPILED)abs/frontend/ast/%.class 

$(COMPILED)abs/frontend/ast/%.class : $(wildcard $(GENERATED_SRC)abs/frontend/ast/*.java)
	mkdir -p $(COMPILED)
	$(JAVAC) $^

genastsrc: $(GENERATED_SRC)abs/frontend/ast/%.java

absparser: $(COMPILED)abs/frontend/parser/ABSLexer.class $(COMPILED)abs/frontend/parser/ABSParser.class

$(COMPILED)abs/frontend/parser/%.class : $(GENERATED_SRC)abs/frontend/parser/%.java
	mkdir -p $(COMPILED)
	$(JAVAC) $^

$(GENERATED_SRC)abs/frontend/parser/ABSLexer.java \
$(GENERATED_SRC)abs/frontend/parser/ABSParser.java: $(SOURCE)abs/frontend/parser/ABS.g
	$(ANTLR) $(ANTLROPTS) -fo $(GENERATED_SRC)abs/frontend/parser  $<

$(GENERATED_SRC)abs/frontend/ast/%.java: $(SOURCE)abs/frontend/ast/ABS.ast
	@mkdir -p $(GENERATED_SRC)abs/frontend/ast
	$(JASTADD) $^

clean: clean_java clean_antlr 

clean_java:
	@echo "Removing class files"
	@rm -rf $(GENERATED_SRC)
	@rm -rf $(COMPILED)

clean_antlr:
	@echo "Removing automatically generated Antlr files"
	@rm -rf $(GENERATED_SRC)
	@rm -rf $(COMPILED)

sense:
	@fortune


#generate jpg files from dot files for nfas/dfas 
automata: absparser
	@echo ${DOTS} ; 
	@for file in $(DOTS); do \
	dot -Tjpg  "$${file}" -O ;  \
	done ;




#	@for f in {gen/abs/frontend/parser/*.dot} ; do echo $f ; done
#	dot -Tjpg -o $out creolc.out 	@for f in . ; do echo $f ; done ; 
#	dot -Tjpg -o $out creolc.out 
