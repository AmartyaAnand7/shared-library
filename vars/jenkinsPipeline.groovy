def call(String innerstageName , String branchName , String repoUrl) {
  pipeline {
       agent any
       stages {
           stage("Checkout Code") {
               environment {
					GITHUB_CREDENTIAL_ID = 'jenkins-operator'
				}
				parallel {
					stage("${innerstageName}") {
						steps {
							sh ' if [ -d "${innerstageName}"]; then rm -Rf "${innerstageName}"; fi; mkdir "${innerstageName}"'
							dir ("${innerstageName}") {
								script{STAGE_NAME="Checkout Code"}
								git branch: "${branchName.split("/")[2]}",
								credentialsId: "${env.GITHUB_CREDENTIAL_ID}",
								url: "${repoUrl}"
							}
						}
					}
				}
           }
           stage("Build") {
                     parallel {
			     stage("${innerstageName}") {
						agent {
							docker {
								image 'maven:3-alpine'
								args '-v $HOME/.m2:/root/.m2'
								reuseNode true
							}
						}
						steps {
							script{STAGE_NAME="Build Stage"}
							dir ("${innerstageName}") {
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
					stage('Test - "${innerstageName}"') {
						agent {
							docker {
								image 'maven:3-alpine'
								args '-v $HOME/.m2:/root/.m2'
								reuseNode true
							}
						}
						steps {
							script{STAGE_NAME="Unit Test"}
							dir ("${innerstageName}") {
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
