def call(String repoUrl, String branchName, String directoryName, String projectName) {
    pipeline {
       agent any
       stages {
           stage("Checkout Code") {
               environment {
			GITHUB_CREDENTIAL_ID = 'scid-jenkins-operator'
		}
		steps {
			sh " if [ -d ${directoryName} ]; then rm -Rf ${directoryName}; fi; mkdir ${directoryName}"
			dir ("${directoryName}") {
				script{STAGE_NAME="Checkout Code"}
				git credentialsId: "${env.GITHUB_CREDENTIAL_ID}",
				    branch: "${branchName}",
				    url: "${repoUrl}"
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
			dir ("${directoryName}") {
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
			dir ("${directoryName}") {
				sh "mvn clean install  pom.xml sonar:sonar -Dsonar.host.url=http://16.107.50.87:8090 -Dsonar.exclusions=**/*.ts -Dsonar.analysis.mode=publish -Dsonar.projectName= ${projectName}"
			}
		  }
	   }
       }
   }
}
