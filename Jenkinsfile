pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                slackSend color: 'good', channel: "#ci-build", message: "[dev zone] scinapse-api Build & Push Started: ${env.BRANCH_NAME}"
                checkout scm
                sh 'git status'
            }
        }

        stage('build') {
            steps {
                script {
                    try {
                        if (env.BRANCH_NAME != 'master') {
                            sh './gradlew clean build'
                            sh 'docker build -t scinapse-api/dev .'
                        }
                    } catch (err) {
                        slackSend color: "danger", failOnError: true, message: "[dev zone] scinapse-api Build Failed: ${env.BRANCH_NAME}"
                        throw err
                    }
                }
            }
        }
        stage('Tag & Push') {
            steps {
                script {
                    try {
                        if (env.BRANCH_NAME != 'master') {
                            sh '$(aws ecr get-login --no-include-email --region us-east-1)'
                            sh "docker tag scinapse-api/dev:latest 966390130392.dkr.ecr.us-east-1.amazonaws.com/scinapse-api/dev:${env.BRANCH_NAME}"
                            sh "docker push 966390130392.dkr.ecr.us-east-1.amazonaws.com/scinapse-api/dev:${env.BRANCH_NAME}"
                        }
                    } catch (err) {
                        slackSend color: "danger", failOnError: true, message: "[dev zone] scinapse-api PUSH Failed: ${env.BRANCH_NAME}"
                        throw err
                    }
                }
            }
        }
        stage('deploy') {
            steps {
                script {
                    try {
                        if (env.BRANCH_NAME != 'master') {
                             IS_DEPLOY = sh(
                                    script: 'aws deploy list-deployments --region us-east-1  --application-name scinapse-api-dev-deploy  --deployment-group-name=scinapse-api-dev --include-only-statuses=InProgress --query "deployments[]" --output text',
                                    returnStatus: true
                                )
                             sh 'echo deploy ID: ${IS_DEPLOY}'
                             if (!IS_DEPLOY) {
                                 sh '''
                                  cd $WORKSPACE
                                  rm -rf ./deploy
                                  aws s3 sync s3://pluto-deploy/dev/scinapse-api/ ./deploy/
                                  port=80
                                  rm -f ./deploy/script/start.sh
                                  echo "#!/bin/bash" >> ./deploy/script/start.sh
                                  echo "docker run -d -p 80:80 -e DEFAULT_HOST=profile.dev-api.scinapse.io -v /var/run/docker.sock:/tmp/docker.sock:ro jwilder/nginx-proxy" >> ./deploy/script/start.sh
                                  for branch in $(git branch -r | cut -d '/' -f2 | grep -Ev '( |master)'); do
                                          ((port++))
                                      echo "docker run -d -v /srv/scinapse-api/app/application.properties:/application.properties -v /pluto/logs/:/pluto/logs/ -e WEB_PORTS=$port -e VIRTUAL_HOST=$branch.dev-api.scinapse.io -p $port:8080 966390130392.dkr.ecr.us-east-1.amazonaws.com/scinapse-api/dev:$branch" >> ./deploy/script/start.sh
                                  done
                                  aws s3 sync deploy/ s3://pluto-deploy/dev/scinapse-api/
                                  aws deploy push \
                                     --application-name scinapse-api-dev-deploy \
                                     --s3-location s3://pluto-deploy/dev/scinapse-api/archive/scinapse-api.zip \
                                     --region us-east-1 \
                                     --source deploy

                                  aws deploy create-deployment \
                                     --application-name scinapse-api-dev-deploy \
                                     --deployment-group-name=scinapse-api-dev \
                                     --s3-location bucket=pluto-deploy,key=dev/scinapse-api/archive/scinapse-api.zip,bundleType=zip \
                                     --auto-rollback-configuration enabled=true,events=DEPLOYMENT_FAILURE \
                                     --region us-east-1
                                 '''
                             }
                        }
                    } catch (err) {
                        slackSend color: "danger", failOnError: true, message: "[dev zone] scinapse-api DEPLOY Failed: ${env.BRANCH_NAME}"
                        throw err
                    }
                }
            }
        }
    }
    post {
        always {
            deleteDir()
        }
    }
}
