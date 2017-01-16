node {
   checkout scm
   def ci = load("./deploy/ci.groovy");

   ci.onFail {
       currentBuild.result = 'FAILURE'
       notifyJobFailed()
   }

   ci {

       stage ('unit_test') {
           runUnitTests()
       }

       stage ('integration_test') {
           runIntegrationTests()
       }

       stage ('provision') {
           runProvisioning()
       }

       stage ('build') {
           runBuild()
       }

       //notifyJobSucceed()
   }



}
