def call(String repoUrl, String branchName, String directoryName, String projectName) {
    pipeline {
       agent any
       parameters {
	    booleanParam(
		name: 'INCLUDE_BUILD_STAGE',
		defaultValue: true,
		description: 'Tick if build has to be performed',
	    )
	    booleanParam(
		name: 'INCLUDE_UNIT_TESTING',
		defaultValue: true,
		description: 'Tick if unit testing has to be performed',
	    )
	    booleanParam(
		name: 'INCLUDE_SONAR_REPORT_STAGE',
		defaultValue: true,
		description: 'Tick if sonar stage has to be performed',
	    )
	}
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
		  when {
		     expression { params.INCLUDE_BUILD_STAGE == true }
		  }
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
		   when {
		     expression { params.INCLUDE_UNIT_TESTING == true }
		   }
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
		    when {
		      expression { params.INCLUDE_SONAR_REPORT_STAGE == true }
		   }
		   agent {
			docker {
			        image 'maven:3-alpine'
				args '-v $HOME/.m2:/root/.m2'
				reuseNode true
			}
		   }
		   steps {
			dir ("${directoryName}") {
				sh "pwd"
				sh "mvn -f /var/lib/jenkins/workspace/shared,library/oca/pom.xml -DskipTests sonar:sonar -Dsonar.host.url=http://16.107.50.87:8090 -Dsonar.exclusions=**/*.ts -Dsonar.analysis.mode=publish -Dsonar.projectName= ${projectName}"
			}
		  }
	   }
       }
   }
}
