pipeline {
    agent { label 'buildeng-automation' }
    stages {
        stage('Trigger') {
            steps {
                script {
                	jobDsl targets: 'jenkins/casc/jobs/devops.groovy'
                }
                script {
                    jobDsl removedJobAction: 'DELETE', removedViewAction: 'DELETE', targets: 'jenkins/casc/jobs/devopssetup.groovy' 
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