def call(String repoUrl , String branchName) {
  pipeline {
       agent any
       stages {
           stage("Checkout Code") {
               environment {
					GITHUB_CREDENTIAL_ID = 'jenkins-operator'
				}
				parallel {
					stage('smartcid') {
						steps {
							sh ' if [ -d "smartcid" ]; then rm -Rf "smartcid"; fi; mkdir smartcid'
							dir ('smartcid') {
								script{STAGE_NAME="Checkout Code"}
								git branch: "${params.branchName.split("/")[2]}",
								credentialsId: "${env.GITHUB_CREDENTIAL_ID}",
								url: "${repoUrl}"
							}
						}
					}
		}
           }
           stage("Build") {
                     parallel {
					stage('New SmartCID') {
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
		     }   
           }
           stage("Unit Test") {
		   when {
					expression { params.INCLUDE_UNIT_TESTING == true }
				}
				parallel {
					stage('Test - smartcid') {
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
		    }
                   
           }
           
       }
   }
}
