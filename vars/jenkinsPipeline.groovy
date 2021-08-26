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
                   echo "cleaned"
               }
           }
           stage("Build") {
               steps {
                   echo "built"
               }
           }
           stage("Running Testcase") {
              steps {
                   echo "tested"
               }
           }
           stage("Packing Application") {
               steps {
                   echo "packed"
               }
           }
       }
   }
}
