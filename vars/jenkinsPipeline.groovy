def call(String branchName , String repoUrl , String projectName) {
    pipeline {
       agent any
       parameters {
	   listGitBranches branchFilter: '(.*)', credentialsId: 'scid-jenkins-operator', defaultValue: 'refs/heads/develop', name: "${branchName}", description: 'Branch in new backend repo',quickFilterEnabled: false, remoteURL: "${repoUrl}", selectedValue: 'DEFAULT',type: 'PT_BRANCH'    
       }
       stages {
           stage("Checkout Code") {
               environment {
			GITHUB_CREDENTIAL_ID = 'scid-jenkins-operator'
		}
		steps {
			sh ' if [ -d "${projectName}" ]; then rm -Rf "${projectName}"; fi; mkdir(${projectName})'
		    dir ('smartcid') {
				script{STAGE_NAME="Checkout Code"}
				git credentialsId: "${env.GITHUB_CREDENTIAL_ID}",
				    branch: "${params.BRANCH_IN_NEW_BACKEND.split("/")[2]}",
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
