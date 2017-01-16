node {
   checkout scm
   def ci = load("./deploy/ci.groovy");

   ci.onFail {
       currentBuild.result = 'FAILURE'
       notifyJobFailed()
   }

   ci {

       stage ('unit_test') {
           sh "echo unit_test" 
           /* runUnitTests() */
       }

       stage ('integration_test') {
            sh "echo integration tests"
           /* runIntegrationTests() */
       }

       stage ('provision') {
            sh "echo provision"
           /* runProvisioning() */
       }

       stage ('build') {
            sh "echo provision"
       /*     runBuild() */
       }

       /* //notifyJobSucceed() */
   }



}
