pipeline {
    agent any

    tools {
        maven 'mvn'
    }

    stages {
        stage('Checkout code') {
            steps {
                git branch: 'main', url: 'https://github.com/Raed-Bourouis/country-service.git'
            }
        }

        stage('Compile, test code, package in war file and store in maven repo') {
            steps {
                sh 'mvn clean install -DskipTests'
            }
            post {
                success {
                    junit allowEmptyResults: true,
                        testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }
        stage('Deploy using Ansible playbook') {
            steps {
                script {
                    withCredentials([string(credentialsId: 'ansible-vault', variable: 'VAULT_PASS')]) {
                        sh '''
                        echo "$VAULT_PASS" > vault_pass.txt
                        ansible-playbook playbookCICD.yml --vault-password-file vault_pass.txt
                        rm -f vault_pass.txt
                    '''
                    }
                }
            }
        }
    }

    post {
        always {
            cleansWs()
        }
        success {
            echo 'Pipeline executed successfully!'
        }
        failure {
            echo 'Pipeline failed! Check console output for details.'
        }
    }
}
