def call(String branchName , String repoUrl) {
    pipeline {
       agent any
       parameters {
	   listGitBranches branchFilter: '(.*)', credentialsId: 'scid-jenkins-operator', defaultValue: 'refs/heads/develop', name: "${branchName}", description: 'Branch in new backend repo',quickFilterEnabled: false, remoteURL: "${repoUrl}", selectedValue: 'DEFAULT',type: 'PT_BRANCH'    
       }
       stages {
           stage("Checkout Code") {
               environment {
			GITHUB_CREDENTIAL_ID = 'scid-jenkins-operator'
		        BRANCH_NAME = "${branchName}"
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
