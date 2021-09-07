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
		     dir ("${directoryName}") {
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
		   steps {
			   echo "scanned"
		   }
	   }
       }
   }
}
