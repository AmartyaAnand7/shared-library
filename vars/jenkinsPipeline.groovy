def call(String repoUrl) {
  pipeline {
       agent any
       stages {
           stage("Checkout Code") {
               steps {
                   git branch: 'master',
                       url: "${repoUrl}"
               }
           }
           stage("Cleaning workspace") {
               steps {
                   sh "cleaned"
               }
           }
           stage("Build") {
               steps {
                   sh "builded"
               }
           }
           stage("Running Testcase") {
              steps {
                   sh "tested"
               }
           }
           stage("Packing Application") {
               steps {
                   sh "packed"
               }
           }
       }
   }
}
