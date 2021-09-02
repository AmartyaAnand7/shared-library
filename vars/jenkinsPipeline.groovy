def call(String branchName , String repoUrl, String projectName) {
    pipeline {
       agent any
       stages {
           stage("Checkout Code") {
               environment {
			GITHUB_CREDENTIAL_ID = 'scid-jenkins-operator'
		        BRANCH_NAME = "${branchName}"
		        PROJECT_NAME = "${projectName}"
		}
		steps {
			sh ' if [ -d "${env.PROJECT_NAME}" ]; then rm -Rf "${env.PROJECT_NAME}"; fi; mkdir ${env.PROJECT_NAME}'
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
