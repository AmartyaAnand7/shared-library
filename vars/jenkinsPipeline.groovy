def call(String repoUrl, String branchName, String projectName) {
    pipeline {
       agent any
       stages {
           stage("Checkout Code") {
               environment {
			GITHUB_CREDENTIAL_ID = 'scid-jenkins-operator'
		}
		steps {
			sh " if [ -d ${projectName} ]; then rm -Rf ${projectName}; fi; mkdir ${projectName}"
			dir ("${projectName}") {
				script{STAGE_NAME="Checkout Code"}
				git credentialsId: "${env.GITHUB_CREDENTIAL_ID}",
				    branch: "${branchName}",
				    url: "${repoUrl}"
			}
		}
	   }
           stage("Build") {
		   steps{
			   echo "built"
		   }
	   }
           stage("Unit Test") {
		   steps{
			   echo "tested"
		   }
	   }
	   stage("Sonar Report") {
		   steps{
			   echo "scanned"
		   }
	   }
       }
   }
}
