import hudson.model.*
import java.text.SimpleDateFormat

def folder_path = "devops"

def isoFmt = new SimpleDateFormat("dd-MMM-yyyy' 'HH:mm:ss")
isoFmt.setTimeZone(TimeZone.getTimeZone('PST'))
def dateTime = isoFmt.format(new Date())

pipelineJob("${folder_path}/PipelineRunner") {
  description """<hr/>GENERATED ON: ${dateTime} *** <b>MANUAL CHANGES WILL BE OVERWRITTEN</b> ***"""
}

pipelineJob("${folder_path}/GlideBuild-ci") {
  description """<hr/>GENERATED ON: ${dateTime} *** <b>MANUAL CHANGES WILL BE OVERWRITTEN</b> ***"""
}

pipelineJob("${folder_path}/GlideBuild-merge") {
  description """<hr/>GENERATED ON: ${dateTime} *** <b>MANUAL CHANGES WILL BE OVERWRITTEN</b> ***"""
}