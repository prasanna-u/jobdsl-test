import hudson.model.*
import java.text.SimpleDateFormat

def team =  TEAM_JENKINS_URL ? "${TEAM_JENKINS_URL}".replaceAll('^(http|https)://.*\\/teams-','').replaceAll('/', '') : null
def folder_path = team ? "${team}/devops" : 'devops'
def pl_runner_repo_url = 'ssh://code.devsnc.com/dev/snc-jenkins-pipelines.git'
def jobs_repo_url = 'ssh://code.devsnc.com/dev/glide-build.git'
def app_jobs_repo_url = 'ssh://code.devsnc.com/dev/snc-jenkins-pipelines.git'
def instance_url = 'https://buildtools1backend.service-now.com'

def isoFmt = new SimpleDateFormat("dd-MMM-yyyy' 'HH:mm:ss")
isoFmt.setTimeZone(TimeZone.getTimeZone('PST'))
def dateTime = isoFmt.format(new Date())

folder("${folder_path}") {
  // Add date and time stamp on the folder when last got updated
  description("<b>GENERATED ON: ${dateTime}</b>")
  properties {
    envVarsFolderProperty {
      properties """BUILDTOOLS1_URL=${instance_url}
AGENT_BUILD_IMAGE=registry.devsnc.com/devsnc/jenkins-agent:stable
PL_BUILD_CONSOLE_ARCHIVE=true
PL_TRIGGER_EVENT_URL=/api/x_snc_devops_auto/event_trigger/trigger
PL_CI_REPO_ID=devsnc-incrementals
PL_DEBUG_POD_TEMPLATE=false
PL_DEBUG_SHELL=false
PL_DEBUG_WEBHOOK=false
PL_DEBUG_MAVEN=false
PL_CI_REPO_URL=https://nexus.devsnc.com/repository/snc-maven-ci
PL_CI_MAIN_REPO_URL=https://artifact.devsnc.com/content/repositories/dev-snc-snapshots
PL_GITHUB_URL=ssh://code.devsnc.com
PL_GITREF_DIR=/projects
PL_RESOURCES_DIR=/projects/snc-jenkins-pipelines/test/resources/ws
PL_ATTACHMENT_URL=${instance_url}/api/now/attachment/file
PL_LOCK_MESSAGE_URL=${instance_url}/api/x_snc_devops_auto/execution/lock_msg_info
PL_WEBHOOK_URL=${instance_url}/api/x_snc_devops_auto/execution/status
PL_CONFLICT_URL=${instance_url}/api/x_snc_devops_auto/execution/conflict
PL_TEST_SUITE_URL=${instance_url}/api/x_snc_devops_auto/execution/test_suite
PL_IT_TEST_STATUS_API=${instance_url}/api/x_snc_devops_auto/execution/test_status
PL_EXECUTION_DATA_URL=${instance_url}/api/x_snc_devops_auto/execution/exec_data
PL_METRIC_URL=${instance_url}/api/x_snc_devops_auto/execution/metric
PL_POD_BASE_TEMPLATE=aws/base
PL_POD_ROOT_TEMPLATE=team-pod-template
PL_SEND_METRICS=true
PL_SEND_EXTENDED_METRICS=false
PL_METRIC_INTERVAL_SECS=30
PL_REPO_TEST_SUITES_PATH=.test-suites
PL_IT_TESTS_WAIT=false
PL_IT_TESTS_PROPAGATE=false"""
    }
  }
}

pipelineJob("${folder_path}/PipelineRunner") {
  description """<hr/>GENERATED ON: ${dateTime} *** <b>MANUAL CHANGES WILL BE OVERWRITTEN</b> ***"""
  properties {
    disableConcurrentBuilds()
    disableResume()
  }

  parameters {
    stringParam('pipeline_ref', 'master', 'Branch of pipeline code to use')
  }
  
  definition {
    cpsScm {
      scm {
        git {
          branch '${pipeline_ref}'
          remote {
            url pl_runner_repo_url
          }

          extensions {
            ignoreNotifyCommit()
          }
        }

      }

      lightweight false
      scriptPath '.jenkins/Jenkinsfile-pl-runner.groovy'
    }
  }
}

