pipeline {
    agent {
        label 'java'
    }

    stages {
        stage('test sonar') {
            steps {
                script {
                    container('tools') {
                        alaudaPlatform.withBindInProjectSonarEnv("zxj", "sonar-zxj") {
                            sh "echo \"SonarQube Server URL is $SONAR_SERVER_URL\""
                            sh "echo \"SonarQube Server token is $SONAR_TOKEN\""
                            sh "sonar-scanner -D sonar.host.url=$SONAR_SERVER_URL -D sonar.login=$SONAR_TOKEN  -D sonar.projectName=sonar-scan-demo -D sonar.projectKey=demo"
                        }
                    }
                }
            }
        }
    }
}
