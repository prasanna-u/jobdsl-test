pipeline {
    agent any
    stages {
        stage('Trigger') {
            steps {
                script {
                	jobDsl targets: 'devops.groovy'
                    jobDsl targets: 'devops.groovy', removedJobAction: 'DELETE', removedViewAction: 'DELETE'
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