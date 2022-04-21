pipeline {
    agent any
    stages {
        stage('Trigger') {
            steps {
                script {
                	jobDsl targets: 'devops.groovy'
                }
                script {
                    jobDsl removedJobAction: 'DELETE', removedViewAction: 'DELETE', targets: 'devopssetup.groovy' 
                }
            }
        }
        //stage('Cleanup') {
        //    steps {
        //        script {
        //        	jobDsl targets: 'jenkins/casc/jobs/devopscleanup.groovy'
        //        }
        //    }
        //}
    }
}