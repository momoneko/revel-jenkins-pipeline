import groovy.transform.Field
import groovy.json.JsonOutput

@Field Boolean publish = false
@Field failClosures = []

def onFail (Closure cl) {
    cl.delegate = this
    failClosures.add(cl)
}

def runProvisioning() {
    // tmp'
    // sh 'cp ./conf/settings.conf.sample ./conf/settings.conf'
    sh 'echo $PATH'
    sh 'echo $(pwd)'
    sh 'ls'
   withEnv(["GOPATH=~/go-workspace"]) {
        // -p so that mkdir whines when folders exists
        sh 'mkdir -p $GOPATH'
        withEnv(["PATH=$PATH:/usr/local/bin/"]) {
                sh 'glide install'
            }
    }
}


def runPHPUnitTests() {
    try{
      sh 'php ./bin/phpunit --coverage-clover=CloverReport.xml'
      publishCloverResults()
    } catch(e){
      publishCloverResults()
      throw e
    }
}


def runPHPCodeStyleCheck() {
    try{
      sh './vendor/squizlabs/php_codesniffer/scripts/phpcs --standard=ruleset.xml --report=checkstyle --report-file=CSReport.xml ./src ./tests'
      publishPHPCodeStyleCheckResult()
    } catch(e){
      publishPHPCodeStyleCheckResult()
      throw e
    }
}

def publishPHPCodeStyleCheckResult(){
    if ( !this.publish) {
        return
    }
    step(
        [
            $class         : "CheckStylePublisher",
            canComputeNew  : false,
            defaultEncoding: "",
            healthy        : "100",
            pattern        : "CSReport.xml",
            unHealthy      : "100"
        ]
    )
}




def publishCloverResults(){
    if ( !this.publish) {
        return
    }
  step(
      [
          $class: 'CloverPublisher',
          cloverReportDir: './',
          cloverReportFileName: 'CloverReport.xml',
          healthyTarget: [
            methodCoverage: 80,
            conditionalCoverage: 80,
            statementCoverage: 80
          ],
          unhealthyTarget: [
            methodCoverage: 60,
            conditionalCoverage: 60,
            statementCoverage: 60
          ],
          failingTarget: [
            methodCoverage: 40,
            conditionalCoverage: 40,
            statementCoverage: 40
          ]
      ]
  )
}

def runJSUnitTests() {
    sh 'npm test'
}

def runDeployCRCST() {
    stage 'deploy_cr_cst'
    build 'cr-cs-tool-dev'
}

def installBuild(projectDir, stageName) {
    stage stageName
    def releasesDir = projectDir + '/releases'
    def currentReleaseDir = releasesDir + '/' + currentBuild.number
    def currentSymlink = projectDir + '/current'

    sh 'sudo mkdir -p ' + currentReleaseDir
    sh 'sudo chown -R vagrant:vagrant ' + currentReleaseDir

    dir(currentReleaseDir) {
      unstash "ci_stash"
      sh 'sudo rm -f ./app/config/parameters.yml'
      sh 'sudo rm -rf ./app/cache/*'
      sh 'cp ../../parameters.yml ./app/config/parameters.yml'

      //TODO: find a better way to get rid of the following parallel statement
      //the following hack is required because of diffs between OSs
      parallel(
        'npm_rebuild': {
          sh 'npm rebuild'
        },
        'composer_install': {
          sh 'composer update'
        }
      )
      sh 'gulp build'
    }

    sh 'sudo chown -R apache:apache ' + currentReleaseDir
    sh 'sudo rm -f ' + currentSymlink
    sh 'sudo ln -s ' + currentReleaseDir + ' ' + currentSymlink
}

def slackNotify(message, color) {
    slackSend channel: '#ellation_cs_service', color: color, message: message, teamDomain: 'ellation', token: '4OfkpfeLYzA0AFpNdAnj26fT'
}

def notifyJobStarted() {
    slackNotify("STARTED: Job ${env.JOB_NAME} [${env.BUILD_NUMBER}] (${env.BUILD_URL})", '#ff9966')
}

def notifyJobFailed() {
    slackNotify("FAILED: Job ${env.JOB_NAME} [${env.BUILD_NUMBER}] (${env.BUILD_URL})", '#ff0000')
}

def notifyJobSucceed() {
    slackNotify("SUCCESS: Job ${env.JOB_NAME} [${env.BUILD_NUMBER}] (${env.BUILD_URL})", 'good')
}

def call(Closure cl)
{
   try {
     cl.delegate = this
     cl.call()
    }
   catch (e) {
        callClosures(failClosures)
        throw e
   }
}

/**
* These are the default steps which should be called on the master pipeline to ensure the code tests are ok
*/
def call() {
    stage 'code'
    runProvisioning()
    runPHPCodeStyleCheck()
    runPHPUnitTests()
    runJSUnitTests()
}

def callClosures(closureList) {
        for (def i=0;i < closureList.size();i ++) {
            println "Executing Closure"
            def cl = closureList[i]
            cl.delegate = this
            cl.call()
        }
}

return this
