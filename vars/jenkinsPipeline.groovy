def call(String directoryName , String branchName , String repoUrl) {
  pipeline {
       agent any
       stages {
           stage("Checkout Code") {
               environment {
					GITHUB_CREDENTIAL_ID = 'jenkins-operator'
				}
				
						steps {
							sh ' if [ -d ${directoryName}]; then rm -Rf ${directoryName}; fi; mkdir ${directoryName}'
							dir ("${directoryName}") {
								script{STAGE_NAME="Checkout Code"}
								git branch: "${branchName.split("/")[2]}",
								credentialsId: "${env.GITHUB_CREDENTIAL_ID}",
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
							dir ("${innerstageName}") {
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
           
       
       }
   }
}
