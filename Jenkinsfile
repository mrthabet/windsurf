pipeline {
  agent any

  options {
    timestamps()
    buildDiscarder(logRotator(numToKeepStr: '20'))
    disableConcurrentBuilds()
  }

  parameters {
    string(name: 'BASE_URL', defaultValue: 'https://tarh-test.vercel.app', description: 'Base URL')
    string(name: 'EMAIL', defaultValue: 'fp_user_1@sa.com', description: 'Login email')
    password(name: 'PASSWORD', defaultValue: 'secret@123', description: 'Login password')
    string(name: 'OTP', defaultValue: '123456', description: 'One-time password')
    string(name: 'TEST_CLASS', defaultValue: 'FundingWorkflowTest', description: 'TestNG class to run')
  }

  environment {
    // Additional environment can be set here if needed
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build & Test') {
      steps {
        script {
          def commonProps = "-Dtest=${params.TEST_CLASS} -Dbase.url=\"${params.BASE_URL}\" -Demail=\"${params.EMAIL}\" -Dpassword=\"${params.PASSWORD}\" -Dotp=\"${params.OTP}\" -Dbrowser=chrome -Dheadless=true"
          def mvnCmd = isUnix() ? "mvn -B -q ${commonProps} test" : "mvn -B -q ${commonProps} test"
          if (isUnix()) {
            sh "${mvnCmd}"
          } else {
            bat "${mvnCmd}"
          }
        }
      }
      post {
        always {
          archiveArtifacts allowEmptyArchive: true, artifacts: 'target/surefire-reports/**, allure-results/**'
          junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
          script {
            try {
              // If Allure plugin installed
              allure includeProperties: false, jdk: '', results: [[path: 'allure-results']]
            } catch (err) {
              echo "Allure plugin not configured: ${err}"
            }
          }
        }
      }
    }
  }
}
