// https://jenkins.io/doc/book/pipeline/syntax/
@Library('alauda-cicd') _

// global variables for pipeline
def GIT_BRANCH
def GIT_COMMIT
def FOLDER = "."
// image can be used for promoting...
def IMAGE
def CURRENT_VERSION
def code_data
def DEBUG = false
def deployment
def RELEASE_VERSION
def RELEASE_BUILD
def TEST_IMAGE
pipeline {
	// 运行node条件
	// 为了扩容jenkins的功能一般情况会分开一些功能到不同的node上面
	// 这样每个node作用比较清晰，并可以并行处理更多的任务量
	agent { label 'all && java' }

	// (optional) 流水线全局设置
	options {
		// 保留多少流水线记录（建议不放在jenkinsfile里面）
		buildDiscarder(logRotator(numToKeepStr: '10'))

		// 不允许并行执行
		disableConcurrentBuilds()
	}

	//(optional) 环境变量
	environment {
		// for building an scanning
		JENKINS_IMAGE = "jenkins/jenkins:lts"
		REPOSITORY = "alauda-devops-pipeline-plugin"
		OWNER = "alauda"
		IMAGE_TAG = "dev"
		// sonar feedback user
		// needs to change together with the credentialsID
		SCM_FEEDBACK_ACCOUNT = "alaudabot"
		SONARQUBE_SCM_CREDENTIALS = "alaudabot"
		DEPLOYMENT = "alauda-devops-pipeline-plugin"
		DINGDING_BOT = "devops-chat-bot"
		TAG_CREDENTIALS = "alaudabot-github"
		IN_K8S = "true"
	}
	// stages
	stages {
		stage('Checkout') {
			steps {
				script {
					// checkout code
					def scmVars = checkout scm
					// extract git information
					env.GIT_COMMIT = scmVars.GIT_COMMIT
					env.GIT_BRANCH = scmVars.GIT_BRANCH
					GIT_COMMIT = "${scmVars.GIT_COMMIT}"
					GIT_BRANCH = "${scmVars.GIT_BRANCH}"
					pom = readMavenPom file: 'pom.xml'
					//RELEASE_VERSION = pom.properties['revision'] + pom.properties['sha1'] + pom.properties['changelist']
					RELEASE_VERSION = pom.version
					RELEASE_BUILD = "${RELEASE_VERSION}.${env.BUILD_NUMBER}"
					if (GIT_BRANCH != "master") {
						def branch = GIT_BRANCH.replace("/","-").replace("_","-")
						RELEASE_BUILD = "${RELEASE_VERSION}.${branch}.${env.BUILD_NUMBER}"
					}

					sh 'echo "commit=$GIT_COMMIT" > src/main/resources/debug.properties'
					sh 'echo "build=$RELEASE_BUILD" >> src/main/resources/debug.properties'
				}
				// installing golang coverage and report tools
				sh "go get -u github.com/alauda/gitversion"
				script {
					if (GIT_BRANCH == "master") {
						sh "gitversion patch ${RELEASE_VERSION} > patch"
						RELEASE_BUILD = readFile("patch").trim()
					}
					echo "release ${RELEASE_VERSION} - release build ${RELEASE_BUILD}"
				}
			}
		}
		stage('CI'){
			failFast true
			parallel {
				stage('Build') {
					steps {
						script {
							// setup kubectl
							if (GIT_BRANCH == "master") {
								// master is already merged
								deploy.setupStaging()

							} else {
								// pull-requests
								deploy.setupInt()
							}

							sh """
                                mvn clean install -U -Dmaven.test.skip=true
                                # tests needs refactoring, still using the same host address for multiple jenkins instances
                                # mvn clean install -U findbugs:findbugs

                                if [ -d .tmp ]; then
                                  rm -rf .tmp
                                fi;

                                mkdir .tmp
                                cp artifacts/images/* .tmp
                                cp target/*.hpi .tmp
                            """

                            archiveArtifacts 'target/*.hpi'
						}
					}
				}
			}
		}

		// after build it should start deploying
		stage('Promoting') {
			// limit this stage to master only
			when {
				expression { GIT_BRANCH == "master" }
			}
			steps {
				script {
					// promote to release
					IMAGE.push("release")

					// adding tag to the current commit
					withCredentials([usernamePassword(credentialsId: TAG_CREDENTIALS, passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
						sh "git tag -l | xargs git tag -d" // clean local tags
						sh """
                        git config --global user.email "alaudabot@alauda.io"
                        git config --global user.name "Alauda Bot"
                    """
						def repo = "https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/${OWNER}/${REPOSITORY}.git"
						sh "git fetch --tags ${repo}" // retrieve all tags
						sh("git tag -a ${RELEASE_BUILD} -m 'auto add release tag by jenkins'")
						sh("git push ${repo} --tags")
					}
				}
			}
		}

		// sonar scan
		stage('Sonar') {
			steps {
				script {
					deploy.scan(
						REPOSITORY,
						GIT_BRANCH,
						SONARQUBE_SCM_CREDENTIALS,
						FOLDER,
						DEBUG,
						OWNER,
						SCM_FEEDBACK_ACCOUNT).startToSonar()
				}
			}
		}
	}

	// (optional)
	// happens at the end of the pipeline
	post {
		// 成功
		success {
			echo "Horay!"
			script {
				deploy.notificationSuccess(DEPLOYMENT, DINGDING_BOT, "流水线完成了", RELEASE_BUILD)
			}
		}
		// 失败
		failure {
			// check the npm log
			// fails lets check if it
			script { echo "damn!" // deploy.notificationFailed(DEPLOYMENT, DINGDING_BOT, "流水线失败了", RELEASE_BUILD)
			}
		}
		always { junit allowEmptyResults: true, testResults: "**/target/surefire-reports/**/*.xml" }
	}
}

