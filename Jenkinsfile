node {
    stage("checkout"){
        sh 'mkdir -p gopath/src'
        checkout([$class: 'GitSCM', 
                branches: [[name: '*/master']],
                doGenerateSubmoduleConfigurations: false,
                extensions: [
                    [$class: 'RelativeTargetDirectory', relativeTargetDir: 'gopath/src/project'],
                    [$class: 'CleanBeforeCheckout']], 
                submoduleCfg: [],
                userRemoteConfigs: [
                    [credentialsId: 'b952124c-8888-48ce-b77d-1b1599be8673',
                    url: 'https://github.com/momoneko/revel-jenkins-pipeline.git']]
                ])
    }
    withEnv(["GOPATH=${WORKSPACE}/gopath", 'PATH=$PATH:/usr/local/bin']) {
            dir('gopath/src/project') {
                stage('provision') {
                    withEnv(['GLIDEBIN=/usr/local/bin/glide']) {
                        sh '$GLIDEBIN install'
                        sh 'go get -v github.com/revel/revel'
                        sh 'go get -v github.com/revel/cmd/revel'
                    }
                }
                stage ('test') {
                    withEnv(["REVELIMPORTPATH=project"]) {
                        sh "go test ./app/test_package"
                    }
                }
                stage ('build') {
                    sh '$GOPATH/bin/revel package project'
                }
        }
    }
        
}
