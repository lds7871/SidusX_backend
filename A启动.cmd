@echo off
title GH
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dfile.encoding=GBK"