pipelineJob("${folder_path}/GlideBuild-ci") {
  description """<hr/>GENERATED ON: ${dateTime} *** <b>MANUAL CHANGES WILL BE OVERWRITTEN</b> ***"""
  properties {
    disableResume()
  }

  parameters {
    stringParam('scm_repo', '', 'Git repository to build')
    stringParam('scm_head', 'master', 'Branch of code to build')
    stringParam('scm_base', '', 'Branch to merge in before build')
    stringParam('last_build_time', '', 'Last build of scm_head in YYYY-MM-DDTHH:MM:SSZ format')
    stringParam('pipeline_ref', 'master', 'Branch of pipeline code to use')
  }

  definition {
    cpsScm {
      scm {
        git {
          branch '${pipeline_ref}'
          remote {
            url jobs_repo_url
          }

          extensions {
            ignoreNotifyCommit()
          }
        }

      }

      lightweight false
      scriptPath '.jenkins/Jenkinsfile-ci'
    }
  }
}

pipelineJob("${folder_path}/GlideBuild-merge") {
  description """<hr/>GENERATED ON: ${dateTime} *** <b>MANUAL CHANGES WILL BE OVERWRITTEN</b> ***"""
  properties {
    disableResume()

  }

  parameters {
    stringParam('sys_id', '', 'The execution id')
    stringParam('scm_head', '', 'Branch to merge from before build')
    stringParam('scm_base', 'master', 'Branch to merge in before build')
    stringParam('pipeline_ref', 'master', 'Branch of pipeline code to use')
    stringParam('email_to', '', 'comma separated list of emails to send notifications')
    stringParam('email_from', 'agileatscale@servicenow.com', 'the email from which emails are being sent')
    stringParam('merge_sys_id', '', 'The merge definition sys_id')
    stringParam('parent_execution', '', '')
    stringParam('glide_version', '', '')
    booleanParam('notify_by_email', false, 'Whether or not to notify by email of job status')
    booleanParam('lock_base', true, '')
    booleanParam('octopus_merge', false, '')
    booleanParam('continue_merge', false, '')
    booleanParam('skip_build_it', false, '')
    booleanParam('has_test_suites', false, '')
    booleanParam('remote_db_dump', false, '')
    booleanParam('dependency_publish', false, '')
    booleanParam('test_results_publish', true, '')
    booleanParam('it_test_results_publish', true, '')
    booleanParam('project_times_publish', false, '')
    booleanParam('ignore_build_it_failure', false, '')
    booleanParam('trigger_track_build', false, '')
    booleanParam('clean_ws', true, '')
    textParam('runtime_config', '', '')
  }

  definition {
    cpsScm {
      scm {
        git {
          branch '${pipeline_ref}'
          remote {
            url jobs_repo_url
          }

          extensions {
            ignoreNotifyCommit()
          }
        }

      }

      lightweight false
      scriptPath '.jenkins/Jenkinsfile-merge'
    }
  }
}

pipelineJob("${folder_path}/run-test-schedules") {
  description """<hr/>GENERATED ON: ${dateTime} *** <b>MANUAL CHANGES WILL BE OVERWRITTEN</b> ***"""
  properties {
    disableResume()
  }

  parameters {
    stringParam('glide_version', '', '')
    stringParam('scm_repos', '', '')
    stringParam('scm_head', 'master', '')
    stringParam('scm_base', '', '')
    stringParam('stream_id', '', '')
    stringParam('parent_sys_id', '', '')
    stringParam('sys_id', '', '')
    stringParam('test_suite', '', '')
    stringParam('pipeline_ref', 'master', '')
    stringParam('test_pipeline', '', '')
    stringParam('build_request_id', '', '')
    stringParam('email_to', '', '')
    stringParam('email_from', '', '')
    textParam('runtime_config', '', '')
    stringParam('test_cycle_size', '', '')
    booleanParam('notify_by_email', false, 'Whether or not to notify by email of job status')
    booleanParam('it_test_results_publish', true, 'Whether to publish test results')
    stringParam('app_versions', '[]', 'Versions for artifacts that were built by upstream jobs to be used for tests')
  }


  definition {
    cpsScm {
      scm {
        git {
          branch '${pipeline_ref}'
          remote {
            url jobs_repo_url
          }

          extensions {
            ignoreNotifyCommit()
          }
        }

      }

      lightweight false
      scriptPath '.jenkins/run-test-schedules.groovy'
    }
  }
}

