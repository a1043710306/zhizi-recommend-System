@echo off

set CLASSPATH=.;build;

set LOCALCLASSPATH=%CLASSPATH%
for %%i in ("lib\*.jar") do call ".\lcp.bat" %%i
set CLASSPATH=%LOCALCLASSPATH%

java -cp %CLASSPATH% %*
