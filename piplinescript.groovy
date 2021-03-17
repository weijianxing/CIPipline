pipeline {
    
     agent {
         label 'extdocker' 
     }
     environment {
        rootDir = '/root/jenkins/workspace/test_pipeline/'
        remote_chrome    = 'http://selenium-hub.wuage-inc.com/wd/hub'
        //jenkins env
        job_name= "${env.JOB_NAME}"
        job_url = "${env.JOB_URL}"
        build_url = "${env.BUILD_URL}"
        build_number = "${env.BUILD_NUMBER}"
        node_name="${env.NODE_NAME}"
        workspace="/root/jenkins/workspace/${env.JOB_NAME}/gui-sample"
        goal="生产巡检"
        type="GUI"
        //dingding @
        WarningLevel1="魏建星,bla"
        WarningLevel2="cxo"
        
        
     }

   stages {
    stage('testrun') {
       agent {
    //       node {
    //   label 'master'
    //   customWorkspace "${BUILD_TAG}-id"
    // }
        docker { 
            image 'harbor.wuage.com/quality_assurance/java:v1.8.0_66' 
            args '-v $HOME/.m2:/root/.m2 -u root --privileged'
            label 'docker'
             reuseNode true
            //  customWorkspace "${BUILD_TAG}-id"
            }
        }
         steps {
            sh '''
                rm -rf *
                source /etc/locale.conf
                export LANG=en_US.UTF-8
                git clone https://wuage_test_group:i1yxWKi7NmfEOaSe@gitlab.wuage-inc.com/test/gui-sample.git
                echo "execute IM testing"
                cd gui-sample/Helper
                echo selenium.browser=chrome > src/main/resources/selenium.properties
                echo selenium.implicit.wait=150 >> src/main/resources/selenium.properties
                echo selenium.page.wait=130 >> src/main/resources/selenium.properties
                echo selenium.script.wait=130 >> src/main/resources/selenium.properties
                echo selenium.remote=http://10.2.20.43:4444/wd/hub  >> src/main/resources/selenium.properties
                cat src/main/resources/selenium.properties
               
                mvn gui:genPage -U -Duser.timezone=GMT+08 
                mvn gui:genTests -Duser.timezone=GMT+08
                mvn api:filter -Dfilter="DependencyCase !='1' and p ='1'  and enable !='0'  and  Path like '%%首页%%'" -U
                cd ..
                mvn clean test -Djava.awt.headless=true -Duser.timezone=GMT+08  || true
                export WORKSPACE=\$WORKSPACE
                export BUILD_NUMBER=\$BUILD_NUMBER
                export JOB_NAME=\$JOB_NAME
                export JOB_URL=\$JOB_URL
                export BUILD_URL=\$BUILD_URL
                export NODE_NAME=\$NODE_NAME
                export type="GUI"
                export goal="生产巡检"
                export WarningLevel1=\$WarningLevel1
                export WarningLevel2=\$WarningLevel2
                cd TestProject
                  mvn gui:report -Duser.timezone=GMT+08 || true
                mvn gui:check -Duser.timezone=GMT+08
                '''
         }
      }
  
    }
    post {
            
            failure {
                echo "test fail."
               
            }
             always {
                echo "pass execution " 
                
                script{
                    publishHTML (target: [
                        allowMissing: false,
                        alwaysLinkToLastBuild: false,
                        keepAll: true,
                        reportDir: './gui-sample/TestProject/target/surefire-reports/html',
                        reportFiles: 'index.html',
                        reportName: "testReport"
                    ])
            }
        
                
            }
        }
   
}
