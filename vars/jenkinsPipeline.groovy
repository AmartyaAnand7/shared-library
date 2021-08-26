def call(String repoUrl , String branchName) {
  pipeline {
       agent any
       stages {
           stage("Checkout Code") {
               steps {
                   git branch: "${branchName}",
                       url: "${repoUrl}"
                  
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
