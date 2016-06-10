class Config {
	public static String newVersion
	public static String releaseMetaDataURL = 'http://monochromata.de/maven/releases/org.commonreality/org/commonreality/core/maven-metadata.xml'
	public static String gitRepo = 'https://github.com/monochromata/commonreality.git'
}

// TODO: Even this might be moved into workflowLibs, passing just a Config instance
node("1gb") {
   installToolsIfNecessary()
   withCredentials([[$class: 'FileBinding', credentialsId: 'settings.xml', variable: 'PATH_TO_SETTINGS_XML'],
   					[$class: 'FileBinding', credentialsId: 'jarsigner.keystore', variable: 'PATH_TO_JARSIGNER_KEYSTORE'],
   					[$class: 'FileBinding', credentialsId: 'pubring.gpg', variable: 'PATH_TO_GPG_PUBLIC_KEYRING'],
   					[$class: 'FileBinding', credentialsId: 'secring.gpg', variable: 'PATH_TO_GPG_SECRET_KEYRING'],
   					[$class: 'FileBinding', credentialsId: 'upload.server.ssh.signature.file', variable: 'PATH_TO_UPLOAD_SERVER_SSH_FINGERPRINT_FILE'],
   					[$class: 'StringBinding', credentialsId: 'upload.server.name', variable: 'UPLOAD_SERVER_NAME'],]) {
   					
		withEnv(["PATH+MAVEN=${tool 'mvn'}/bin", 
				 "PATH+JAVA=${tool 'jdk8'}/bin"]) {
		   stage 'Checkout'
		   git url: Config.gitRepo
		   
		   stage name: 'Set versions', concurrency: 1
		   Config.newVersion = getNextVersion()
		   maven('''--file parent/pom.xml \
		   			-Dcommonreality.eclipse.version='''+newVersion.replaceAll('-', '.')+''' \	       				 
				    versions:set''')
	       
	       stage name: "Clean & verify", concurrency: 1
	       maven('''-Dcommonreality.eclipse.version='''+newVersion.replaceAll('-', '.')+''' \
	       		  clean verify''')
	
	       stage name:"Deploy", concurrency: 1
	       // TODO: Deploy to Maven Central will require the maven central ssh fingerprint
	       sh '''touch ~/.ssh/known_hosts \
	       		 && ssh-keygen -f ~/.ssh/known_hosts -R $UPLOAD_SERVER_NAME \
	       		 && cat $PATH_TO_UPLOAD_SERVER_SSH_FINGERPRINT_FILE >> ~/.ssh/known_hosts'''
	       // Retry is necessary because upload is unreliable
	       retry(3) {
	       		maven('''-Dcommonreality.eclipse.version='''+newVersion.replaceAll('-', '.')+''' \
	       				 -DskipTests=true \
	       				 -DskipITs=true \
	       				 deploy''')
	       }
	             
	       stage name:"Site deploy", concurrency: 1
	       // Retry is necessary because upload is unreliable
	       retry(3) {
	       		maven('''-Dcommonreality.eclipse.version='''+newVersion.replaceAll('-', '.')+''' \
	       				 -DskipTests=true \
	       				 -DskipITs=true \
	       				 site-deploy''')
	     	}
         }
   }
}

// TODO: Move to workflowLibs
def maven(String optionsAndGoals) {
   sh '''mvn \
   		 -Djarsigner.keystore.path=$PATH_TO_JARSIGNER_KEYSTORE \
   		 -Dgpg.publicKeyring=$PATH_TO_GPG_PUBLIC_KEYRING \
   		 -Dgpg.secretKeyring=$PATH_TO_GPG_SECRET_KEYRING \
         --errors \
         --settings $PATH_TO_SETTINGS_XML \
         -DnewVersion='''+Config.newVersion+''' \
         '''+optionsAndGoals
}

// TODO: Move to workflowLibs
// TODO: Auto-assign version numbers via an algorithm that
//		 * does not yield un-deployed versions for failed builds
// 		 * permits major and minor numbers to be incremented via tags in the commit message
//		 * starts with a patch number of 0 if the minor was incremented (same for minor if major was incremented)
def getNextVersion() {
	def tmpDir=pwd tmp: true

	// Get last release version
	def mavenMetaDataFile = tmpDir+'/maven-metadata.xml'
	def versionFile = tmpDir+'/maven.release'
	sh 'curl --silent '+Config.releaseMetaDataURL+' > '+mavenMetaDataFile
	sh 'xpath -e metadata/versioning/release -q '+mavenMetaDataFile+' | sed --regexp-extended "s/<\\/?release>//g" > '+versionFile
	def oldVersion = readFile(versionFile).trim()
	sh 'rm '+mavenMetaDataFile
	sh 'rm '+versionFile

    // Determine last commit message
    def commitFile=tmpDir+'/last-commit-message.txt'
    sh 'git log --max-count=1 > '+commitFile
    def lastCommitMessage = readFile commitFile
    sh 'rm '+commitFile
    
    // Determine last commit hash
    def commitHashFile=tmpDir+'/last-commit-hash.txt'
    sh 'git log --oneline --max-count=1 | cut --delimiter=" " --fields=1 >'+commitHashFile
    def lastCommitHash = readFile(commitHashFile).trim()
    sh 'rm '+commitHashFile
	
	// Create new version number
	def newVersion = oldVersion
	def oldVersionWithoutQualifier = oldVersion.split("-")[0]
	String[] parts = oldVersionWithoutQualifier.split("\\.")
	if(lastCommitMessage.contains("+majorVersion")) {
		newVersion = (parts[0].toInteger()+1)+".0.0"
	} else if(lastCommitMessage.contains("+minorVersion")) {
		newVersion = parts[0]+"."+(parts[1].toInteger()+1)+".0"
	} else {
		newVersion = parts[0]+"."+parts[1]+"."+(parts[2].toInteger()+1)
	}
	
	// Add last commit hash so permit version numbers to be correlated with Git commits.
	// Note that the version number in this format of suitable for Maven, but not for Eclipse.
	// While Maven versions have the format /<major>.<minor>.<patch>-<qualifier>/ , 
	//     Eclipse versions have the format /<major>.<minor>.<patch>.<qualifier>/ ,
	// thus - needs to be replaced by . to create the latter out of the former.
	newVersion += '-'+lastCommitHash
	echo "Updating version $oldVersion -> $newVersion"
	currentBuild.displayName = '#'+currentBuild.number+' v'+newVersion
	return newVersion
}

// TODO: Move to workflowLibs
def installToolsIfNecessary() {
    // Retry is necessary because downloads via apt-get are unreliable
   	retry(3) {
	   sh '''echo "deb http://http.debian.net/debian jessie-backports main" > /etc/apt/sources.list.d/jessie-backports.list \
	        && apt-get update \
	        && apt-get remove --yes openjdk-7-jdk \
	        && apt-get install --yes openjdk-8-jre-headless openjdk-8-jdk \
	        && /usr/sbin/update-java-alternatives -s java-1.8.0-openjdk-amd64 \
	        && apt-get install --yes curl git maven libxml-xpath-perl'''
    }
}