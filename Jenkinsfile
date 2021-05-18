pipeline {
    agent any

    environment {
        PROJECT_NAME = 'blockchain-logger'
        HELM_PROJECT_NAME = 'bcos-logger'
    }

    stages {
        stage('init') {
            steps {
                script {
                    begin = new Date()
                    start_time = begin.format('yyyy-MM-dd HH:mm:ss')
                    DATETIME_TAG = begin.format('yyyyMMddHHmmss')
                    IMAGE_TAG_NAME = 'ts' + DATETIME_TAG
                }
            }
        }

        stage('create&push docker image') {
            steps {
                sh './mvnw clean package'
                sh "docker tag docker-private.cnhealth.com/lr-apps/${PROJECT_NAME}:latest docker-private.cnhealth.com/lr-apps/${PROJECT_NAME}:${IMAGE_TAG_NAME}"

                sh "docker push docker-private.cnhealth.com/lr-apps/${PROJECT_NAME}:latest"
                sh "docker push docker-private.cnhealth.com/lr-apps/${PROJECT_NAME}:${IMAGE_TAG_NAME}"
            }
        }

        stage('update helm script') {
            steps {
                sh """#!/bin/sh -l
                      cd helm
                      helm package ${HELM_PROJECT_NAME}/
                      curl http://helm.cnhealth.com/repository/lrhelm/ --upload-file ${HELM_PROJECT_NAME}-0.1.0.tgz -v -n"""
            }
        }

        stage('deploy') {
            steps {
                sh """
                      ssh -p 29022 root@192.167.148.60 "helm repo update"
                      ssh -p 29022 root@192.167.148.60 "helm fetch lrhelm/${HELM_PROJECT_NAME}"
                      ssh -p 29022 root@192.167.148.60 "helm uninstall ${HELM_PROJECT_NAME} --namespace lr-app || echo ${HELM_PROJECT_NAME} helm instance does not exist"
                      ssh -p 29022 root@192.167.148.60 "helm install ${HELM_PROJECT_NAME} --namespace lr-app --create-namespace --set 'image.tag=${IMAGE_TAG_NAME}' lrhelm/${HELM_PROJECT_NAME}" """
            }
        }
    }

    /* post {
        success {
            script {
                finish = new Date()
                end_time = finish.format('yyyy-MM-dd HH:mm:ss')
                elaspe = ((finish.getTime() - begin.getTime()) / 1000) as int
            }
            httpRequest contentType: 'APPLICATION_JSON_UTF8', httpMode: 'POST', ignoreSslErrors: true, requestBody: """{
                "msgtype": "markdown",
                "markdown": {
                    "content":
                       "项目<font color=\\"info\\">${PROJECT_NAME}</font> 由流水线 [${JOB_NAME}](${JOB_URL}) 构建<font color=\\"info\\">成功</font>, 用时<font color=\\"info\\">${elaspe}s</font>
                        > 开始时间: <font color=\\"comment\\">${start_time}</font>
                        > 结束时间: <font color=\\"comment\\">${end_time}</font>
                        > 查看[控制台](${BUILD_URL}console)
                        > 打开[应用部署地址](http://120.221.160.236:32335/)"
                }
            }""", url: 'https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=db63f754-1400-4bd6-a901-a31eca138140'
        }
        failure {
            script {
                finish = new Date()
                end_time = finish.format('yyyy-MM-dd HH:mm:ss')
                elaspe = ((finish.getTime() - begin.getTime()) / 1000) as int
            }
            httpRequest contentType: 'APPLICATION_JSON_UTF8', httpMode: 'POST', ignoreSslErrors: true, requestBody: """{
                "msgtype": "markdown",
                "markdown": {
                    "content":
                       "项目<font color=\\"warning\\">${PROJECT_NAME}</font> 由流水线 [${JOB_NAME}](${JOB_URL}) 构建`失败`, 用时<font color=\\"info\\">${elaspe}s</font>
                        > 开始时间: <font color=\\"comment\\">${start_time}</font>
                        > 结束时间: <font color=\\"comment\\">${end_time}</font>
                        > 查看[控制台](${BUILD_URL}console)"
                }
            }""", url: 'https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=db63f754-1400-4bd6-a901-a31eca138140'}
        }
    } */
}

