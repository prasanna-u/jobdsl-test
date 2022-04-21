import hudson.model.*
import java.text.SimpleDateFormat

def team =  TEAM_JENKINS_URL ? "${TEAM_JENKINS_URL}".replaceAll('^(http|https)://.*\\/teams-','').replaceAll('/', '') : null
def folder_path = team ? "${team}/devops" : 'devops'
def isoFmt = new SimpleDateFormat("dd-MMM-yyyy' 'HH:mm:ss")
isoFmt.setTimeZone(TimeZone.getTimeZone('PST'))
def dateTime = isoFmt.format(new Date())

// This is to cleanup the devops folder to keep the jobs generated from script and delete everything else
def devopsJobs = hudson.model.Hudson.getInstance().getAllItems().findAll { it.getFullName().contains(folder_path) }
devopsJobs.forEach { job -> 
  // keep the jobs generated with timestamp within the last hour and delete everything else
  if (job.name != "devops" && !job.description.contains(dateTime.substring(0,14))) {
      job.delete()
    } 
}