PHONY: clean all

clean:
	ant clean

all:
	ant create_run_jar
	launch4jc.exe .\launch4j_config.xml
