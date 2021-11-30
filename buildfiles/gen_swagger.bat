REM *********************************************************
REM * Copyright Regione Piemonte - 2021
REM * SPDX-License-Identifier: EUPL-1.2-or-later
REM *********************************************************

set CSI_SWAGGER_CODEGEN_HOME=..\..\swagger-codegen
set CLI_JAR_PATH=%CSI_SWAGGER_CODEGEN_HOME%\swagger-codegen-cli.jar;%CSI_SWAGGER_CODEGEN_HOME%\csi-java-swagger-codegen-1.0.0.jar

set DEBUG_OPTS=
rem set DEBUG_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,address=9797,server=y,suspend=y

rem pause "#### generazione skeleton jaxrs ####"

#java -cp %CLI_JAR_PATH% %DEBUG_OPTS% io.swagger.codegen.SwaggerCodegen generate -i ..\src\yaml\ministero.yaml -l jaxrs-resteasy-eap -o ..\tempgen --config swagger_config_java.json


rem pause "#### generazione stub jaxrs ####"
#java -cp %CLI_JAR_PATH% %DEBUG_OPTS% io.swagger.codegen.SwaggerCodegen generate -i ..\src\yaml\ministero.yaml -l java -o ..\tempgenclient --config swagger_config_javaclient.json

rem pause "#### generazione stub jaxrs (external ministero)####"
#java -cp %CLI_JAR_PATH% %DEBUG_OPTS% io.swagger.codegen.SwaggerCodegen generate -i ..\src\yaml\original_ministero.yaml -l java -o ..\tempgenclientmin --config swagger_config_javaclient_ext.json


rem echo
rem pause "#### generazione stub angular2 ####"
rem java -jar %CLI_JAR_PATH% generate -i ..\src\yaml\anagrafica.yaml -l typescript-angular -o ..\tempgenang2 --config swagger_config_angular.json 

rem echo
rem pause "#### generazione documentazione html ####"
java -cp %CLI_JAR_PATH% %DEBUG_OPTS% io.swagger.codegen.SwaggerCodegen generate -l html2 -i ..\src\yaml\ministero.yaml -o ..\temphelp