pipelineJob("${folder_path}/run-test-project") {
  description """<hr/>GENERATED ON: ${dateTime} *** <b>MANUAL CHANGES WILL BE OVERWRITTEN</b> ***"""
  properties {
    disableResume()
  }

  parameters {
    stringParam('glide_version', '', '')
    stringParam('project_name', 'smoke-test', '')
    stringParam('tests', '')
    stringParam('scm_repo', 'dev/glide-test', '')
    stringParam('scm_head', 'master', '')
    stringParam('scm_base', '', '')
    stringParam('stream_id', '', '')
    stringParam('sys_id', '')
    stringParam('parent_sys_id', '')
    stringParam('retry', '', '')
    stringParam('pipeline_ref', 'master', '')
    stringParam('email_to', '', '')
    stringParam('email_from', '', '')
    stringParam('pass_criteria', '', '')
    textParam('runtime_config', '', '')
    textParam('dependencies', '', 'the projects that the IT project is dependent on')
    booleanParam('coverage', false, 'Whether collect the coverage or not')
    booleanParam('notify_by_email', false, 'Whether or not to notify by email of job status')
    booleanParam('it_test_results_publish', true, 'Whether to publish test results')
    booleanParam('nightly_build', false, 'nightly build flag')
    booleanParam('is_db_dump', false, 'Is the project is db dump or not')
    stringParam('app_versions', '[]', 'Versions for artifacts that were built by upstream jobs to be used for tests')
  }

  definition {
    cpsScm {
      scm {
        git {
          branch '${pipeline_ref}'
          remote {
            url jobs_repo_url
          }

          extensions {
            ignoreNotifyCommit()
          }
        }

      }

      lightweight false
      scriptPath '.jenkins/run-test-project.groovy'
    }
  }
}

pipelineJob("${folder_path}/run-test-batch") {
  description """<hr/>GENERATED ON: ${dateTime} *** <b>MANUAL CHANGES WILL BE OVERWRITTEN</b> ***"""
  properties {
    disableResume()
  }

  parameters {
    stringParam('glide_version', '', '')
    stringParam('project_name', 'smoke-test', '')
    stringParam('scm_repo', 'dev/glide-test', '')
    stringParam('scm_head', 'master', '')
    stringParam('scm_base', '', '')
    stringParam('stream_id', '', '')
    stringParam('sys_id', '')
    stringParam('parent_sys_id', '')
    stringParam('agent_name', '')
    stringParam('retry', '', '')
    stringParam('pipeline_ref', 'master', '')
    stringParam('email_to', '', '')
    stringParam('email_from', '', '')
    stringParam('pass_criteria', '', '')
    textParam('runtime_config', '', '')
    stringParam('test_pipeline', '', '')
    booleanParam('notify_by_email', false, 'Whether or not to notify by email of job status')
    booleanParam('nightly_build', false, 'nightly build flag')
    booleanParam('is_db_dump', false, 'Is the project is db dump or not')
  }

  definition {
    cpsScm {
      scm {
        git {
          branch '${pipeline_ref}'
          remote {
            url jobs_repo_url
          }

          extensions {
            ignoreNotifyCommit()
          }
        }
      }

      lightweight false
      scriptPath '.jenkins/run-test-batch.groovy'
    }
  }
}


pipelineJob("${folder_path}/build-release") {
  description """<hr/>GENERATED ON: ${dateTime} *** <b>MANUAL CHANGES WILL BE OVERWRITTEN</b> ***"""
  properties {
    disableResume()
  }

  parameters {
		stringParam('scm_head', '', '')
		stringParam('sys_id', '', '')
		stringParam('pipeline_ref', 'master', '')
		booleanParam('nightly_build', false, '')
		stringParam('glide_builddate', '', '')
		stringParam('glide_buildtag', '', '')
		stringParam('mid_builddate', '', '')
		stringParam('mid_buildstamp', '', '')
		stringParam('email_to', '', '')
		stringParam('email_from', '', '')
		booleanParam('notify_by_email', false, '')
		booleanParam('collect_metric_data', false, '')
		booleanParam('release_candidate', false, '')
		booleanParam('clean_ws', true, '')
		stringParam('runtime_config', '', '')
		stringParam('release_version', '', '')
  }

  definition {
    cpsScm {
      scm {
        git {
          branch '${pipeline_ref}'
          remote {
            url jobs_repo_url
          }

          extensions {
            ignoreNotifyCommit()
          }
        }
      }

      lightweight false
      scriptPath '.jenkins/build-release.groovy'
    }
  }
}


