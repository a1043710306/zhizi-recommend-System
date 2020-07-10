@echo off

set CLASSPATH=.;build;

set LOCALCLASSPATH=%CLASSPATH%
for %%i in ("lib\*.jar") do call ".\lcp.bat" %%i
set CLASSPATH=%LOCALCLASSPATH%

java -Xmx2G -cp %LOCALCLASSPATH% -Dfile.encoding=UTF-8 -Djava.util.logging.config.file=log4j.properties bsh.Interpreter %*
