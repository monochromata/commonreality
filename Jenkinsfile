node {
   def newVersion="3.1."+env.BUILD_NUMBER
   withCredentials([[$class: 'FileBinding', credentialsId: 'settings.xml', variable: 'PATH_TO_SETTINGS_XML']]) {
	   stage 'Checkout'
	   git url: 'https://github.com/monochromata/commonreality.git'
	
	   stage 'Set versions'
       sh '''mvn \
             --errors \
             --settings $PATH_TO_SETTINGS_XML \
             --file parent/pom.xml \
             -DnewVersion='''+newVersion+''' \
             versions:set'''
       
       stage "Clean & verify"
       sh '''mvn \
             --errors \
             --settings $PATH_TO_SETTINGS_XML \
             -DnewVersion='''+newVersion+''' \
             clean verify'''
             
       stage "Tag"
       // TODO: Maybe configure the admin e-mail address as a Jenkins credential
       sh '''git config user.email "sl@monochromata.de" \
  			 && git config user.name "Jenkins" \
  			 && mvn \
	             --errors \
	             --settings $PATH_TO_SETTINGS_XML \
             -DnewVersion='''+newVersion+''' \
	             -DskipTests=true \
				 -DskipITs=true \
	             scm:tag'''

       stage "Deploy & site deploy"
       sh '''mvn \
             --errors \
             --settings $PATH_TO_SETTINGS_XML \
             -DnewVersion='''+newVersion+''' \
             -DskipTests=true \
			 -DskipITs=true \
             deploy site-deploy'''		
   }
   
   // TODO: Publish JUnit test reports ... **/target/surefire-reports/*.xml ?
}