pipelineJob("${folder_path}/build-db-dump") {
  description """<hr/>GENERATED ON: ${dateTime} *** <b>MANUAL CHANGES WILL BE OVERWRITTEN</b> ***"""
  properties {
    disableResume()
  }

  parameters {
    stringParam('glide_version', '', '')
    stringParam('project_name', 'smoke-test', '')
    stringParam('tests', '')
    stringParam('scm_repo', 'dev/glide-test', '')
    stringParam('scm_head', 'master', '')
    stringParam('scm_base', '', '')
    stringParam('stream_id', '', '')
    stringParam('sys_id', '')
    stringParam('parent_sys_id', '')
    stringParam('agent_name', '')
    stringParam('retry', '', '')
    stringParam('pipeline_ref', 'master', '')
    stringParam('email_to', '', '')
    stringParam('email_from', '', '')
    stringParam('pass_criteria', '', '')
    textParam('runtime_config', '', '')
    textParam('dependencies', '', 'the projects that the IT project is dependent on')
    booleanParam('coverage', false, 'Whether collect the coverage or not')
    booleanParam('notify_by_email', false, 'Whether or not to notify by email of job status')
    booleanParam('it_test_results_publish', true, 'Whether to publish test results')
    booleanParam('nightly_build', false, 'nightly build flag')
    booleanParam('is_db_dump', false, 'Is the project is db dump or not')
  }

  definition {
    cpsScm {
      scm {
        git {
          branch '${pipeline_ref}'
          remote {
            url jobs_repo_url
          }

          extensions {
            ignoreNotifyCommit()
          }
        }

      }

      lightweight false
      scriptPath '.jenkins/build-db-dump.groovy'
    }
  }
}

pipelineJob("${folder_path}/run-parallel-builds") {
  description """<hr/>GENERATED ON: ${dateTime} *** <b>MANUAL CHANGES WILL BE OVERWRITTEN</b> ***"""
  properties {
    disableResume()
  }

  parameters {
    stringParam('apps', '', '')
    stringParam('stream_id', '', '')
    stringParam('sys_id', '')
    stringParam('parent_sys_id', '')
    stringParam('pipeline_ref', 'master', '')
    textParam('runtime_config', '{}', '')
    booleanParam('scoped_app', true, 'If the given projects are scoped apps')
    booleanParam('has_test_suites', false, '')
  }

  definition {
    cpsScm {
      scm {
        git {
          branch '${pipeline_ref}'
          remote {
            url app_jobs_repo_url
          }

          extensions {
            ignoreNotifyCommit()
          }
        }

      }

      lightweight false
      scriptPath '.jenkins/run-parallel-builds.groovy'
    }
  }
}

pipelineJob("${folder_path}/build-scoped-apps") {
  description """<hr/>GENERATED ON: ${dateTime} *** <b>MANUAL CHANGES WILL BE OVERWRITTEN</b> ***"""
  properties {
    disableResume()
  }

  parameters {
    stringParam('scm_repo', 'dev/glide-test', '')
    stringParam('scm_head', 'master', '')
    stringParam('poms', '', '')
    stringParam('stream_id', '', '')
    stringParam('sys_id', '')
    stringParam('parent_sys_id', '')
    stringParam('pipeline_ref', 'master', '')
    booleanParam('scoped_app', true, '')
    textParam('runtime_config', '{}', '')
  }

  definition {
    cpsScm {
      scm {
        git {
          branch '${pipeline_ref}'
          remote {
            url app_jobs_repo_url
          }

          extensions {
            ignoreNotifyCommit()
          }
        }

      }

      lightweight false
      scriptPath '.jenkins/build-scoped-apps.groovy'
    }
  }
}

pipelineJob("${folder_path}/get-controller-data") {
  description """<hr/>GENERATED ON: ${dateTime} *** <b>MANUAL CHANGES WILL BE OVERWRITTEN</b> ***"""
  properties {
    disableResume()
  }

  parameters {
    stringParam('pipeline_ref', 'master', 'Branch of pipeline code to use')
  }

  definition {
    triggers {
      cron '*/30 * * * *'
    }
    cpsScm {
      scm {
        git {
          branch '${pipeline_ref}'
          remote {
            url app_jobs_repo_url
          }

          extensions {
            ignoreNotifyCommit()
          }
        }

      }

      lightweight false
      scriptPath '.jenkins/get-controller-data.groovy'
    }
  }
}