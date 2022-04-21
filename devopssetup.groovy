pipelineJob('test') {
    definition {
        cps {
                script{
                    jobDsl targets: 'devops.groovy'
                    jobDsl targets: 'devopssetup.groovy', removedJobAction: 'DELETE', removedViewAction: 'DELETE'
                }
        }
    }
}