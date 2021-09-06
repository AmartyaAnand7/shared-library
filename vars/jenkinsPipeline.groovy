def call(String repoUrl) {
    pipeline {
       agent any
       stages {
           stage("Checkout Code") {
               environment {
			GITHUB_CREDENTIAL_ID = 'scid-jenkins-operator'
		        BRANCH_NAME = 'develop'
		        
		}
		steps {
			sh ' if [ -d "smartcid" ]; then rm -Rf "smartcid"; fi; mkdir smartcid'
			dir ('smartcid') {
				script{STAGE_NAME="Checkout Code"}
				git credentialsId: "${env.GITHUB_CREDENTIAL_ID}",
				    branch: "${env.BRANCH_NAME}",
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
