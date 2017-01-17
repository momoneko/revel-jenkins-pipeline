node {
    stage("checkout"){
        sh 'mkdir -p gopath/src'
        checkout([$class: 'GitSCM', 
                branches: scm.branches,
                doGenerateSubmoduleConfigurations: false,
                extensions: scm.extensions + [
                    [$class: 'RelativeTargetDirectory', relativeTargetDir: 'gopath/src/project'],
                    [$class: 'CleanBeforeCheckout']], 
                submoduleCfg: [])
                
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
