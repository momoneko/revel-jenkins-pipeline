node("GO-slave") {
    stage("checkout"){
        sh 'mkdir -p gopath/src'
        checkout([$class: 'GitSCM', 
                branches: scm.branches,
                doGenerateSubmoduleConfigurations: false,
                extensions: scm.extensions + [
                    [$class: 'RelativeTargetDirectory', relativeTargetDir: 'gopath/src/project'],
                    [$class: 'CleanBeforeCheckout']],
                userRemoteConfigs: scm.userRemoteConfigs
                ])
    }
    withEnv(["GOPATH=${WORKSPACE}/gopath"]) {
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
                        sh "go test -cover ./app/..."
                    }
                }
                stage ('revel test') {
                    sh "$GOPATH/bin/revel test project"
                }
                    
                stage ('build') {
                    sh '$GOPATH/bin/revel package project'
                }
        }
    }
        
}
