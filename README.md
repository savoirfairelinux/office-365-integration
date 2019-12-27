# Office 365 integration

# requirement

To be able to authenticate with office 365, the plugin needs to be configured with an apiKey and an api secret that can be retrieved in the Microsoft azure panel by registering a new application. A redirect URL will also need to be provided. This URL needs to be your portal URL followed by /o/o365/login. Note that except for localhost tests, the URL is required to be HTTPS.

More info on the application registration is available on Microsoft documentation: https://docs.microsoft.com/fr-fr/graph/auth-register-app-v2

When targeting users in an organizational directory, the organization needs to approve the application for its users before they can be authorized to use it.

# Dependencies

The following bundles are required for this module to work:
    * scribejava-apis-6.5.1.jar http://central.maven.org/maven2/com/github/scribejava/scribejava-apis/6.5.1/scribejava-apis-6.5.1.jar
    * scribejava-core-6.5.1.jar http://central.maven.org/maven2/com/github/scribejava/scribejava-core/6.5.1/scribejava-core-6.5.1.jar
 
    * guava-20.0.jar http://central.maven.org/maven2/com/google/guava/guava/20.0/guava-20.0.jar
    * jsr311-api-1.1.1.jar http://central.maven.org/maven2/javax/ws/rs/jsr311-api/1.1.1/jsr311-api-1.1.1.jar
    * gson-2.8.5.jar http://central.maven.org/maven2/com/google/code/gson/gson/2.8.5/gson-2.8.5.jar
    
An easy way of downloading all of them if to build the o365-package module, they are all included in the zip.

* json-20180813.jar http://central.maven.org/maven2/org/json/json/20180813/json-20180813.jar
* jersey-server-1.19.4.jar http://central.maven.org/maven2/com/sun/jersey/jersey-server/1.19.4/jersey-server-1.19.4.jar
* jersey-core-1.19.4.jar http://central.maven.org/maven2/com/sun/jersey/jersey-core/1.19.4/jersey-core-1.19.4.jar

# Authentication

The authentication work with a filter hook that listens to the /o/o365/login URL of the portal. 

1. Whenever a user hit that URL he will be redirected to the office 365 login page
2. The user login on the Microsoft site and accept the authorization requested
3. The user is sent back to the portal /o/o365/login URL with an authentication code provided
4. The filter hook validates that code and get an access token from office 365
5. The filter hook save the access token to use it subsequent request

More info is available to understand the office 365 authentication process: https://docs.microsoft.com/fr-fr/azure/active-directory/develop/v1-protocols-openid-connect-code

# Configuration
Your application must be registered in Azure AD before being able to connect to it (https://docs.microsoft.com/en-us/azure/active-directory/develop/v1-protocols-openid-connect-code#register-your-application-with-your-ad-tenant)
You will need to retrieve the application ID and secret of your application to configure it in liferay system settings.

# How to build

To build the whole project, the liferay version must be provided to maven to account for different configuration required.
Liferay 7.0:
    mvn -Dliferay=70 clean package

Liferay 7.1:
    mvn -Dliferay=71 clean package

Liferay 7.2:
    mvn -Dliferay=72 clean package

The required artifacts can be found in `modules/o365-package/target/o365-package-*.zip/`
    