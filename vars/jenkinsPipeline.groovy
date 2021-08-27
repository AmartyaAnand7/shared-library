def call(String branchName , String repoUrl , String personalaccessToken) {
    pipeline {
       agent any
       parameters {
	   listGitBranches branchFilter: '(.*)', credentialsId: 'jenkins-operator', defaultValue: 'refs/heads/develop', name: "${branchName}", description: 'Branch in new backend repo',quickFilterEnabled: false, remoteURL: "${repoUrl}", selectedValue: 'DEFAULT',type: 'PT_BRANCH'    
       }
       stages {
           stage("Checkout Code") {
               environment {
			GITHUB_CREDENTIAL_ID = 'jenkins-operator'
		}
		steps {
		    sh ' if [ -d "smartcid" ]; then rm -Rf "smartcid"; fi; mkdir smartcid'
		    dir ('smartcid') {
				script{STAGE_NAME="Checkout Code"}
				git credentialsId: "${env.GITHUB_CREDENTIAL_ID}",
			            branch: "${params.BRANCH_IN_NEW_BACKEND.split("/")[2]}",
				    url: "${repoUrl}"
		    }
		}
	   }
           stage("Build") {
                 agent {
		    docker {
		       image 'maven:3-alpine'
		       args '-v $HOME/.m2:/root/.m2'
		       reuseNode true
		    }
		  }
		  steps {
		     script{STAGE_NAME="Build Stage"}
		     dir ('smartcid') {
			sh 'mvn clean install -DskipTests '
		     }
		  }
	   }
           stage("Unit Test") {
		  agent {
		      docker {
			image 'maven:3-alpine'
			args '-v $HOME/.m2:/root/.m2'
			reuseNode true
		      }
		  }
		  steps {
			script{STAGE_NAME="Unit Test"}
			dir ('smartcid') {
				sh 'mvn test'
				sh 'mvn speedy-spotless:install-hooks'
				sh 'mvn speedy-spotless:check'
			}
		  }
		  post {
		        always {
			        junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
			}
		  }
	   }
	   stage("Sonar Report") {
		   agent {
			docker {
			        image 'maven:3-alpine'
				args '-v $HOME/.m2:/root/.m2'
				reuseNode true
			}
		   }
		   steps {
			dir ('smartcid') {
				sh 'mvn -DskipTests sonar:sonar -Dsonar.host.url=http://16.107.50.87:8090 -Dsonar.exclusions=**/*.ts -Dsonar.analysis.mode=publish -Dsonar.projectName=SCID-NEW'
			}
		  }
	   }
       }
   }
}
